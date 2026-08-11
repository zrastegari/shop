# زمینه‌ی پروژه — فیچرهای هم‌پیمایی و تاکسی اشتراکی

> این فایل برای دادن context به DeepSeek در چت‌های جدید نوشته شده، تا لازم نباشه کل
> تاریخچه‌ی مکالمات قبلی رو دوباره توضیح بدم.

## زمینه‌ی کلی پروژه

سیستم اصلی شرکت یک اپلیکیشن تاکسی‌یابی مشابه اسنپ است (جاوا/Spring Boot). دو قابلیت
جدید داره بهش اضافه می‌شه:

1. **هم‌پیمایی (Carpooling)** — ✅ **کامل و تست‌شده (فاز ۱)**
2. **تاکسی اشتراکی (Shared Taxi)** — 🔄 **در حال شروع (فاز ۲)**

من (نویسنده) یک برنامه‌نویس Java **جونیور** هستم، هنوز کد پروژه‌ی اصلی حمل‌ونقل شرکت
رو تحویل نگرفتم و ساختارش رو نمی‌شناسم. این دو فیچر رو در یک پروژه‌ی آزمایشی جدا
توسعه می‌دیم تا بعداً به پروژه‌ی اصلی منتقل بشن.

## تصمیمات کلیدی معماری

- **پروژه‌ی توسعه:** فیچرها داخل یک پروژه‌ی موجود و تست‌شده به اسم `shop` (پکیج پایه
  `com.learning.shop`، Java 21، Spring Boot 3.4.1) توسعه داده می‌شن — نه یک پروژه‌ی
  کاملاً جدا. دلیل: انتقال به پروژه‌ی اصلی در آینده صرفاً یک کار مکانیکی (Rename پکیج)
  خواهد بود، نه بازنویسی.
- **دیتابیس:** پروژه‌ی `shop` از قبل SQL Server داره (برای سرویس‌های Neshan قدیمی)،
  اما **دیتابیس پروژه‌ی اصلی شرکت MongoDB است** (تایید شده با تیم). به همین خاطر،
  مدل‌های داده‌ی هر دو فیچر جدید از همون ابتدا با **MongoDB** (Spring Data MongoDB)
  نوشته می‌شن، نه JPA. دو دیتابیس (SQL Server قدیمی + MongoDB جدید) در همین پروژه
  کنار هم هستن، بدون تداخل.
- **API های نقشه:** از Neshan API استفاده می‌شه، نه ساخت الگوریتم مسیریابی از صفر.
  یک سرویس تست‌شده و آماده به اسم `Neshanroutingservice` (در پکیج
  `com.learning.shop.service`) از قبل در پروژه هست، با متد:
  ```java
  public NeshanDistanceMatrixResponse getDistanceMatrix(String origins, String destinations)
  ```
  `origins`/`destinations` فرمت `"lat,lng|lat,lng"`؛ پاسخ شامل
  `rows[].elements[].distance.value` (متر) و `duration.value` (ثانیه). **این سرویس
  موجوده و نباید دوباره ساخته بشه** — فقط ازش استفاده می‌کنیم. به هیچ دیتابیسی
  وابسته نیست (فقط HTTP call می‌زنه)، پس با اضافه‌شدن MongoDB تغییری نمی‌کنه.
- **ساختار پکیج‌ها:**
  ```
  com.learning.shop.controller   -> کنترلرهای REST
  com.learning.shop.service      -> سرویس‌ها
  com.learning.shop.dto          -> DTO ها، زیرپکیج به ازای هر فیچر (dto.carpool, dto.sharedtaxi)
  com.learning.shop.document     -> مدل‌های MongoDB (@Document) — نه "entity"، چون در دنیای Mongo به این‌ها "document" می‌گن
  com.learning.shop.repository   -> Repository های Mongo (MongoRepository، نه JpaRepository)
  ```
- **محیط تست:** به‌جای embedded Mongo (Flapdoodle) یا Testcontainers، از **MongoDB
  واقعی نصب‌شده روی سیستم لوکال** (`localhost:27017`) برای تست‌های Repository
  استفاده می‌شه — چون Flapdoodle نمی‌تونست باینری mongod رو از `fastdl.mongodb.org`
  دانلود کنه (خطای HTTP 403، احتمالاً محدودیت شبکه). این مشکل حل شده و دیگه ازش
  استفاده نمی‌کنیم.

## فاز ۱: هم‌پیمایی (Carpooling) — ✅ کامل

راننده یا مسافر سفر خودشو با مبدأ/مقصد (به‌صورت «نقطه + شعاع»، نه نقطه‌ی دقیق) و
بازه‌ی زمانی ثبت می‌کنه؛ سیستم سفرهای هم‌مسیر رو پیدا کرده و دو طرف رو **معرفی**
می‌کنه (matching استاتیک، نه جفت‌سازی خودکار). هم داخل‌شهری هم برون‌شهری، کاملاً جدا
از هم بررسی می‌شن.

**الگوریتم matching (۴ مرحله):**
1. فیلتر ارزان در دیتابیس: `tripType` یکسان، `status=ACTIVE`، بازه‌ی زمانی هم‌پوشان
2. فیلتر فاصله‌ی هوایی (Haversine، محلی در جاوا): فاصله‌ی مبدأها و فاصله‌ی مقصدها هر
   کدوم باید ≤ مجموع شعاع دو سفر باشه
3. دقیق‌سازی با Neshan Distance Matrix API (قابل غیرفعال‌سازی با
   `carpool.matching.use-distance-matrix-api`): فاصله‌ی واقعی جاده‌ای زیر
   `carpool.matching.max-road-distance-meters` (پیش‌فرض 3000)
4. ثبت `CarpoolMatch` با `status=PENDING`؛ جلوگیری از match تکراری و match با خود کاربر

**فایل‌های ساخته‌شده:**
- Enum: `TripType` (IN_CITY/OUT_CITY), `TripStatus`, `MatchStatus`
- Document: `CarpoolTrip`, `CarpoolMatch`
- Repository: `CarpoolTripRepository`, `CarpoolMatchRepository`
- DTO (در `dto.carpool`): `CreateTripRequest`, `CreateTripResponse`, `TripResponse`,
  `MatchResponse`
- سرویس: `HaversineUtil`, `CarpoolTripService`, `CarpoolMatchingService`
- کنترلر: `CarpoolController` با ۶ endpoint

**نتایج تست:** ۱۳ تست، همه سبز (`HaversineUtilTest` ۳ تست، `CarpoolMatchingServiceTest`
۸ تست با Mockito، `CarpoolTripRepositoryTest` ۲ تست با Mongo لوکال واقعی).
`ShopApplicationTests` (تست پیش‌فرض اسکلت پروژه) خطا می‌ده چون کل context رو با
SQL Server لود می‌کنه — این خطا **بی‌ربط به Carpool** است و قابل نادیده‌گرفتنه.

## فاز ۲: تاکسی اشتراکی (Shared Taxi) — 🔄 در حال شروع

راننده در حین یک سفر فعال می‌تونه ۱-۲ مسافر دیگه با مسیر نزدیک رو هم سوار کنه. بر
خلاف فاز ۱ (matching استاتیک یک‌باره)، این یک تصمیم **real-time** است: «آیا مسافر
جدید رو می‌شه به این سفر فعال اضافه کرد؟»

**دو نقطه‌ی ورود (هر دو باید از یک تابع تصمیم مشترک استفاده کنن):**
1. Driver-initiated: راننده سفرش رو شروع می‌کنه → بین مسافرهای منتظر بگرد
2. Passenger-initiated: مسافر جدید درخواست می‌ده → بین سفرهای فعال با ظرفیت خالی بگرد

**ظرفیت:** راننده موقع شروع سفر تعداد ظرفیت اضافه رو اعلام می‌کنه
(`extraCapacity`)؛ ظرفیت باقیمانده همیشه **محاسبه‌شده** است (از روی توقف‌های
completed)، نه یک فیلد دستی جدا.

**الگوریتم (۵ مرحله):**
1. فیلترهای ارزان: ظرفیت باقیمانده > ۰، `tripType` یکسان، فیلتر خشن فاصله با
   `HaversineUtil` موجود
2. Enumeration ترتیب توقف‌ها: چون تعداد توقف‌ها کمه (حداکثر ۲-۳ تا)، همه‌ی ترتیب‌های
   ممکن insert کردن PICKUP/DROPOFF مسافر جدید رو enumerate می‌کنیم (بدون الگوریتم
   بهینه‌سازی پیچیده)
3. محاسبه‌ی هزینه‌ی هر گزینه با Neshan Distance Matrix API (همون سرویس فاز ۱):
   مسافت اضافه‌شده به مسیر راننده + بیشترین انحراف تحمیلی به مسافر(های) فعلی سوار
4. آستانه‌های قابل‌تنظیم: `sharedtaxi.matching.max-detour-meters-existing-passenger`،
   `sharedtaxi.matching.max-extra-distance-meters-driver`
5. انتخاب بهترین گزینه (کمترین مسافت اضافه) و ثبت `SharedTaxiOffer` با
   `status=PENDING` — فقط پیشنهاد می‌دیم، خودکار قبول نمی‌کنیم

**مدل داده (Document های جدید):** `ActiveSharedTrip` (با لیست embedded از
`TripStop`)، `WaitingPassenger`، `SharedTaxiOffer`

**نکته‌ی مهم:** enum `TripType` و کلاس‌های `HaversineUtil`/`Neshanroutingservice` از
فاز ۱ **دوباره استفاده می‌شن، دوباره ساخته نمی‌شن**.

پرامپت کامل و جزئی فاز ۲ (با مشخصات دقیق فایل‌ها، فیلدها، endpoint ها، و الزامات
تست) در یک فایل جدا (`shared-taxi-prompt-v1.md`) آماده شده و باید جدا داده بشه.

## نکات کیفیت کد (برای همه‌ی فیچرها)

- لایه‌بندی: Controller -> Service -> Repository
- لاگ‌گذاری (SLF4J) در نقاط تصمیم‌گیری الگوریتم matching
- کامنت‌های توضیحی فارسی، اسم کلاس/متد انگلیسی
- از Design Pattern های سنگین پرهیز بشه — کد ساده و مستقیم (سطح جونیور)
- در پایان هر پیاده‌سازی، توضیح داده بشه هر فایل/کلاس چیکار می‌کنه، و تفاوت‌های
  کلیدی MongoDB با JPA/SQL (که من باهاش آشناترم) توضیح داده بشه

## نکته‌ی امنیتی (برای یادآوری، نه اکشن فوری در DeepSeek)

رمز عبور واقعی SQL Server و کلید API واقعی Map.ir در `application.properties` قبلاً
در مکالمات افشا شدن و باید rotate/عوض بشن. این موضوع ربطی به کد فیچرها نداره، صرفاً
یادآوریه.

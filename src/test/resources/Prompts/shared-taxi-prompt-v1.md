# پرامپت برای Claude Code / DeepSeek — افزودن فیچر «تاکسی اشتراکی» (Shared Taxi، فاز ۲)

> این فیچر روی همون پروژه‌ی `shop` و روی همون زیرساخت MongoDB که برای فیچر «هم‌پیمایی»
> (Carpooling، فاز ۱) ساخته شده اضافه می‌شه. فاز ۱ کامل و تست‌شده است (۱۳ تست سبز) و
> نباید تغییر کنه. فیچر جدید کاملاً مستقل و کنار فاز ۱ اضافه می‌شه، فقط بعضی کلاس‌های
> Utility مثل `HaversineUtil` و سرویس `Neshanroutingservice` مجدداً استفاده می‌شن.

---

## PROMPT

من می‌خوام به پروژه‌ی موجود Spring Boot (`com.learning.shop`، Java 21، Spring Boot
3.4.1) که از قبل فیچر «هم‌پیمایی» (Carpooling) رو داره، یک فیچر جدید و مستقل به اسم
«تاکسی اشتراکی» (Shared Taxi) اضافه کنم. این فیچر هم از MongoDB استفاده می‌کنه (مثل
فیچر قبلی).

### تفاوت این فیچر با «هم‌پیمایی» (فاز ۱)

فاز ۱ یک matching **استاتیک** بود: دو سفر ثبت‌شده رو یک‌بار با هم مقایسه می‌کردیم و
پیشنهاد می‌دادیم. این فاز یک تصمیم **real-time** است: یک راننده در حال انجام یک سفر
فعاله (با مسافر یا مسافرهایی که از قبل سوارش هستن)، و باید تصمیم بگیریم آیا یک مسافر
جدید رو می‌شه به همین سفر اضافه کرد یا نه — با محاسبه‌ی ترتیب بهینه‌ی pickup/drop-off و
بررسی اینکه این کار چقدر مسیر مسافرهای فعلی رو منحرف می‌کنه.

### زمینه‌ی کسب‌وکار و منطق تصمیم‌گیری

**دو نقطه‌ی ورود به این الگوریتم داریم (هر دو باید پیاده‌سازی بشن):**

1. **Driver-initiated:** وقتی یک سفر فعال (`ActiveSharedTrip`) تغییر وضعیت پیدا می‌کنه
   یا شروع می‌شه، سیستم بین «مسافرهای منتظر» (کسانی که درخواست تاکسی اشتراکی ثبت
   کردن و هنوز به سفری وصل نشدن) دنبال بهترین کاندیدا می‌گرده.
2. **Passenger-initiated:** یک مسافر جدید درخواست سفر تاکسی اشتراکی ثبت می‌کنه؛
   سیستم بین «سفرهای فعال» (با ظرفیت خالی) دنبال بهترین گزینه می‌گرده.

هر دو ورودی باید در نهایت از همون **تابع تصمیم مرکزی** (که در ادامه توضیح داده می‌شه)
استفاده کنن — فقط جهت جستجو (کدام لیست را می‌گردیم) فرق می‌کنه. لطفاً این منطق مشترک
رو در یک متد/سرویس واحد پیاده‌سازی کن تا کد تکراری نشه.

**ظرفیت:** راننده موقع شروع سفر، تعداد ظرفیت اضافه (مثلاً ۱ یا ۲ نفر) رو مشخص می‌کنه
(فیلد `extraCapacity` روی سفر فعال). بعد از هر سوار شدن مسافر جدید، سیستم خودش
باید ظرفیت باقی‌مانده رو از روی تعداد مسافرهای فعلاً سوار محاسبه کنه (نه یک فیلد
جدا که دستی آپدیت بشه — محاسبه‌شونده باشه تا جای اشتباه نمونه).

### مدل داده — Document های جدید MongoDB

در پکیج `com.learning.shop.document` این‌ها رو اضافه کن:

**۱. `ActiveSharedTrip`** — `@Document(collection = "active_shared_trips")`:
- `id` (String)
- `driverId` (Long)
- `tripType` (از enum موجود `TripType`: IN_CITY / OUT_CITY — همون enum فاز ۱ رو
  استفاده کن، دوباره نساز)
- `extraCapacity` (Integer) — ظرفیت اضافه‌ای که راننده موقع شروع سفر اعلام کرده
- `originLat`, `originLng` (Double) — مبدأ اولیه‌ی سفر راننده
- `finalDestLat`, `finalDestLng` (Double) — مقصد نهایی راننده
- `currentLat`, `currentLng` (Double) — موقعیت فعلی راننده (فرض می‌کنیم یک API دیگه
  این رو آپدیت می‌کنه؛ فعلاً فقط فیلدش رو بساز)
- `stops` — لیستی از یک کلاس تو‌کار (embedded، نه Document جدا) به اسم `TripStop` با
  فیلدهای: `passengerId` (Long)، `type` (enum: PICKUP / DROPOFF)، `lat`, `lng`
  (Double)، `sequenceOrder` (Integer)، `completed` (Boolean) — این لیست، ترتیب فعلی
  توقف‌های سفره
- `status` (enum جدید `SharedTripStatus`: ACTIVE, COMPLETED, CANCELLED)
- `createdAt` (LocalDateTime)

**۲. `WaitingPassenger`** — `@Document(collection = "waiting_passengers")`:
- `id` (String)
- `passengerId` (Long)
- `tripType` (TripType)
- `pickupLat`, `pickupLng` (Double)
- `dropoffLat`, `dropoffLng` (Double)
- `requestedAt` (LocalDateTime)
- `status` (enum جدید `WaitingStatus`: WAITING, MATCHED, CANCELLED, EXPIRED)

**۳. `SharedTaxiOffer`** — `@Document(collection = "shared_taxi_offers")`:
- `id` (String)
- `activeTripId` (String) — اشاره به `ActiveSharedTrip`
- `waitingPassengerId` (String) — اشاره به `WaitingPassenger`
- `proposedStops` — لیست `TripStop` پیشنهادی (ترتیب جدید کامل، شامل توقف‌های قبلی +
  دو توقف جدید مسافر)
- `extraDistanceMetersForDriver` (Double) — مسافت اضافه‌شده به مسیر کل راننده
- `maxDetourMetersForExistingPassengers` (Double) — بیشترین انحرافی که به
  مسافر(های) فعلی سوار تحمیل می‌شه (اگه هیچ مسافر فعلی سوار نیست، صفر)
- `status` (enum جدید `OfferStatus`: PENDING, ACCEPTED, REJECTED, EXPIRED)
- `createdAt` (LocalDateTime)

Repository های متناظر (`ActiveSharedTripRepository`, `WaitingPassengerRepository`,
`SharedTaxiOfferRepository`) از `MongoRepository` ارث ببرن — دقیقاً مثل الگوی فاز ۱.

### الگوریتم — این بخش مهم‌ترین قسمته

پیاده‌سازی در یک سرویس جدید به اسم `SharedTaxiMatchingService` (در
`com.learning.shop.service`).

**مرحله ۱ — فیلترهای ارزان:**
قبل از هر محاسبه‌ی مسیر، این شرط‌ها رو در جاوا (یا کوئری ساده) چک کن:
- ظرفیت باقیمانده‌ی سفر فعال > ۰ — محاسبه‌شده از: `extraCapacity` منهای تعداد
  توقف‌های PICKUP که `completed=true` هستن ولی توقف DROPOFF متناظرشون هنوز
  `completed=false` است (یعنی مسافرهایی که الان واقعاً سوار هستن)
- `tripType` یکسان بین سفر فعال و مسافر منتظر
- فیلتر خشن جهت/فاصله: با استفاده از `HaversineUtil` موجود (از فاز ۱، دوباره
  ننویس)، فاصله‌ی هوایی بین pickup مسافر جدید و نزدیک‌ترین نقطه از مسیر باقیمانده‌ی
  راننده (می‌تونی ساده‌سازی کنی: فاصله تا موقعیت فعلی راننده + فاصله تا مقصد نهایی
  راننده، جمعشون از یک آستانه‌ی قابل‌تنظیم بیشتر نباشه) — این یک فیلتر تقریبیه، فقط
  برای رد کردن گزینه‌های واضحاً بی‌ربط قبل از فراخوانی API

**مرحله ۲ — تولید گزینه‌های ترتیب (insertion enumeration):**
مسافر جدید دو توقف اضافه می‌کنه: `PICKUP` و `DROPOFF`. این دو باید در لیست
`stops` فعلی سفر فعال «چپونده» بشن، با این محدودیت‌ها:
- توقف‌های `completed=true` نباید جابجا بشن (ترتیبشون ثابته، قبلاً اتفاق افتادن)
- `PICKUP` جدید باید قبل از `DROPOFF` جدید بیاد
- بین توقف‌های باقیمانده (`completed=false`) + دو توقف جدید، همه‌ی ترتیب‌های
  ممکنی که این دو قانون رو رعایت می‌کنن رو enumerate کن (چون تعداد توقف‌های
  باقیمانده کمه — حداکثر ۲-۳ تا معمولاً — این تعداد حالت‌ها خیلی کوچیکه، نیازی به
  الگوریتم بهینه‌سازی پیچیده نیست، یک تابع recursive/permutation ساده کافیه)

**مرحله ۳ — محاسبه‌ی هزینه‌ی هر گزینه با Neshan Distance Matrix API:**
از سرویس موجود `Neshanroutingservice.getDistanceMatrix(origins, destinations)`
استفاده کن (سرویس جدید نساز). برای هر گزینه‌ی ترتیب پیشنهادی از مرحله ۲:
- فاصله‌ی کل مسیر پیشنهادی رو با جمع کردن فاصله‌ی بین توقف‌های متوالی حساب کن
  (هر جفت توقف متوالی یک فراخوانی یا یک ردیف/ستون در Distance Matrix)
- `extraDistanceMetersForDriver` = فاصله‌ی کل مسیر جدید منهای فاصله‌ی کل مسیر فعلی
  (بدون مسافر جدید)
- برای هر مسافر فعلی سوار (بین PICKUP انجام‌شده و DROPOFF انجام‌نشده‌اش): فاصله‌ی
  باقیمانده‌ی مسیرش رو در دو حالت (با و بدون مسافر جدید) مقایسه کن، بیشترین این
  اختلاف = `maxDetourMetersForExistingPassengers`
- config flag برای غیرفعال‌سازی این مرحله در تست (مثل فاز ۱):
  `sharedtaxi.matching.use-distance-matrix-api=true/false`

**مرحله ۴ — آستانه‌های قبولی (قابل‌تنظیم در application.properties):**
- `sharedtaxi.matching.max-detour-meters-existing-passenger=2000` — بیشترین
  انحراف قابل‌قبول برای مسافر(های) فعلی
- `sharedtaxi.matching.max-extra-distance-meters-driver=5000` — بیشترین مسافت
  اضافه‌ی قابل‌قبول برای کل مسیر راننده
- گزینه فقط اگر هر دو آستانه رعایت بشه قابل‌قبوله

**مرحله ۵ — انتخاب بهترین گزینه:**
از بین گزینه‌های قابل‌قبول، اونی با کمترین `extraDistanceMetersForDriver` انتخاب
می‌شه. یک `SharedTaxiOffer` با `status=PENDING` ساخته می‌شه. اگه هیچ گزینه‌ی
قابل‌قبولی نبود، هیچ offer ای ساخته نمی‌شه (نه خطا، فقط لیست خالی برگردون).

**نکته‌ی مهم:** این الگوریتم رو در یک متد عمومی و مشترک بنویس (مثلاً
`findBestOffer(ActiveSharedTrip activeTrip, WaitingPassenger passenger)`) که هم
از مسیر Driver-initiated و هم Passenger-initiated صدا زده بشه — تفاوت فقط در اینه
که کدوم endpoint این متد رو با کدوم لیست از کاندیداها صدا می‌زنه.

### API های REST مورد نیاز

- `POST /api/sharedtaxi/active-trips` — راننده سفر فعال جدید با ظرفیت اضافه ثبت
  می‌کنه؛ بلافاصله بین مسافرهای منتظر (WAITING) بگرد و اگه offer مناسبی پیدا شد،
  در پاسخ برگردون (Driver-initiated)
- `POST /api/sharedtaxi/waiting-passengers` — مسافر درخواست جدید ثبت می‌کنه؛
  بلافاصله بین سفرهای فعال (ACTIVE با ظرفیت خالی) بگرد و بهترین offer رو برگردون
  (Passenger-initiated)
- `GET /api/sharedtaxi/active-trips/{id}`
- `GET /api/sharedtaxi/offers/{id}`
- `POST /api/sharedtaxi/offers/{id}/accept` — offer را `ACCEPTED` کن، توقف‌های
  `proposedStops` رو جایگزین `stops` سفر فعال کن، وضعیت `WaitingPassenger` رو
  `MATCHED` کن
- `POST /api/sharedtaxi/offers/{id}/reject` — offer را `REJECTED` کن (مسافر
  `WAITING` می‌مونه، دوباره می‌تونه در جستجوهای بعدی کاندیدا بشه)
- `POST /api/sharedtaxi/active-trips/{id}/stops/{stopId}/complete` — یک توقف رو
  `completed=true` علامت بزن (برای اینکه ظرفیت واقعی و مرحله ۲ درست محاسبه بشن)

DTO های جدا برای Request/Response در `com.learning.shop.dto.sharedtaxi` (پکیج جدید،
مشابه الگوی `dto.carpool` از فاز ۱). Document ها مستقیم expose نشن. اعتبارسنجی
(`@NotNull`, `@Min` و غیره) روی DTO های Request.

### الزامات کیفیت کد

- لایه‌بندی: Controller -> Service -> Repository (همون الگوی فاز ۱)
- از `HaversineUtil` و `Neshanroutingservice` موجود دوباره استفاده کن، بازنویسی نکن
- از enum `TripType` موجود (فاز ۱) استفاده کن، دوباره نساز
- لاگ‌گذاری (SLF4J) در نقاط تصمیم‌گیری الگوریتم (مخصوصاً چرا یک گزینه رد شد —
  کدام آستانه رو رد کرد)
- کامنت‌های توضیحی فارسی، اسم کلاس/متد انگلیسی
- من یک برنامه‌نویس جونیور جاوا هستم؛ از Design Pattern های سنگین پرهیز کن، کد
  ساده و مستقیم بنویس
- توجه: enumeration ترتیب توقف‌ها (مرحله ۲) اولین جایی در این پروژه است که یک
  الگوریتم ترکیبی/recursive می‌نویسیم — لطفاً این تابع رو با کامنت‌های گام‌به‌گام
  توضیح بده چون از فاز ۱ پیچیده‌تره

### تست

- Unit test برای تابع enumeration ترتیب توقف‌ها (چند سناریو: صفر توقف قبلی، یک
  توقف قبلی، دو توقف قبلی؛ تایید اینکه PICKUP جدید همیشه قبل از DROPOFF جدید میاد و
  توقف‌های completed جابجا نمی‌شن)
- Unit test برای `SharedTaxiMatchingService` با Mockito (مشابه الگوی
  `CarpoolMatchingServiceTest` از فاز ۱): سناریوهای قبولی، رد به‌خاطر ظرفیت پر، رد
  به‌خاطر tripType متفاوت، رد به‌خاطر عبور از آستانه‌ی detour، رد به‌خاطر عبور از
  آستانه‌ی extra-distance
- برای تست Repository ها، از همون الگوی فاز ۱ استفاده کن: اتصال مستقیم به MongoDB
  لوکال (`localhost:27017`) با یک دیتابیس/پروفایل تست جدا (نه Flapdoodle embedded
  Mongo — قبلاً به‌خاطر محدودیت شبکه مشکل داشتیم و با Mongo لوکال حلش کردیم)
- فراخوانی‌های `Neshanroutingservice` رو در تست‌ها mock کن (Mockito)

### خروجی نهایی

قبل از شروع کدنویسی، یک خلاصه از فایل‌های جدیدی که قراره بسازی (اسم فایل + پکیج +
مسئولیت) نشون بده تا تایید کنم، بعد شروع به پیاده‌سازی کن.

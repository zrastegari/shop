📋 گزارش کامل پیشرفت — سرویس تاکسی اشتراکی (Shared Taxi)
✅ کارهای انجام‌شده (فاز ۱)
۱. Enums — پکیج com.learning.shop.document
فایل	توضیح
SharedTripStatus.java	ACTIVE / COMPLETED / CANCELLED
WaitingStatus.java	WAITING / MATCHED / CANCELLED / EXPIRED
OfferStatus.java	PENDING / ACCEPTED / REJECTED / EXPIRED
TripStopType.java	PICKUP / DROPOFF
۲. Documents — پکیج com.learning.shop.document
فایل	توضیح
TripStop.java	کلاس embedded (نه Document جدا) — شامل متد copy()
ActiveSharedTrip.java	سفر فعال (extraCapacity، stops، موقعیت فعلی، status)
WaitingPassenger.java	مسافر منتظر
SharedTaxiOffer.java	پیشنهاد (proposedStops، extraDistance، maxDetour)
⚠️ نکته: ActiveSharedTrip از enum موجود TripType (فاز ۱) استفاده می‌کند. اگر این enum در پکیج دیگه‌ای است، import آن باید چک شود (کامپایل موفق بود، پس همه‌چیز درسته).

۳. Repositories — پکیج com.learning.shop.repository
ActiveSharedTripRepository (MongoRepository)
WaitingPassengerRepository
SharedTaxiOfferRepository
۴. DTOs — پکیج com.learning.shop.dto.sharedtaxi
Request ها: CreateActiveTripRequest، CreateWaitingPassengerRequest
Response ها: ActiveTripResponse، TripStopResponse، OfferResponse، WaitingPassengerResponse، CreateActiveTripResponse، CreateWaitingPassengerResponse
۵. سرویس‌ها — پکیج com.learning.shop.service
SharedTaxiMatchingService — هسته الگوریتم ۵ مرحله‌ای:
مرحله ۱: فیلتر ارزان (ظرفیت، tripType، Haversine خشن)
مرحله ۲: enumerateInsertions() — enumeration بازگشتی ترتیب توقف‌ها
مرحله ۳: محاسبه مسافت با segmentDistance() (Neshan یا Haversine)
مرحله ۴: بررسی آستانه maxDetour و maxExtraDistance
مرحله ۵: انتخاب بهترین گزینه + ساخت offer با PENDING
متد public computeDetourBetween(passengerId, currentStops, proposedStops) برای تست unit مستقیم
SharedTaxiTripService — CRUD + accept/reject/completeStop
۶. Controller
SharedTaxiController — endpoint ها:
POST /api/sharedtaxi/active-trips (Driver-initiated + جستجوی فوری)
POST /api/sharedtaxi/waiting-passengers (Passenger-initiated + جستجوی فوری)
GET /api/sharedtaxi/active-trips/{id}
GET /api/sharedtaxi/offers/{id}
POST /api/sharedtaxi/offers/{id}/accept
POST /api/sharedtaxi/offers/{id}/reject
POST /api/sharedtaxi/active-trips/{id}/stops/{stopId}/complete
۷. تنظیمات
به application.properties اضافه شد:
sharedtaxi.matching.max-detour-meters-existing-passenger
sharedtaxi.matching.max-extra-distance-meters-driver
sharedtaxi.matching.use-distance-matrix-api
sharedtaxi.matching.coarse-distance-threshold-meters
۸. تست‌ها — پکیج src/test/java/com/learning/shop
SharedTaxiStopEnumerationTest — تست unit تابع enumeration
صفر/یک/دو توقف قبلی
قید PICKUP جدید قبل از DROPOFF جدید
توقف‌های completed ثابت در ابتدا
SharedTaxiMatchingServiceTest — تست Mockito:
سناریوی موفق (قبول)
ظرفیت پر → null
tripType متفاوت → null
coarse distance → null
ظرفیت محاسبه پویا
reject-by-extra-distance
تست ترکیبی rejectsWhenNoOptionSatisfiesBothThresholds
تست unit مستقیم computeDetourBetween (با ورودی دستی)
✅ وضعیت کامپایل
mvnw.cmd compile — موفق ✅
دستور تست هنوز اجرا نشده (آخرین فایل تست ساخته شده)
🔑 تصمیم مهم طراحی که گرفتی (مستند در کد هست)
در معماری enumeration، چون DROPOFF مسافر فعلی می‌تونه به‌عنوان اولین توقف پیشنهادی بیاد، همیشه حداقل یک گزینه با detour≈0 برای مسافر(های) فعلیِ منفرد وجود داره. نتیجه: reject نهایی معمولاً از مسیر extra-distance (یا ترکیب با detour) اتفاق می‌افته، نه detour خالص. این رفتار عمدی و مستند شده — نه باگ.

🔜 کارهای باقی‌مانده (برای چت بعدی)
۱. ⚠️ اولویت اول: اجرای تست‌ها
دستور تست هنوز اجرا نشده. این را اجرا کن:


Apply
cd C:\Users\PC\Downloads\shop; .\mvnw.cmd test
احتمالاً چند assert عددی (مخصوصاً در computeDetourBetween و سناریوی detour) باید با مقادیر واقعی Haversine تنظیم شود.

۲. بررسی import TripType
چون فایل‌های ما از TripType (موجود قبلی) استفاده می‌کنند، مطمئن شو پکیج آن درست import شده. کامپایل موفق بود، ولی در چت جدید نفعی ندارد دوباره چک شود.

۳. بررسی کامل فایل‌های Neshan
NeshanDistanceMatrixResponse ساختار rows → elements → distance.value دارد ✅ (با استفاده سرویس هماهنگ بود)
Neshanroutingservice.getDistanceMatrix(origins, destinations) دو-آرگومانی موجود است ✅
۴. ادامه پیاده‌سازی (طبق پرامپت اصلی)
اگر فاز ۲ به بعد وجود دارد، باید ادامه داده شود. فایل پرامپت را در چت جدید مجدداً بخوان.

🗂️ نکات مهم برای ادامه
Maven wrapper استفاده شود: .\mvnw.cmd (نه mvn)
PowerShell است، برای دستورات ترکیبی از ; استفاده کن نه &&
تعهدات Git احتمالاً باید بررسی شوند (پروژه .git دارد)
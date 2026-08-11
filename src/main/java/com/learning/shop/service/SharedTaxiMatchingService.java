package com.learning.shop.service;

import com.learning.shop.document.*;
import com.learning.shop.dto.neshan.NeshanDistanceMatrixResponse;
import com.learning.shop.repository.SharedTaxiOfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * هسته‌ی الگوریتم «تاکسی اشتراکی» (Shared Taxi Matching).
 * <p>
 * این یک تصمیم real-time است: «آیا مسافر جدیدِ در حال انتظار را می‌شود به یک
 * سفر فعال اضافه کرد؟». الگوریتم در ۵ مرحله انجام می‌شود تا ابتدا گزینه‌ها ارزان
 * فیلتر شوند و فقط در مرحله‌ی آخر (که هزینه‌بر است) فراخوانی خارجی انجام شود:
 *
 * <ol>
 *   <li><b>فیلتر ارزان</b> — ظرفیت باقی‌مانده &gt; ۰، tripType یکسان، فیلتر خشن فاصله با Haversine.</li>
 *   <li><b>تولید گزینه‌های ترتیب (insertion enumeration)</b> — همه‌ی ترتیب‌های ممکنِ
 *       «چپاندن» PICKUP و DROPOFF جدید در توقف‌های باقی‌مانده‌ی سفر فعال.</li>
 *   <li><b>محاسبه‌ی هزینه‌ی هر گزینه</b> — با Neshan Distance Matrix: مسافت اضافه‌شده به
 *       مسیر راننده + بیشترین انحراف تحمیل‌شده به مسافر(های) فعلی.</li>
 *   <li><b>بررسی آستانه‌ها</b> — detour و extra-distance قابل قبول باشند.</li>
 *   <li><b>انتخاب بهترین گزینه</b> — کمترین مسافت اضافه؛ یک SharedTaxiOffer با PENDING.</li>
 * </ol>
 *
 * فقط «پیشنهاد» می‌دهد، خودکار قبول نمی‌کند.
 */
@Service
public class SharedTaxiMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SharedTaxiMatchingService.class);

    private final SharedTaxiOfferRepository offerRepository;
    private final Neshanroutingservice neshanRoutingService;

    /** بیشترین انحراف قابل‌قبول برای مسافرهای فعلی (متر) */
    @Value("${sharedtaxi.matching.max-detour-meters-existing-passenger:2000}")
    private int maxDetourMetersExistingPassenger;

    /** بیشترین مسافت اضافه‌ی قابل‌قبول برای مسیر کل راننده (متر) */
    @Value("${sharedtaxi.matching.max-extra-distance-meters-driver:5000}")
    private int maxExtraDistanceMetersDriver;

    /** آیا مرحله‌ی ۳ (فراخوانی API نشون) فعال باشد؟ */
    @Value("${sharedtaxi.matching.use-distance-matrix-api:true}")
    private boolean useDistanceMatrixApi;

    /** آستانه‌ی فیلتر خشن فاصله در مرحله ۱ (متر) — قابل تنظیم */
    @Value("${sharedtaxi.matching.coarse-distance-threshold-meters:8000}")
    private double coarseDistanceThresholdMeters;

    public SharedTaxiMatchingService(SharedTaxiOfferRepository offerRepository,
                                     Neshanroutingservice neshanRoutingService) {
        this.offerRepository = offerRepository;
        this.neshanRoutingService = neshanRoutingService;
    }

    // ======================================================================
    //  متد مرکزی و مشترک — هم Driver-initiated و هم Passenger-initiated
    //  از همین متد استفاده می‌کنند؛ فقط جهتِ جستجو (کدام لیست) فرق می‌کند.
    // ======================================================================

    /**
     * تصمیم‌گیری مشترک: آیا مسافر جدید به این سفر فعال اضافه می‌شود؟
     * اگر بله یک {@link SharedTaxiOffer} با status=PENDING ثبت و برگردانده می‌شود؛
     * در غیر این‌صورت null (نه خطا).
     *
     * <p><b>نکته‌ی عمدی در طراحی enumeration:</b> چون DROPOFF مسافر فعلی می‌تواند
     * به‌عنوان اولین توقف پیشنهادی بیاید، همیشه حداقل یک گزینه با detour≈0 برای
     * مسافر(های) فعلی وجود دارد. پس reject نهایی معمولاً از مسیر extra-distance
     * (یا ترکیب detour + extra-distance) اتفاق می‌افتد، نه detour خالص. این رفتار
     * عمدی است، نه باگ.
     *
     * @param activeTrip سفر فعالی که قرار است مسافر جدید به آن اضافه شود
     * @param passenger  مسافر منتظر
     * @return بهترین offer پیشنهادی یا null اگر هیچ گزینه‌ی قابل‌قبولی نبود
     */
    public SharedTaxiOffer findBestOffer(ActiveSharedTrip activeTrip, WaitingPassenger passenger) {
        // ---- مرحله ۱: فیلترهای ارزان ----
        if (!hasRemainingCapacity(activeTrip)) {
            log.debug("offer رد شد: سفر {} ظرفیت خالی ندارد", activeTrip.getId());
            return null;
        }
        if (activeTrip.getTripType() != passenger.getTripType()) {
            log.debug("offer رد شد: tripType متفاوت (سفر={}, مسافر={})",
                    activeTrip.getTripType(), passenger.getTripType());
            return null;
        }
        if (!passesCoarseDistanceFilter(activeTrip, passenger)) {
            log.debug("offer رد شد: فیلتر خشن فاصله (مرحله ۱) برقرار نیست — سفر {}", activeTrip.getId());
            return null;
        }

        // ---- مرحله ۲: ساخت توقف‌های جدید مسافر و enumeration ترتیب‌ها ----
        TripStop newPickup = buildNewStop(passenger, TripStopType.PICKUP);
        TripStop newDropoff = buildNewStop(passenger, TripStopType.DROPOFF);

        List<List<TripStop>> insertionOptions =
                enumerateInsertions(activeTrip.getStops(), newPickup, newDropoff);

        if (insertionOptions.isEmpty()) {
            log.debug("offer رد شد: هیچ ترتیب ممکنی برای سفر {} یافت نشد", activeTrip.getId());
            return null;
        }

        // بهترین گزینه را پیدا می‌کنیم: کمترین مسافت اضافه‌ی راننده
        SharedTaxiOffer bestOffer = null;

        for (List<TripStop> proposedStops : insertionOptions) {
            double extraDistanceForDriver = totalRouteDistance(proposedStops)
                    - totalRouteDistance(currentRemainingStops(activeTrip));
            double maxDetour = computeMaxExistingPassengerDetour(activeTrip, proposedStops);

            // ---- مرحله ۴: بررسی آستانه‌ها ----
            if (maxDetour > maxDetourMetersExistingPassenger) {
                log.debug("offer رد شد: detour مسافران فعلی {} متر از آستانه {} بیشتر است (سفر {})",
                        maxDetour, maxDetourMetersExistingPassenger, activeTrip.getId());
                continue;
            }
            if (extraDistanceForDriver > maxExtraDistanceMetersDriver) {
                log.debug("offer رد شد: مسافت اضافه‌ی راننده {} متر از آستانه {} بیشتر است (سفر {})",
                        extraDistanceForDriver, maxExtraDistanceMetersDriver, activeTrip.getId());
                continue;
            }

            // ---- مرحله ۵: انتخاب بهترین گزینه (کمترین مسافت اضافه) ----
            if (bestOffer == null || extraDistanceForDriver < bestOffer.getExtraDistanceMetersForDriver()) {
                SharedTaxiOffer offer = new SharedTaxiOffer();
                offer.setActiveTripId(activeTrip.getId());
                offer.setWaitingPassengerId(passenger.getId());
                offer.setProposedStops(proposedStops);
                offer.setExtraDistanceMetersForDriver(extraDistanceForDriver);
                offer.setMaxDetourMetersForExistingPassengers(maxDetour);
                offer.setStatus(OfferStatus.PENDING);
                offer.setCreatedAt(LocalDateTime.now());
                bestOffer = offer;
            }
        }

        if (bestOffer == null) {
            log.debug("offer رد شد: هیچ گزینه‌ی قابل‌قبولی برای سفر {} پیدا نشد", activeTrip.getId());
            return null;
        }

        SharedTaxiOffer saved = offerRepository.save(bestOffer);
        log.info("offer جدید ثبت شد: id={}, سفر={}, مسافر={}, مسافت اضافه={}م، detour={}م",
                saved.getId(), saved.getActiveTripId(), saved.getWaitingPassengerId(),
                saved.getExtraDistanceMetersForDriver(),
                saved.getMaxDetourMetersForExistingPassengers());
        return saved;
    }

    // ======================================================================
    //  مرحله ۱ — فیلترهای ارزان
    // ======================================================================

    /**
     * محاسبه‌ی ظرفیت باقی‌مانده‌ی سفر فعال.
     * ظرفیت باقی‌مانده = extraCapacity منهای تعداد مسافرهایی که «فعلاً سوار هستند».
     * یک مسافر فعلاً سوار است وقتی PICKUP او انجام‌شده باشد اما DROPOFF او هنوز انجام‌نشده.
     */
    private int remainingCapacity(ActiveSharedTrip trip) {
        Set<Long> onboardPassengers = new HashSet<>();
        for (TripStop stop : trip.getStops()) {
            if (stop.getType() == TripStopType.PICKUP && Boolean.TRUE.equals(stop.getCompleted())) {
                onboardPassengers.add(stop.getPassengerId());
            }
        }
        // اگر DROPOFF هم انجام شده باشد، آن مسافر دیگر سوار نیست — حذفش می‌کنیم
        for (TripStop stop : trip.getStops()) {
            if (stop.getType() == TripStopType.DROPOFF && Boolean.TRUE.equals(stop.getCompleted())) {
                onboardPassengers.remove(stop.getPassengerId());
            }
        }
        return trip.getExtraCapacity() - onboardPassengers.size();
    }

    private boolean hasRemainingCapacity(ActiveSharedTrip trip) {
        return remainingCapacity(trip) > 0;
    }

    /**
     * فیلتر خشن جهت/فاصله با Haversine — فقط برای زود رد کردن گزینه‌های بی‌ربط.
     * قرارداد ساده: فاصله‌ی pickup مسافر تا موقعیت فعلی راننده + فاصله‌ی همان pickup
     * تا مقصد نهایی راننده باید از یک آستانه بیشتر نشود.
     */
    private boolean passesCoarseDistanceFilter(ActiveSharedTrip trip, WaitingPassenger passenger) {
        double fromCurrent = HaversineUtil.distanceMeters(
                trip.getCurrentLat(), trip.getCurrentLng(),
                passenger.getPickupLat(), passenger.getPickupLng());
        double toFinalDest = HaversineUtil.distanceMeters(
                passenger.getPickupLat(), passenger.getPickupLng(),
                trip.getFinalDestLat(), trip.getFinalDestLng());
        return (fromCurrent + toFinalDest) <= coarseDistanceThresholdMeters;
    }

    // ======================================================================
    //  مرحله ۲ — تولید ترتیب‌های ممکن (insertion enumeration)
    // ======================================================================

    /**
     * ساخت یک توقف جدید برای مسافر بر اساس نوع (PICKUP یا DROPOFF).
     */
    private TripStop buildNewStop(WaitingPassenger passenger, TripStopType type) {
        TripStop stop = new TripStop();
        stop.setPassengerId(passenger.getPassengerId());
        stop.setType(type);
        stop.setLat(type == TripStopType.PICKUP ? passenger.getPickupLat() : passenger.getDropoffLat());
        stop.setLng(type == TripStopType.PICKUP ? passenger.getPickupLng() : passenger.getDropoffLng());
        stop.setSequenceOrder(0); // بعداً در enumerate قرار می‌گیرد
        stop.setCompleted(false);
        return stop;
    }

        /**
     * تابع enumeration ترتیب توقف‌ها — مدل B (insert-only).
     *
     * <p><b>تصمیم طراحی (عمدی):</b> ترتیب نسبی همه‌ی توقف‌های موجود (چه
     * {@code completed} چه {@code pending}) هرگز تغییر نمی‌کند؛ فقط دو توقف جدید
     * مسافرِ تازه ({@code newPickup} و {@code newDropoff}) در شکاف‌های ممکنِ
     * بین/قبل/بعدِ این توالیِ دست‌نخورده درج می‌شوند (با این قید که
     * {@code newPickup} همواره قبل از {@code newDropoff} می‌آید). این عمداً است تا
     * «تعهد ضمنی» مسافرهای فعلی درباره‌ی ترتیب مسیرشان به هم نریزد و فضای جستجو
     * کنترل‌شده (از مرتبه‌ی O(m^2) و نه فاکتوریلی) بماند.
     *
     * <p>تعداد گزینه‌ها = C(m+2, 2) که m = تعداد توقف‌های موجود است
     * (برای m=0 → ۱، m=1 → ۳، m=2 → ۶).
     *
     * @param currentStops توقف‌های فعلی سفر فعال (به ترتیبِ موجودشان)
     * @param newPickup    توقف PICKUP مسافر جدید
     * @param newDropoff   توقف DROPOFF مسافر جدید
     * @return لیستی از ترتیب‌های کامل ممکن (هر ترتیب یک لیست TripStop کامل)
     */
    public List<List<TripStop>> enumerateInsertions(List<TripStop> currentStops,
                                                    TripStop newPickup,
                                                    TripStop newDropoff) {
        List<List<TripStop>> results = new ArrayList<>();
        int m = currentStops.size();
        // دو شکافِ مرتب i <= j برای درج PICKUP و DROPOFF در میان توالیِ موجودها
        for (int i = 0; i <= m; i++) {
            for (int j = i; j <= m; j++) {
                List<TripStop> option = new ArrayList<>();
                for (int k = 0; k < i; k++) option.add(currentStops.get(k)); // موجودهای قبل از PICKUP
                option.add(newPickup);
                for (int k = i; k < j; k++) option.add(currentStops.get(k)); // موجودهای بین PICKUP و DROPOFF
                option.add(newDropoff);
                for (int k = j; k < m; k++) option.add(currentStops.get(k)); // موجودهای بعد از DROPOFF
                for (int seq = 0; seq < option.size(); seq++) {
                    option.get(seq).setSequenceOrder(seq + 1);
                }
                results.add(option);
            }
        }
        return results;
}

    // ======================================================================
    //  مرحله ۳ — محاسبهی هزینه با Neshan / Haversine
    // ======================================================================


    /**
     * توقف‌های باقی‌مانده‌ی سفر فعال (بدون مسافر جدید) — برای محاسبه‌ی مسیر فعلی.
     * مسیر فعلی = position فعلی راننده → توقف‌های نه‌کامل → مقصد نهایی.
     */
    private List<TripStop> currentRemainingStops(ActiveSharedTrip trip) {
        List<TripStop> remaining = new ArrayList<>();
        // توقف‌های انجام‌شده را حذف می‌کنیم — از آن‌ها رد شده‌ایم
        for (TripStop stop : trip.getStops()) {
            if (!Boolean.TRUE.equals(stop.getCompleted())) {
                remaining.add(stop);
            }
        }
        return remaining;
    }

    /**
     * محاسبه‌ی مسافت کل یک مسیر (متر).
     * مسیر از «موقعیت فعلی راننده» شروع شده، از روی همه‌ی توقف‌ها می‌گذرد و
     * به «مقصد نهایی» می‌رسد. برای مسیری که توقف‌های انجام‌شده را دارد، باز هم
     * موقعیت فعلی نقطه شروع محسوب می‌شود (این‌ها نقاط آینده‌ی مسیر هستند).
     *
     * @param stops توقف‌های مسیر (که باید مسافت بین آن‌ها حساب شود)
     */
    private double totalRouteDistance(List<TripStop> stops) {
        // نقطه شروع: موقعیت فعلی راننده. اما این متدِ کمکی ژنریک برای محاسبه‌ی
        // مسافتِ خودِ توقف‌ها استفاده می‌شود؛ مبدأ و مقصد نهایی در متدِ صداکننده اضافه می‌شود.
        if (stops.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = 0; i < stops.size() - 1; i++) {
            total += segmentDistance(stops.get(i), stops.get(i + 1));
        }
        return total;
    }

    /**
     * مسافت بین دو نقطه (متر) — با نشون اگر فعال باشد، وگرنه با Haversine.
     */
    private double segmentDistance(TripStop a, TripStop b) {
        if (useDistanceMatrixApi) {
            Double neshan = measureViaNeshan(a, b);
            if (neshan != null) {
                return neshan;
            }
        }
        return HaversineUtil.distanceMeters(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }

    /**
     * فراخوانی Distance Matrix نشون برای مسافت واقعی بین دو توقف.
     *
     * @return مسافت بر حسب متر؛ یا null در صورت خطا/پاسخ نامعتبر
     */
    private Double measureViaNeshan(TripStop a, TripStop b) {
        try {
            String origins = a.getLat() + "," + a.getLng();
            String destinations = b.getLat() + "," + b.getLng();
            NeshanDistanceMatrixResponse response =
                    neshanRoutingService.getDistanceMatrix(origins, destinations);
            if (response == null || response.getRows() == null || response.getRows().isEmpty()) {
                return null;
            }
            List<NeshanDistanceMatrixResponse.MatrixElement> elements =
                    response.getRows().get(0).getElements();
            if (elements == null || elements.isEmpty()
                    || elements.get(0).getDistance() == null) {
                return null;
            }
            return elements.get(0).getDistance().getValue();
        } catch (Exception e) {
            log.error("خطا در فراخوانی Distance Matrix نشون: {}", e.getMessage());
            return null;
        }
    }

    /**
     * محاسبه‌ی بیشترین انحراف تحمیل‌شده به مسافرهای فعلیِ سوار.
     * برای هر مسافرِ فعلاً سوار (PICKUP انجام‌شده ولی DROPOFF انجام‌نشده)،
     * مسافت باقی‌مانده‌ی مسیرش را در دو حالت مقایسه می‌کنیم:
     * «با مسافر جدید» و «بدون مسافر جدید»؛ بیشترین اختلاف برمی‌گردد.
     * اگر هیچ مسافر فعلی سوار نباشد، صفر برمی‌گردد.
     */
    private double computeMaxExistingPassengerDetour(ActiveSharedTrip trip, List<TripStop> proposedStops) {
        // یافتن مسافرهایی که فعلاً سوار هستند
        Set<Long> onboard = currentOnboardPassengers(trip);
        if (onboard.isEmpty()) {
            return 0.0;
        }

        double maxDetour = 0.0;
        for (Long passengerId : onboard) {
            // مسافت باقی‌مانده بدون مسافر جدید (مسیر فعلی)
            double without = passengerRemainingDistance(passengerId, trip.getStops());
            // مسافت باقی‌مانده با مسافر جدید (مسیر پیشنهادی)
            double with = passengerRemainingDistance(passengerId, proposedStops);
            double detour = with - without;
            if (detour > maxDetour) {
                maxDetour = detour;
            }
        }
        return maxDetour;
    }

    /**
     * مسافرانِ فعلاً سوار: PICKUP انجام‌شده ولی DROPOFF نه‌انجام‌شده.
     */
    private Set<Long> currentOnboardPassengers(ActiveSharedTrip trip) {
        Set<Long> onboard = new HashSet<>();
        for (TripStop stop : trip.getStops()) {
            if (stop.getType() == TripStopType.PICKUP && Boolean.TRUE.equals(stop.getCompleted())) {
                onboard.add(stop.getPassengerId());
            }
        }
        for (TripStop stop : trip.getStops()) {
            if (stop.getType() == TripStopType.DROPOFF && Boolean.TRUE.equals(stop.getCompleted())) {
                onboard.remove(stop.getPassengerId());
            }
        }
        return onboard;
    }

    /**
     * مسافت باقی‌مانده‌ی مسیر یک مسافر مشخص در یک لیست توقف.
     * از «PICKUP» مسافر شروع می‌شود و تا «DROPOFF» او ادامه می‌یابد
     * (جمع مسافتِ پاره‌خط‌های بین نقاطِ این بازه).
     */
    private double passengerRemainingDistance(Long passengerId, List<TripStop> stops) {
        // اندیس‌های PICKUP و DROPOFF مسافر را در لیست پیدا می‌کنیم
        int pickupIdx = -1;
        int dropoffIdx = -1;
        for (int i = 0; i < stops.size(); i++) {
            TripStop s = stops.get(i);
            if (s.getPassengerId().equals(passengerId)) {
                if (s.getType() == TripStopType.PICKUP) {
                    pickupIdx = i;
                } else if (s.getType() == TripStopType.DROPOFF) {
                    dropoffIdx = i;
                }
            }
        }
        if (pickupIdx < 0 || dropoffIdx < 0 || dropoffIdx <= pickupIdx) {
            return 0.0;
        }
        double total = 0.0;
        for (int i = pickupIdx; i < dropoffIdx; i++) {
            total += segmentDistance(stops.get(i), stops.get(i + 1));
        }
        return total;
    }

    /**
     * محاسبه‌ی مستقیم انحراف (detour) تحمیل‌شده به یک مسافر مشخص، صرفاً با مقایسه‌ی
     * مسافت باقی‌مانده‌ی مسیر او در دو حالت «با مسافر جدید» و «بدون مسافر جدید».
     * <p>
     * این متد مستقل از enumeration کامل ساخته شده تا بتوان فرمول detour را به‌تنهایی
     * و با یک ورودی دستی (نه از طریق {@link #enumerateInsertions}) به‌صورت unit تست کرد.
     *
     * @param passengerId   شناسه‌ی مسافرِ فعلی که انحرافش محاسبه می‌شود
     * @param currentStops  توقف‌های فعلیِ سفر (بدون مسافر جدید) — مبنای baseline
     * @param proposedStops توقف‌های پیشنهادی (با مسافر جدید) — مبنای مقایسه
     * @return انحراف بر حسب متر (اگر مسافر سوار نباشد یا قابل مقایسه نباشد، صفر)
     */
    public double computeDetourBetween(Long passengerId,
                                       List<TripStop> currentStops,
                                       List<TripStop> proposedStops) {
        double without = passengerRemainingDistance(passengerId, currentStops);
        double with = passengerRemainingDistance(passengerId, proposedStops);
        return with - without;
    }
}

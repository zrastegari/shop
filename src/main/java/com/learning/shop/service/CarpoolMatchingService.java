package com.learning.shop.service;

import com.learning.shop.document.CarpoolMatch;
import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.MatchStatus;
import com.learning.shop.document.TripStatus;
import com.learning.shop.dto.neshan.NeshanDistanceMatrixResponse;
import com.learning.shop.repository.CarpoolMatchRepository;
import com.learning.shop.repository.CarpoolTripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * هسته‌ی الگوریتم «هم‌پیمایی» (Carpooling Matching).
 * <p>
 * وقتی یک سفر جدید ثبت می‌شود، این سرویس برای آن هم‌مسیر پیدا می‌کند.
 * الگوریتم در ۴ مرحله انجام می‌شود تا ابتدا گزینه‌ها ارزان فیلتر شوند
 * و فقط در مرحله‌ی آخر (که هزینه‌بر است) فراخوانی خارجی انجام شود:
 *
 * <ol>
 *   <li><b>فیلتر ارزان در دیتابیس (Repository query)</b> — پیدا کردن سفرهای
 *       ACTIVE با همان tripType که بازه‌ی زمانی‌شان با سفر جدید هم‌پوشانی دارد
 *       (فیلتر بازه‌ی زمانی در جاوا انجام می‌شود).</li>
 *   <li><b>فیلتر فاصله‌ی هوایی (Haversine)</b> — فقط هم‌سفرهایی که مبدأ و مقصدشان
 *       در محدوده‌ی «نقطه + شعاع» باشد رد نمی‌شوند.</li>
 *   <li><b>دقیق‌سازی با Distance Matrix نشون</b> — فراخوانی سرویس موجود برای
 *       محاسبه‌ی مسافت واقعی جاده‌ای و مقایسه با آستانه‌ی تنظیم‌شده.</li>
 *   <li><b>ثبت match</b> — اگر همه‌ی مراحل رد شد، یک پیشنهاد SUGGESTED ثبت می‌شود.</li>
 * </ol>
 *
 * فقط «معرفی/پیشنهاد» انجام می‌دهد، نه جفت‌سازی خودکار.
 */
@Service
public class CarpoolMatchingService {

    private static final Logger log = LoggerFactory.getLogger(CarpoolMatchingService.class);

    private final CarpoolTripRepository tripRepository;
    private final CarpoolMatchRepository matchRepository;
    private final Neshanroutingservice neshanRoutingService;

    /** حداکثر مسافت جاده‌ای (متر) برای پذیرش match — از application.properties */
    @Value("${carpool.matching.max-road-distance-meters:3000}")
    private int maxRoadDistanceMeters;

    /** آیا مرحله‌ی ۳ (فراخوانی API نشون) فعال باشد؟ — از application.properties */
    @Value("${carpool.matching.use-distance-matrix-api:true}")
    private boolean useDistanceMatrixApi;

    public CarpoolMatchingService(CarpoolTripRepository tripRepository,
                                  CarpoolMatchRepository matchRepository,
                                  Neshanroutingservice neshanRoutingService) {
        this.tripRepository = tripRepository;
        this.matchRepository = matchRepository;
        this.neshanRoutingService = neshanRoutingService;
    }

    /**
     * اجرای الگوریتم matching برای یک سفر جدید ثبت‌شده (status=ACTIVE).
     *
     * @param newTrip سفر جدیدی که به‌تازگی ثبت شده
     * @return لیست match های پیدا شده (به صورت Document)
     */
    public List<CarpoolMatch> findAndCreateMatchesForNewTrip(CarpoolTrip newTrip) {
        if (newTrip.getStatus() != TripStatus.ACTIVE) {
            log.warn("سفر {} فعال نیست؛ matching انجام نشد", newTrip.getId());
            return Collections.emptyList();
        }

        // === مرحله ۱: فیلتر ارزان در دیتابیس ===
        // فقط سفرهای ACTIVE با همان tripType را می‌آوریم (فیلتر status و tripType در دیتابیس)
        List<CarpoolTrip> candidates = tripRepository
                .findByStatusAndTripType(TripStatus.ACTIVE, newTrip.getTripType());

        log.info("مرحله ۱: برای سفر {} تعداد {} سفر کاندید با tripType مشابه پیدا شد",
                newTrip.getId(), candidates.size());

        List<CarpoolMatch> createdMatches = new ArrayList<>();

        for (CarpoolTrip existing : candidates) {
            // خود سفر، یا سفرهایی که قبلاً MATCHED/CANCELLED هستند را رد می‌کنیم
            if (existing.getId().equals(newTrip.getId())) {
                continue;
            }

            // فیلتر userId متفاوت (راننده/مسافر خودش با خودش جفت نمی‌شود)
            if (existing.getUserId().equals(newTrip.getUserId())) {
                log.debug("سفر {} رد شد: کاربر یکسان", existing.getId());
                continue;
            }

            // فیلتر هم‌پوشانی بازه‌ی زمانی (در جاوا — چون match فقط یک‌بار انجام می‌شود)
            if (!timeRangesOverlap(newTrip, existing)) {
                log.debug("سفر {} رد شد: بازه‌ی زمانی هم‌پوشانی ندارد", existing.getId());
                continue;
            }

            // === مرحله ۲: فیلتر فاصله‌ی هوایی (Haversine) ===
            if (!withinHaversineRange(newTrip, existing)) {
                log.debug("سفر {} رد شد: در محدوده‌ی فاصله‌ی هوایی نیست (مرحله ۲)", existing.getId());
                continue;
            }

            // === مرحله ۳: دقیق‌سازی با نشون (Distance Matrix) — فقط اگر فعال باشد ===
            Double roadDistance = null;
            if (useDistanceMatrixApi) {
                log.debug("کاندیدا {} وارد مرحله ۳ شد: فراخوانی Distance Matrix نشون", existing.getId());
                roadDistance = measureRoadDistanceViaNeshan(newTrip, existing);
                if (roadDistance == null) {
                    log.warn("کاندیدا {} در مرحله ۳ به دلیل ناتوانی در محاسبه‌ی مسافت (خطا/پاسخ نامعتبر) match نمی‌شود", existing.getId());
                    continue;
                }
                if (roadDistance > maxRoadDistanceMeters) {
                    log.debug("کاندیدا {} در مرحله ۳ رد شد: مسافت جاده‌ای {} متر > آستانه‌ی {} متر",
                            existing.getId(), roadDistance, maxRoadDistanceMeters);
                    continue;
                }
            }

            // === مرحله ۴: ثبت match (جلوگیری از ثبت تکراری) ===
            if (isDuplicateMatch(newTrip.getId(), existing.getId())) {
                log.debug("match تکراری بین {} و {} رد شد", newTrip.getId(), existing.getId());
                continue;
            }

            CarpoolMatch match = createMatch(newTrip, existing, roadDistance);
            createdMatches.add(match);
            log.info("match جدید ایجاد شد: id={}, سفرهای {} و {}", match.getId(),
                    match.getTripOneId(), match.getTripTwoId());
        }

        return createdMatches;
    }

    // ---------------------------------------------------------------
    //  مراحل جداگانه‌ی الگوریتم
    // ---------------------------------------------------------------

    /**
     * بررسی هم‌پوشانی بازه‌ی زمانی دو سفر.
     * فرمول: newTrip.earliest <= existing.latest  &&  newTrip.latest >= existing.earliest
     */
    private boolean timeRangesOverlap(CarpoolTrip a, CarpoolTrip b) {
        return !a.getEarliestDepartureTime().isAfter(b.getLatestDepartureTime())
                && !a.getLatestDepartureTime().isBefore(b.getEarliestDepartureTime());
    }

    /**
     * بررسی شرط «نقطه + شعاع» برای مبدأ و مقصد هر دو سفر با فرمول Haversine.
     * <ul>
     *   <li>فاصله‌ی هوایی بین مبدأها باید &lt;= شعاع‌ها</li>
     *   <li>فاصله‌ی هوایی بین مقصدها باید &lt;= شعاع‌ها</li>
     * </ul>
     */
    private boolean withinHaversineRange(CarpoolTrip a, CarpoolTrip b) {
        double originDist = HaversineUtil.distanceMeters(
                a.getOriginLat(), a.getOriginLng(), b.getOriginLat(), b.getOriginLng());
        double originThreshold = a.getOriginRadiusMeters() + b.getOriginRadiusMeters();

        double destDist = HaversineUtil.distanceMeters(
                a.getDestLat(), a.getDestLng(), b.getDestLat(), b.getDestLng());
        double destThreshold = a.getDestRadiusMeters() + b.getDestRadiusMeters();

        boolean originOk = originDist <= originThreshold;
        boolean destOk = destDist <= destThreshold;

        if (!originOk || !destOk) {
            log.debug("فیلتر Haversine رد شد: مبدأ فاصله {}م > آستانه {}م؟ [{}]، مقصد فاصله {}م > آستانه {}م؟ [{}]",
                    (int) originDist, (int) originThreshold, originOk,
                    (int) destDist, (int) destThreshold, destOk);
            return false;
        }
        log.debug("فیلتر Haversine قبول شد: مبدأ {}م ≤ {}م، مقصد {}م ≤ {}م",
                (int) originDist, (int) originThreshold, (int) destDist, (int) destThreshold);
        return true;
    }

    /**
     * اندازه‌گیری مسافت واقعی جاده‌ای با فراخوانی سرویس موجود نشون (Distance Matrix).
     * <p>
     * مبدأِ سفر ۱ به مقصدِ سفر ۱ و مبدأِ سفر ۲ به مقصدِ سفر ۲ فرستاده می‌شود،
     * و فاصله‌ی بزرگ‌تر از بین این دو مسیر به‌عنوان امتیاز در نظر گرفته می‌شود
     * (تا با سخت‌گیری بیشتری هم‌مسیر بودن را بسنجیم).
     *
     * @return مسافت بر حسب متر؛ در صورت خطا یا پاسخ نامعتبر null برمی‌گرداند
     */
    private Double measureRoadDistanceViaNeshan(CarpoolTrip a, CarpoolTrip b) {
        try {
            // origins = مبدأ سفر ۱ | مبدأ سفر ۲ ، destinations = مقصد سفر ۱ | مقصد سفر ۲
            String origins = a.getOriginLat() + "," + a.getOriginLng() + "|"
                           + b.getOriginLat() + "," + b.getOriginLng();
            String destinations = a.getDestLat() + "," + a.getDestLng() + "|"
                                + b.getDestLat() + "," + b.getDestLng();

            log.debug("فراخوانی Distance Matrix نشون: origins=[{}] destinations=[{}]", origins, destinations);

            NeshanDistanceMatrixResponse response = neshanRoutingService.getDistanceMatrix(origins, destinations);
            if (response == null || response.getRows() == null || response.getRows().isEmpty()) {
                log.warn("پاسخ Distance Matrix خالی بود (response={})", response == null ? "null" : "rows empty");
                return null;
            }

            // پیدا کردن بیشترین مسافت جاده‌ای بین دو مسیر (نقطه ۱ به ۱ و ۲ به ۲)
            double maxDistance = 0.0;
            int rowIndex = 0;
            for (NeshanDistanceMatrixResponse.MatrixRow row : response.getRows()) {
                if (row.getElements() == null || rowIndex >= 2) {
                    rowIndex++;
                    continue;
                }
                // عنصر در ستونِ مطابق با همان سطر، مسیر همان سفر را نشان می‌دهد
                NeshanDistanceMatrixResponse.MatrixElement element =
                        row.getElements().get(rowIndex);
                if (element != null && element.getDistance() != null) {
                    maxDistance = Math.max(maxDistance, element.getDistance().getValue());
                }
                rowIndex++;
            }
            return maxDistance;
        } catch (Exception e) {
            log.error("خطا در فراخوانی Distance Matrix نشون: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * جلوگیری از ثبت match تکراری میان دو سفر (در هر دو جهت).
     */
    private boolean isDuplicateMatch(String tripOneId, String tripTwoId) {
        return matchRepository.existsByTripOneIdAndTripTwoIdAndStatus(tripOneId, tripTwoId, MatchStatus.SUGGESTED)
                || matchRepository.existsByTripOneIdAndTripTwoIdAndStatus(tripTwoId, tripOneId, MatchStatus.SUGGESTED);
    }

    /**
     * ساخت و ذخیره‌ی یک CarpoolMatch با وضعیت SUGGESTED.
     */
    private CarpoolMatch createMatch(CarpoolTrip a, CarpoolTrip b, Double roadDistance) {
        CarpoolMatch match = new CarpoolMatch();
        match.setTripOneId(a.getId());
        match.setTripTwoId(b.getId());
        match.setMatchedAt(LocalDateTime.now());
        match.setDistanceScoreMeters(roadDistance);
        match.setStatus(MatchStatus.SUGGESTED);
        return matchRepository.save(match);
    }

    // ---------------------------------------------------------------
    //  عملیات روی match ها — برای endpoints تأیید/رد
    // ---------------------------------------------------------------

    /**
     * پیدا کردن همه match های یک سفر (در هر دو جهت tripOne/tripTwo).
     */
    public List<CarpoolMatch> findMatchesForTrip(String tripId) {
        List<CarpoolMatch> result = new ArrayList<>();
        result.addAll(matchRepository.findByTripOneId(tripId));
        result.addAll(matchRepository.findByTripTwoId(tripId));
        return result;
    }

    /**
     * تأیید یک match: وضعیت match -> CONFIRMED و هر دو سفر -> MATCHED.
     */
    public CarpoolMatch confirmMatch(String matchId, CarpoolTripService tripService) {
        CarpoolMatch match = getMatch(matchId);
        match.setStatus(MatchStatus.CONFIRMED);
        matchRepository.save(match);

        // هر دو سفر مربوطه MATCHED می‌شوند
        tripService.markBothAsMatched(match.getTripOneId(), match.getTripTwoId());
        log.info("match {} تأیید شد", matchId);
        return match;
    }

    /**
     * رد کردن یک match: وضعیت match -> REJECTED.
     */
    public CarpoolMatch rejectMatch(String matchId) {
        CarpoolMatch match = getMatch(matchId);
        match.setStatus(MatchStatus.REJECTED);
        matchRepository.save(match);
        log.info("match {} رد شد", matchId);
        return match;
    }

    private CarpoolMatch getMatch(String matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("match با شناسه " + matchId + " یافت نشد"));
    }
}

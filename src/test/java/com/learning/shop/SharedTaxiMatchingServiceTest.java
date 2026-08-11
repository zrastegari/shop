package com.learning.shop;

import com.learning.shop.document.*;
import com.learning.shop.repository.SharedTaxiOfferRepository;
import com.learning.shop.service.Neshanroutingservice;
import com.learning.shop.service.SharedTaxiMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * تست واحد منطق تطبیق تاکسی اشتراکی.
 * <p>
 * در این تست‌ها {@link Neshanroutingservice} با Mockito mock می‌شود تا فراخوانی
 * واقعی به API نشون انجام نشود. به‌جای استفاده از Distance Matrix، مقدار
 * {@code useDistanceMatrixApi} را false می‌کنیم تا همه‌ی مسافت‌ها با Haversine
 * (قطعی و قابل پیش‌بینی) محاسبه شوند.
 */
@ExtendWith(MockitoExtension.class)
class SharedTaxiMatchingServiceTest {

    @Mock
    private SharedTaxiOfferRepository offerRepository;

    @Mock
    private Neshanroutingservice neshanRoutingService;

    @InjectMocks
    private SharedTaxiMatchingService matchingService;

    @BeforeEach
    void setUp() {
        // تنظیم مقادیر @Value تا به properties وابسته نباشیم
        ReflectionTestUtils.setField(matchingService, "useDistanceMatrixApi", false);
        ReflectionTestUtils.setField(matchingService, "maxDetourMetersExistingPassenger", 2000);
        ReflectionTestUtils.setField(matchingService, "maxExtraDistanceMetersDriver", 5000);
        ReflectionTestUtils.setField(matchingService, "coarseDistanceThresholdMeters", 1_000_000);
    }

    // ---------------------------------------------------------------
    //  سناریوی موفق
    // ---------------------------------------------------------------

    /**
     * موفق: ظرفیت خالی، tripType یکسان، pick/drop جدید نزدیک به هم.
     * باید یک offer با PENDING ساخته شود که حاوی دو توقف جدید مسافر است.
     */
    @Test
    void findBestOffer_whenFeasible_createsPendingOffer() {
        ActiveSharedTrip trip = newTrip(5L, TripType.IN_CITY, 1,
                35.7000, 51.3500, 35.7000, 51.3550);
        WaitingPassenger passenger = newPassenger(100L, TripType.IN_CITY,
                35.7001, 51.3501, 35.7002, 51.3502);

        when(offerRepository.save(any(SharedTaxiOffer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, passenger);

        assertNotNull(offer);
        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertEquals(trip.getId(), offer.getActiveTripId());
        assertEquals(passenger.getId(), offer.getWaitingPassengerId());

        // مسافر فعلی سوار نیست، پس detour باید 0 باشد
        assertEquals(0.0, offer.getMaxDetourMetersForExistingPassengers(), 0.001);
        assertNotNull(offer.getExtraDistanceMetersForDriver());

        // دو توقف جدید (PICKUP + DROPOFF) باید در proposedStops باشد
        long newStops = offer.getProposedStops().stream()
                .filter(s -> s.getPassengerId().equals(passenger.getPassengerId()))
                .count();
        assertEquals(2, newStops);

        verify(offerRepository).save(any(SharedTaxiOffer.class));
    }

    // ---------------------------------------------------------------
    //  مرحله ۱ — فیلترهای ارزان
    // ---------------------------------------------------------------

    /**
     * رد: سفر فعال ظرفیت خالی ندارد (extraCapacity=0 => ظرفیت باقی‌مانده=0).
     */
    @Test
    void findBestOffer_whenNoRemainingCapacity_returnsNull() {
        ActiveSharedTrip fullTrip = newTrip(5L, TripType.IN_CITY, 0,
                35.7000, 51.3500, 35.7000, 51.3550);
        WaitingPassenger passenger = newPassenger(100L, TripType.IN_CITY,
                35.7001, 51.3501, 35.7002, 51.3502);

        SharedTaxiOffer offer = matchingService.findBestOffer(fullTrip, passenger);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    /**
     * رد: tripType سفر فعال با مسافر متفاوت (IN_CITY در برابر OUT_CITY).
     */
    @Test
    void findBestOffer_whenTripTypeDiffers_returnsNull() {
        ActiveSharedTrip trip = newTrip(5L, TripType.IN_CITY, 1,
                35.7000, 51.3500, 35.7000, 51.3550);
        WaitingPassenger outCityPassenger = newPassenger(100L, TripType.OUT_CITY,
                35.7001, 51.3501, 35.7002, 51.3502);

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, outCityPassenger);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    /**
     * رد: مسافر خیلی دور از مسیر راننده است → فیلتر خشن فاصله (مرحله ۱) مانع می‌شود.
     */
    @Test
    void findBestOffer_whenCoarseDistanceTooLarge_returnsNull() {
        // آستانه‌ی خشن را ۱۰۰ متر می‌کنیم؛ مسافر در فاصله‌ی چند کیلومتری → رد
        ReflectionTestUtils.setField(matchingService, "coarseDistanceThresholdMeters", 100);

        ActiveSharedTrip trip = newTrip(5L, TripType.IN_CITY, 1,
                35.7000, 51.3500, 35.7000, 51.3550);
        WaitingPassenger farPassenger = newPassenger(100L, TripType.IN_CITY,
                35.7600, 51.4500, 35.7605, 51.4505);

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, farPassenger);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    // ---------------------------------------------------------------
    //  ظرفیت محاسبه‌شده
    // ---------------------------------------------------------------

    /**
     * ظرفیت به‌صورت پویا محاسبه می‌شود: یک مسافر فعلاً سوار (PICKUP انجام‌شده ولی
     * DROPOFF انجام‌نشده) ظرفیت را از extraCapacity کم می‌کند. با extraCapacity=1
     * و یک مسافر سوار، دیگر ظرفیت خالی نیست → offer ساخته نمی‌شود.
     */
    @Test
    void findBestOffer_whenOnboardPassengerFillsCapacity_returnsNull() {
        // یک مسافر فعلی سوار (passengerId=50) با PICKUP انجام‌شده و DROPOFF انجام‌نشده
        ActiveSharedTrip trip = newTripWithOnboard(5L, TripType.IN_CITY, 1,
                50L, 35.7000, 51.3500, 35.7000, 51.3550);
        WaitingPassenger passenger = newPassenger(100L, TripType.IN_CITY,
                35.7001, 51.3501, 35.7002, 51.3502);

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, passenger);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    // ---------------------------------------------------------------
    //  رد در مرحله‌ی ۴ — آستانه‌ی extra-distance
    // ---------------------------------------------------------------

    /**
     * رد: مسافت اضافه‌ی مسیر راننده از آستانه‌ی maxExtraDistance عبور می‌کند.
     * pick/drop جدید آن‌قدر از هم دورند (بیش از ۵km) که هر ترتیبِ پیشنهادی
     * مسافت اضافه بزرگ‌تری دارد و از آستانه می‌افتد.
     */
    @Test
    void findBestOffer_whenExtraDistanceExceedsThreshold_returnsNull() {
        ActiveSharedTrip trip = newTrip(5L, TripType.IN_CITY, 1,
                35.7000, 51.3500, 35.7000, 51.3550);
        // pick و drop حدود ۵۰ کیلومتر از هم دور — مسافت بینشان ≫ آستانه‌ی ۵۰۰۰
        WaitingPassenger farApart = newPassenger(200L, TripType.IN_CITY,
                35.7001, 51.3501, 35.9000, 51.9000);

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, farApart);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    // ---------------------------------------------------------------
    //  ردِ ترکیبی (detour + extra-distance)
    // ---------------------------------------------------------------

    /**
     * ردِ مرکب: هیچ گزینه‌ای هر دو آستانه را هم‌زمان برآورده نمی‌کند.
     * <p>
     * توضیحِ چرا «detour خالص» در اینجا به‌تنهایی آزمودنی نیست: چون DROPOFF مسافر
     * فعلی می‌تواند به‌عنوان اولین توقف پیشنهادی بیاید، همیشه حداقل یک گزینه با
     * detour≈0 برای مسافر(های) فعلی وجود دارد. پس reject نهایی در این معماری از
     * مسیرِ extra-distance (یا ترکیب extra-distance با detour) اتفاق می‌افتد، نه
     * detour خالص. این تست همان رفتارِ مرکب را پوشش می‌دهد:
     * مسافر فعلیِ سوار (p50) داریم و pick/drop جدیدِ خیلی‌دوردست از مسیر او؛ در
     * نتیجه همه‌ی گزینه‌ها یا از آستانه‌ی extra رد می‌شوند یا از آستانه‌ی detour.
     */
    @Test
    void rejectsWhenNoOptionSatisfiesBothThresholds() {
        // مسافر فعلی سوار: pickup انجام‌شده، dropoff انجام‌نشده — مسیر کوتاه ۵۵۰م
        ActiveSharedTrip trip = newTripWithOnboard(5L, TripType.IN_CITY, 1,
                50L, 35.7000, 51.3500, 35.7000, 51.3550);
        // pick/drop جدید از مسیر کوتاهِ مسافر فعلی خیلی دور (≈ ده‌ها کیلومتر از هم)
        WaitingPassenger farPassenger = newPassenger(200L, TripType.IN_CITY,
                35.9000, 51.9000, 35.9100, 51.9050);

        SharedTaxiOffer offer = matchingService.findBestOffer(trip, farPassenger);

        assertNull(offer);
        verify(offerRepository, never()).save(any(SharedTaxiOffer.class));
    }

    // ---------------------------------------------------------------
    //  تست unitِ مستقیم منطق محاسبه‌ی detour
    // ---------------------------------------------------------------

    /**
     * صحت فرمول detour به‌تنهایی (بدون enumeration کامل).
     * وقتی pick جدید مسیرِ مسافرِ فعلی را از مسیر مستقیمش منحرف می‌کند،
     * detour باید مثبت و تقریباً برابرِ مسافتِ اضافه‌شده باشد.
     */
    @Test
    void computeDetourBetween_injectsPositiveExtraDistance() {
        // ترتیبِ فعلی: pickup50 (انجام‌شده) → dropoff50 (انجام‌نشده)
        TripStop p50Pickup = stop(50L, TripStopType.PICKUP, true, 35.7000, 51.3500);
        TripStop p50Dropoff = stop(50L, TripStopType.DROPOFF, false, 35.7000, 51.3550);
        List<TripStop> current = List.of(p50Pickup, p50Dropoff);

        // ترتیبِ پیشنهادی: pickup50 → pickN (نقطه‌ی دورتر) → dropoff50
        // این pickN مسیر مسافر 50 را مجبور به انحراف به بیرون می‌کند
        TripStop pickN = stop(100L, TripStopType.PICKUP, false, 35.7002, 51.3600);
        List<TripStop> proposed = List.of(p50Pickup, pickN, p50Dropoff);

        double detour = matchingService.computeDetourBetween(50L, current, proposed);

        // انحراف باید مثبت باشد (مسیر با pickN طولانی‌تر از مسیر مستقیم است)
        assertTrue(detour > 0, "انحراف باید مثبت باشد، اما " + detour + " بود");
        // مسافت اضافه‌ی واقعی بین چند صد متر تا چند کیلومتر است
        assertTrue(detour > 100, "انحراف باید چند صد متر باشد، اما " + detour + " بود");
    }

    /**
     * صحت فرمول detour به‌تنهایی: وقتی dropoff مسافر فعلی بلافاصله بعد از pickup
     * او می‌آید (گزینه‌ی dropoff-اولِ شناخته‌شده)، مسیر مسافرِ فعلی تغییر نمی‌کند
     * و detour باید صفر باشد.
     */
    @Test
    void computeDetourBetween_whenDropoffFirst_isZero() {
        TripStop p50Pickup = stop(50L, TripStopType.PICKUP, true, 35.7000, 51.3500);
        TripStop p50Dropoff = stop(50L, TripStopType.DROPOFF, false, 35.7000, 51.3550);
        List<TripStop> current = List.of(p50Pickup, p50Dropoff);

        // گزینه‌ی dropoff-اول: pickup50 → dropoff50 → pickN → dropN
        TripStop pickN = stop(100L, TripStopType.PICKUP, false, 35.7002, 51.3600);
        TripStop dropN = stop(100L, TripStopType.DROPOFF, false, 35.7003, 51.3601);
        List<TripStop> proposed = List.of(p50Pickup, p50Dropoff, pickN, dropN);

        double detour = matchingService.computeDetourBetween(50L, current, proposed);

        // مسیر مسافر 50 از pickup به dropoff بدون تغییر (dropoff بلافاصله) => detour=0
        assertEquals(0.0, detour, 0.01);
    }

    // ---------------------------------------------------------------
    //  متدهای کمکی ساخت داده
    // ---------------------------------------------------------------

    private ActiveSharedTrip newTrip(long driverId, TripType type, int extraCapacity,
                                     double oLat, double oLng, double dLat, double dLng) {
        ActiveSharedTrip trip = new ActiveSharedTrip();
        trip.setId("trip-" + driverId + "-" + System.nanoTime());
        trip.setDriverId(driverId);
        trip.setTripType(type);
        trip.setExtraCapacity(extraCapacity);
        trip.setOriginLat(oLat);
        trip.setOriginLng(oLng);
        trip.setFinalDestLat(dLat);
        trip.setFinalDestLng(dLng);
        trip.setCurrentLat(oLat);
        trip.setCurrentLng(oLng);
        trip.setStops(new ArrayList<>());
        trip.setStatus(SharedTripStatus.ACTIVE);
        return trip;
    }

    /** سفر با یک مسافر فعلیِ سوار (pickup انجام‌شده، dropoff انجام‌نشده) */
    private ActiveSharedTrip newTripWithOnboard(long driverId, TripType type, int extraCapacity,
                                                long onboardPassengerId,
                                                double pLat, double pLng, double dLat, double dLng) {
        ActiveSharedTrip trip = newTrip(driverId, type, extraCapacity, pLat, pLng, dLat, dLng);
        TripStop pickup = stop(onboardPassengerId, TripStopType.PICKUP, true, pLat, pLng);
        TripStop dropoff = stop(onboardPassengerId, TripStopType.DROPOFF, false, dLat, dLng);
        List<TripStop> stops = new ArrayList<>();
        stops.add(pickup);
        stops.add(dropoff);
        trip.setStops(stops);
        return trip;
    }

    private WaitingPassenger newPassenger(long passengerId, TripType type,
                                          double pLat, double pLng,
                                          double dLat, double dLng) {
        WaitingPassenger p = new WaitingPassenger();
        p.setId("wait-" + passengerId + "-" + System.nanoTime());
        p.setPassengerId(passengerId);
        p.setTripType(type);
        p.setPickupLat(pLat);
        p.setPickupLng(pLng);
        p.setDropoffLat(dLat);
        p.setDropoffLng(dLng);
        p.setStatus(WaitingStatus.WAITING);
        return p;
    }

    private TripStop stop(long passengerId, TripStopType type, boolean completed,
                          double lat, double lng) {
        TripStop s = new TripStop();
        s.setPassengerId(passengerId);
        s.setType(type);
        s.setCompleted(completed);
        s.setLat(lat);
        s.setLng(lng);
        return s;
    }
}

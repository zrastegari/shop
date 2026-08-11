package com.learning.shop;

import com.learning.shop.document.CarpoolMatch;
import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.TripStatus;
import com.learning.shop.document.TripType;
import com.learning.shop.dto.neshan.NeshanDistanceMatrixResponse;
import com.learning.shop.repository.CarpoolMatchRepository;
import com.learning.shop.repository.CarpoolTripRepository;
import com.learning.shop.service.CarpoolMatchingService;
import com.learning.shop.service.Neshanroutingservice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * تست واحد منطق matching.
 * <p>
 * در این تست‌ها {@link Neshanroutingservice} با Mockito mock می‌شود
 * تا فراخوانی واقعی به API نشون انجام نشود.
 */
@ExtendWith(MockitoExtension.class)
class CarpoolMatchingServiceTest {

    @Mock
    private CarpoolTripRepository tripRepository;

    @Mock
    private CarpoolMatchRepository matchRepository;

    @Mock
    private Neshanroutingservice neshanRoutingService;

    @InjectMocks
    private CarpoolMatchingService matchingService;

    /** یک سفر مرجع (فرضی) در تهران */
    private CarpoolTrip newTrip;

    @BeforeEach
    void setUp() {
        // تنظیم مقادیر @Value (تا به properties وابسته نباشیم)
        ReflectionTestUtils.setField(matchingService, "useDistanceMatrixApi", true);
        ReflectionTestUtils.setField(matchingService, "maxRoadDistanceMeters", 3000);

        newTrip = buildTrip(
                "new-id", 1L, 35.7000, 51.3500, 500, 35.8300, 50.9900, 500,
                TripType.IN_CITY, 18, 0, 19, 0);
    }

    // ---------------------------------------------------------------
    //  سناریوهای موفق
    // ---------------------------------------------------------------

    /**
     * match موفق: دو سفر هم‌شعاع، هم‌بازه، هم‌مسیر و مسافت جاده‌ای زیر آستانه.
     */
    @Test
    void findMatches_whenAllConditionsMet_createsMatch() {
        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 30, 19, 30);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));
        when(neshanRoutingService.getDistanceMatrix(anyString(), anyString()))
                .thenReturn(buildNeshanResponse(2000.0));
        when(matchRepository.existsByTripOneIdAndTripTwoIdAndStatus(anyString(), anyString(), any()))
                .thenReturn(false);
        when(matchRepository.save(any(CarpoolMatch.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertEquals(1, matches.size());
        assertEquals("existing-id", matches.get(0).getTripTwoId());
        // امتیاز مسافت باید برابر مقدار جواب نشون باشد
        assertEquals(2000.0, matches.get(0).getDistanceScoreMeters(), 0.001);
        verify(neshanRoutingService).getDistanceMatrix(anyString(), anyString());
    }

    /**
     * رد به‌خاطر بازه‌ی زمانی غیرهم‌پوشان.
     */
    @Test
    void findMatches_whenTimeRangeDoesNotOverlap_noMatch() {
        // بازه‌ی سفر موجود یک روز بعد است => هم‌پوشانی ندارد
        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 1, 0, 2, 0); // فردا

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        verify(neshanRoutingService, never()).getDistanceMatrix(anyString(), anyString());
        verify(matchRepository, never()).save(any(CarpoolMatch.class));
    }

    /**
     * رد به‌خاطر tripType متفاوت (داخلی در برابر برون‌شهری).
     * نکته: مرحله ۱ فیلتر tripType را در دیتابیس انجام می‌دهد،
     * پس حتی اگر سفر با tripType متفاوت در ناحیه برگردد، نباید match ایجاد شود.
     */
    @Test
    void findMatches_whenTripTypeDiffers_noMatch() {
        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of()); // سرویس فقط IN_CITY را می‌پرسد، پس OUT_CITY نمی‌آید

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        // هیچ سفر OUT_CITY حتی خوانده نشده است
        verify(tripRepository, never()).findByStatusAndTripType(any(), eq(TripType.OUT_CITY));
    }

    /**
     * رد به‌خاطر فاصله‌ی هوایی زیاد (مرحله ۲) — مبدأها خیلی دورند.
     */
    @Test
    void findMatches_whenHaversineDistanceTooLarge_noMatch() {
        // مبدأ در نقطه‌ای خیلی دورتر (مثلاً اصفهان 32.65,51.66) — بیش از مجموع شعاع‌ها
        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 32.6500, 51.6600, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 0, 19, 0);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        // در مرحله ۲ رد شده؛ پس نشون اصلاً صدا زده نمی‌شود
        verify(neshanRoutingService, never()).getDistanceMatrix(anyString(), anyString());
    }

    /**
     * رد در مرحله ۳: مسافت جاده‌ای بیش از آستانه (3km) است.
     */
    @Test
    void findMatches_whenRoadDistanceExceedsThreshold_noMatch() {
        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 30, 19, 30);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));
        when(neshanRoutingService.getDistanceMatrix(anyString(), anyString()))
                .thenReturn(buildNeshanResponse(5000.0)); // بیش از آستانه

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        verify(matchRepository, never()).save(any(CarpoolMatch.class));
    }

    /**
     * جلوگیری از match با سفر متعلق به همان کاربر.
     */
    @Test
    void findMatches_whenSameUser_skips() {
        CarpoolTrip sameUser = buildTrip(
                "existing-id", 1L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 30, 19, 30);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(sameUser));

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        verify(neshanRoutingService, never()).getDistanceMatrix(anyString(), anyString());
    }

    /**
     * جلوگیری از ثبت match تکراری.
     */
    @Test
    void findMatches_whenDuplicateMatchExists_noMatchCreatedAgain() {
        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 30, 19, 30);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));
        when(neshanRoutingService.getDistanceMatrix(anyString(), anyString()))
                .thenReturn(buildNeshanResponse(2000.0));
        when(matchRepository.existsByTripOneIdAndTripTwoIdAndStatus(anyString(), anyString(), any()))
                .thenReturn(true);

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertTrue(matches.isEmpty());
        verify(matchRepository, never()).save(any(CarpoolMatch.class));
    }

    /**
     * وقتی استفاده از Distance Matrix خاموش است (use-distance-matrix-api=false)،
     * match باید با عبور از مرحله ۲ مستقیماً در مرحله ۴ ثبت شود.
     */
    @Test
    void findMatches_whenDistanceMatrixApiDisabled_skipsStage3() {
        ReflectionTestUtils.setField(matchingService, "useDistanceMatrixApi", false);

        CarpoolTrip existing = buildTrip(
                "existing-id", 2L, 35.7020, 51.3520, 500, 35.8320, 50.9920, 500,
                TripType.IN_CITY, 18, 30, 19, 30);

        when(tripRepository.findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY))
                .thenReturn(List.of(existing));
        when(matchRepository.existsByTripOneIdAndTripTwoIdAndStatus(anyString(), anyString(), any()))
                .thenReturn(false);
        when(matchRepository.save(any(CarpoolMatch.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(newTrip);

        assertEquals(1, matches.size());
        // مسافت null است چون مرحله ۳ اجرا نشده
        assertNull(matches.get(0).getDistanceScoreMeters());
        verify(neshanRoutingService, never()).getDistanceMatrix(anyString(), anyString());
    }

    // ---------------------------------------------------------------
    //  متدهای کمکی ساخت داده
    // ---------------------------------------------------------------

    /** ساخت یک سفر با پارامتر مشخص — زمان پیش‌فرض هم‌روزه است */
    private CarpoolTrip buildTrip(String id, long userId,
                                  double oLat, double oLng, int oRadius,
                                  double dLat, double dLng, int dRadius,
                                  TripType tripType,
                                  int earliestHour, int earliestMin,
                                  int latestHour, int latestMin) {
        CarpoolTrip trip = new CarpoolTrip();
        trip.setId(id);
        trip.setUserId(userId);
        trip.setOriginLat(oLat);
        trip.setOriginLng(oLng);
        trip.setOriginRadiusMeters(oRadius);
        trip.setDestLat(dLat);
        trip.setDestLng(dLng);
        trip.setDestRadiusMeters(dRadius);
        trip.setTripType(tripType);
        trip.setStatus(TripStatus.ACTIVE);
        trip.setEarliestDepartureTime(LocalDateTime.now()
                .withHour(earliestHour).withMinute(earliestMin).withSecond(0).withNano(0));
        trip.setLatestDepartureTime(LocalDateTime.now()
                .withHour(latestHour).withMinute(latestMin).withSecond(0).withNano(0));
        return trip;
    }

    /** ساخت یک پاسخ Distance Matrix نشون برای مسیرهای روی قطر (۱ به ۱ و ۲ به ۲) */
    /** ساخت پاسخ Distance Matrix (2x2) — عناصر روی قطر برابر distanceValue هستند */
    private NeshanDistanceMatrixResponse buildNeshanResponse(double distanceValue) {
        NeshanDistanceMatrixResponse response = new NeshanDistanceMatrixResponse();

        // سطر اول: [distanceValue, 0]
        NeshanDistanceMatrixResponse.MatrixRow row1 = new NeshanDistanceMatrixResponse.MatrixRow();
        row1.setElements(java.util.List.of(makeElement(distanceValue), makeElement(0.0)));

        // سطر دوم: [0, distanceValue]
        NeshanDistanceMatrixResponse.MatrixRow row2 = new NeshanDistanceMatrixResponse.MatrixRow();
        row2.setElements(java.util.List.of(makeElement(0.0), makeElement(distanceValue)));

        response.setRows(java.util.List.of(row1, row2));
        return response;
    }

    /** ساخت یک عنصر ماتریس با مقدار فاصله‌ی مشخص */
    private NeshanDistanceMatrixResponse.MatrixElement makeElement(double value) {
        NeshanDistanceMatrixResponse.MatrixElement element = new NeshanDistanceMatrixResponse.MatrixElement();
        NeshanDistanceMatrixResponse.DistanceDuration distance = new NeshanDistanceMatrixResponse.DistanceDuration();
        distance.setValue(value);
        element.setDistance(distance);
        return element;
    }
}

package com.learning.shop;

import com.learning.shop.document.TripStop;
import com.learning.shop.document.TripStopType;
import com.learning.shop.service.SharedTaxiMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست واحدِ تابع enumeration ترتیب توقف‌ها در {@link SharedTaxiMatchingService}.
 * <p>
 * این تست‌ها بر پایه‌ی «مدل B (insert-only)» نوشته شده‌اند: ترتیب نسبی همه‌ی
 * توقف‌های موجود (چه completed چه pending) دست‌نخورده می‌ماند و فقط دو توقف جدید
 * (PICKUP و DROPOFF مسافر تازه) در شکاف‌های ممکنِ بین/قبل/بعدِ آن‌ها درج می‌شوند.
 * <p>
 * فرمول تعداد گزینه‌ها: با m توقفِ موجود → C(m+2, 2) گزینه.
 * (m=0 → ۱، m=1 → ۳، m=2 → ۶)
 */
class SharedTaxiStopEnumerationTest {

    private SharedTaxiMatchingService matchingService;

    @BeforeEach
    void setUp() {
        // فقط تابع enumeration را تست می‌کنیم؛ سرویس می‌تواند خالی ساخته شود.
        matchingService = new SharedTaxiMatchingService(null, null);
    }

    // ---- متدهای کمکی برای ساخت توقف ----

    private TripStop stop(Long passengerId, TripStopType type, boolean completed) {
        TripStop s = new TripStop();
        s.setPassengerId(passengerId);
        s.setType(type);
        s.setCompleted(completed);
        s.setLat(0.0);
        s.setLng(0.0);
        return s;
    }

    private TripStop pickup(Long passengerId) {
        return stop(passengerId, TripStopType.PICKUP, false);
    }

    private TripStop dropoff(Long passengerId) {
        return stop(passengerId, TripStopType.DROPOFF, false);
    }

    // ---- سناریوها ----

    /**
     * صفر توقف قبلی: باید دقیقاً ۱ گزینه باشد ([PICKUP جدید, DROPOFF جدید]).
     * فرمول: m=0 → C(2,2)=1
     */
    @Test
    void enumerate_whenNoExistingStops_producesOnlyPickupThenDropoff() {
        TripStop newPickup = pickup(100L);
        TripStop newDropoff = dropoff(100L);

        List<List<TripStop>> options =
                matchingService.enumerateInsertions(new ArrayList<>(), newPickup, newDropoff);

        assertEquals(1, options.size());
        List<TripStop> only = options.get(0);
        assertEquals(2, only.size());
        assertEquals(TripStopType.PICKUP, only.get(0).getType());
        assertEquals(TripStopType.DROPOFF, only.get(1).getType());
    }

    /**
     * یک توقف قبلیِ انجام‌نشده: ترتیب نسبی‌اش حفظ می‌شود، ولی چون یک توقفِ جدید
     * می‌تواند قبلش بیاید، index 0 لزوماً توقفِ موجود نیست.
     * فرمول: m=1 → C(3,2)=3 گزینه
     */
    @Test
    void enumerate_whenOneExistingIncompleteStop_keepsExistingOrderStable() {
        TripStop existing = stop(10L, TripStopType.PICKUP, false); // مسافر قبلی هنوز سوار نشده
        TripStop newPickup = pickup(100L);
        TripStop newDropoff = dropoff(100L);

        List<TripStop> current = new ArrayList<>(List.of(existing));
        List<List<TripStop>> options =
                matchingService.enumerateInsertions(current, newPickup, newDropoff);

        // m=1 → C(3,2)=3 گزینه
        assertEquals(3, options.size());

        for (List<TripStop> option : options) {
            assertEquals(3, option.size());
            // ترتیب نسبیِ توقفِ موجود حفظ شده (اینجا فقط یک موجود هست)
            assertTrue(option.contains(existing));
            // PICKUP جدید قبل از DROPOFF جدید می‌آید
            int pickupIdx = indexOfType(option, TripStopType.PICKUP, 100L);
            int dropoffIdx = indexOfType(option, TripStopType.DROPOFF, 100L);
            assertTrue(pickupIdx < dropoffIdx, "PICKUP جدید باید قبل از DROPOFF جدید بیاید");
        }
    }

    /**
     * دو توقف قبلیِ انجام‌نشده: ترتیب نسبیِ آن‌ها نسبت به هم حفظ می‌شود (existing1
     * همیشه قبل از existing2)؛ فقط دو توقف جدید درج می‌شوند.
     * فرمول: m=2 → C(4,2)=6 گزینه
     */
    @Test
    void enumerate_whenTwoExistingIncompleteStops_generatesValidConstraints() {
        TripStop existing1 = stop(10L, TripStopType.PICKUP, false);
        TripStop existing2 = stop(20L, TripStopType.DROPOFF, false);
        TripStop newPickup = pickup(100L);
        TripStop newDropoff = dropoff(100L);

        List<TripStop> current = new ArrayList<>(List.of(existing1, existing2));
        List<List<TripStop>> options =
                matchingService.enumerateInsertions(current, newPickup, newDropoff);

        // m=2 → C(4,2)=6 گزینه
        assertEquals(6, options.size());

        for (List<TripStop> option : options) {
            assertEquals(4, option.size());
            // ترتیب نسبیِ توقف‌های موجود حفظ شده (existing1 قبل از existing2)
            assertTrue(indexOfRef(option, existing1) < indexOfRef(option, existing2),
                    "ترتیب نسبی توقف‌های موجود باید حفظ شود");
            // PICKUP جدید قبل از DROPOFF جدید می‌آید
            int pickupIdx = indexOfType(option, TripStopType.PICKUP, 100L);
            int dropoffIdx = indexOfType(option, TripStopType.DROPOFF, 100L);
            assertTrue(pickupIdx < dropoffIdx, "PICKUP جدید باید قبل از DROPOFF جدید بیاید");
        }
    }

    /**
     * یک توقف completed: جایگاهش نسبت به توقف‌های موجودِ همراه حفظ می‌شود، ولی
     * چون توقفِ جدید می‌تواند قبلش بیاید، index 0 لزوماً آن نیست.
     * فرمول: m=1 → C(3,2)=3 گزینه
     */
    @Test
    void enumerate_whenCompletedStopsExist_keepsOrderStable() {
        TripStop done = stop(10L, TripStopType.PICKUP, true); // انجام شده
        TripStop newPickup = pickup(100L);
        TripStop newDropoff = dropoff(100L);

        List<TripStop> current = new ArrayList<>(List.of(done));
        List<List<TripStop>> options =
                matchingService.enumerateInsertions(current, newPickup, newDropoff);

        // m=1 → C(3,2)=3 گزینه
        assertEquals(3, options.size());

        for (List<TripStop> option : options) {
            assertEquals(3, option.size());
            // توقفِ انجام‌شده هنوز همان‌جاست (فقط ترتیبِ نسبی، نه لزوماً index 0)
            assertTrue(option.contains(done));
            int pickupIdx = indexOfType(option, TripStopType.PICKUP, 100L);
            int dropoffIdx = indexOfType(option, TripStopType.DROPOFF, 100L);
            assertTrue(pickupIdx < dropoffIdx);
        }
    }

    /**
     * ترکیبِ توقف completed + توقف انجام‌نشده: ترتیب نسبیِ آن‌ها (done قبل از pending)
     * حفظ می‌شود؛ فقط دو توقف جدید درج می‌شوند.
     * فرمول: m=2 → C(4,2)=6 گزینه
     */
    @Test
    void enumerate_mixedCompletedAndPending_keepsOrderStable() {
        TripStop done = stop(10L, TripStopType.PICKUP, true);
        TripStop pending = stop(20L, TripStopType.DROPOFF, false);
        TripStop newPickup = pickup(100L);
        TripStop newDropoff = dropoff(100L);

        List<TripStop> current = new ArrayList<>(List.of(done, pending));
        List<List<TripStop>> options =
                matchingService.enumerateInsertions(current, newPickup, newDropoff);

        // m=2 → C(4,2)=6 گزینه
        assertEquals(6, options.size());

        for (List<TripStop> option : options) {
            assertEquals(4, option.size());
            // ترتیب نسبیِ توقف‌های موجود حفظ شده (done قبل از pending)
            assertTrue(indexOfRef(option, done) < indexOfRef(option, pending),
                    "ترتیب نسبی توقف‌های موجود باید حفظ شود");
            int pickupIdx = indexOfType(option, TripStopType.PICKUP, 100L);
            int dropoffIdx = indexOfType(option, TripStopType.DROPOFF, 100L);
            assertTrue(pickupIdx < dropoffIdx);
        }
    }

    // ---- کمکی ----

    private int indexOfType(List<TripStop> list, TripStopType type, Long passengerId) {
        for (int i = 0; i < list.size(); i++) {
            TripStop s = list.get(i);
            if (s.getType() == type && s.getPassengerId().equals(passengerId)) {
                return i;
            }
        }
        return -1;
    }

    /** اندیسِ اولین توقفی که با همین reference یکسان باشد */
    private int indexOfRef(List<TripStop> list, TripStop target) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == target) {
                return i;
            }
        }
        return -1;
    }
}

package com.learning.shop;

import com.learning.shop.document.ActiveSharedTrip;
import com.learning.shop.document.SharedTripStatus;
import com.learning.shop.document.TripStop;
import com.learning.shop.document.TripStopType;
import com.learning.shop.document.TripType;
import com.learning.shop.repository.ActiveSharedTripRepository;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * تست Repository سفرهای فعال تاکسی اشتراکی با MongoDB لوکال واقعی.
 * از پروفایل sharedtaxi-test (دیتابیس جداگانه sharedtaxidb_test) استفاده می‌کند.
 * Embedded Mongo (Flapdoodle) غیرفعال شده است.
 */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ActiveProfiles("sharedtaxi-test")
class ActiveSharedTripRepositoryTest {

    @Autowired
    private ActiveSharedTripRepository tripRepository;

    @AfterEach
    void cleanUp() {
        tripRepository.deleteAll();
    }

    private ActiveSharedTrip buildTrip(String id, long driverId, SharedTripStatus status, TripType tripType,
                                       int extraCapacity, List<TripStop> stops) {
        ActiveSharedTrip trip = new ActiveSharedTrip();
        trip.setId(id);
        trip.setDriverId(driverId);
        trip.setTripType(tripType);
        trip.setExtraCapacity(extraCapacity);
        trip.setOriginLat(35.70);
        trip.setOriginLng(51.35);
        trip.setFinalDestLat(35.83);
        trip.setFinalDestLng(50.99);
        trip.setCurrentLat(35.71);
        trip.setCurrentLng(51.36);
        trip.setStops(stops);
        trip.setStatus(status);
        trip.setCreatedAt(LocalDateTime.now());
        return trip;
    }

    private List<TripStop> buildStops() {
        // یک مسافر سوار (سفر فعال): PICKUP انجام شده، DROPOFF نشده
        TripStop pickup = new TripStop();
        pickup.setPassengerId(10L);
        pickup.setType(TripStopType.PICKUP);
        pickup.setLat(35.72);
        pickup.setLng(51.38);
        pickup.setSequenceOrder(1);
        pickup.setCompleted(true);

        TripStop dropoff = new TripStop();
        dropoff.setPassengerId(10L);
        dropoff.setType(TripStopType.DROPOFF);
        dropoff.setLat(35.80);
        dropoff.setLng(50.95);
        dropoff.setSequenceOrder(2);
        dropoff.setCompleted(false);

        return List.of(pickup, dropoff);
    }

    @Test
    void findByStatusAndTripType_filtersCorrectly() {
        tripRepository.save(buildTrip("t1", 1L, SharedTripStatus.ACTIVE, TripType.IN_CITY, 1, buildStops()));
        tripRepository.save(buildTrip("t2", 2L, SharedTripStatus.COMPLETED, TripType.IN_CITY, 1, buildStops()));
        tripRepository.save(buildTrip("t3", 3L, SharedTripStatus.ACTIVE, TripType.OUT_CITY, 1, buildStops()));

        List<ActiveSharedTrip> activeInCity = tripRepository
                .findByStatusAndTripType(SharedTripStatus.ACTIVE, TripType.IN_CITY);

        assertEquals(1, activeInCity.size());
        assertEquals("t1", activeInCity.get(0).getId());
    }

    @Test
    void findByStatus_filtersByStatusOnly() {
        tripRepository.save(buildTrip("t1", 1L, SharedTripStatus.ACTIVE, TripType.IN_CITY, 1, buildStops()));
        tripRepository.save(buildTrip("t2", 2L, SharedTripStatus.ACTIVE, TripType.IN_CITY, 1, buildStops()));
        tripRepository.save(buildTrip("t3", 3L, SharedTripStatus.CANCELLED, TripType.IN_CITY, 1, buildStops()));

        List<ActiveSharedTrip> activeOnly = tripRepository.findByStatus(SharedTripStatus.ACTIVE);

        assertEquals(2, activeOnly.size());
    }

    @Test
    void saveAndFindById_roundTrip_preservesEmbeddedStops() {
        List<TripStop> stops = buildStops();
        ActiveSharedTrip saved = tripRepository.save(
                buildTrip("t1", 1L, SharedTripStatus.ACTIVE, TripType.IN_CITY, 2, stops));

        var found = tripRepository.findById(saved.getId());
        assertTrue(found.isPresent());

        ActiveSharedTrip trip = found.get();
        assertEquals(1L, trip.getDriverId());
        assertEquals(TripType.IN_CITY, trip.getTripType());
        assertNotNull(trip.getStops());
        assertEquals(2, trip.getStops().size());
        assertEquals(2, trip.getExtraCapacity());
        // توقف‌های embedded باید ذخیره و بازیابی شوند
        assertEquals(TripStopType.PICKUP, trip.getStops().get(0).getType());
        assertEquals(true, trip.getStops().get(0).getCompleted());
        assertEquals(10L, trip.getStops().get(0).getPassengerId());
    }
}

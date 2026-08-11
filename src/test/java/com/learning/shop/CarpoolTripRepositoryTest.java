package com.learning.shop;

import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.TripStatus;
import com.learning.shop.document.TripType;
import com.learning.shop.repository.CarpoolTripRepository;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * تست Repository سفرهای هم‌پیمایی با MongoDB لوکال واقعی.
 * از پروفایل test (دیتابیس جداگانه carpooldb_test) استفاده می‌کند.
 * Embedded Mongo (Flapdoodle) غیرفعال شده است.
 */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ActiveProfiles("test")
class CarpoolTripRepositoryTest {

    @Autowired
    private CarpoolTripRepository tripRepository;

    @AfterEach
    void cleanUp() {
        tripRepository.deleteAll();
    }

    private CarpoolTrip buildTrip(String id, long userId, TripStatus status, TripType tripType) {
        CarpoolTrip trip = new CarpoolTrip();
        trip.setId(id);
        trip.setUserId(userId);
        trip.setOriginLat(35.70);
        trip.setOriginLng(51.35);
        trip.setOriginRadiusMeters(500);
        trip.setDestLat(35.83);
        trip.setDestLng(50.99);
        trip.setDestRadiusMeters(500);
        trip.setTripType(tripType);
        trip.setStatus(status);
        trip.setEarliestDepartureTime(LocalDateTime.of(2025, 1, 1, 18, 0));
        trip.setLatestDepartureTime(LocalDateTime.of(2025, 1, 1, 19, 0));
        trip.setCreatedAt(LocalDateTime.now());
        return trip;
    }

    @Test
    void findByStatusAndTripType_filtersCorrectly() {
        tripRepository.save(buildTrip("t1", 1L, TripStatus.ACTIVE, TripType.IN_CITY));
        tripRepository.save(buildTrip("t2", 2L, TripStatus.CANCELLED, TripType.IN_CITY));
        tripRepository.save(buildTrip("t3", 3L, TripStatus.ACTIVE, TripType.OUT_CITY));

        List<CarpoolTrip> activeInCity = tripRepository
                .findByStatusAndTripType(TripStatus.ACTIVE, TripType.IN_CITY);

        assertEquals(1, activeInCity.size());
        assertEquals("t1", activeInCity.get(0).getId());
    }

    @Test
    void saveAndFindById_roundTrip() {
        CarpoolTrip saved = tripRepository.save(buildTrip("t1", 1L, TripStatus.ACTIVE, TripType.IN_CITY));

        var found = tripRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
    }
}
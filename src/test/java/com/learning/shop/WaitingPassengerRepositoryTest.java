package com.learning.shop;

import com.learning.shop.document.TripType;
import com.learning.shop.document.WaitingPassenger;
import com.learning.shop.document.WaitingStatus;
import com.learning.shop.repository.WaitingPassengerRepository;
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
 * تست Repository مسافرهای منتظر تاکسی اشتراکی با MongoDB لوکال واقعی.
 * از پروفایل sharedtaxi-test (دیتابیس جداگانه sharedtaxidb_test) استفاده می‌کند.
 * Embedded Mongo (Flapdoodle) غیرفعال شده است.
 */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ActiveProfiles("sharedtaxi-test")
class WaitingPassengerRepositoryTest {

    @Autowired
    private WaitingPassengerRepository passengerRepository;

    @AfterEach
    void cleanUp() {
        passengerRepository.deleteAll();
    }

    private WaitingPassenger buildPassenger(String id, long passengerId, WaitingStatus status, TripType tripType) {
        WaitingPassenger p = new WaitingPassenger();
        p.setId(id);
        p.setPassengerId(passengerId);
        p.setTripType(tripType);
        p.setPickupLat(35.72);
        p.setPickupLng(51.39);
        p.setDropoffLat(35.82);
        p.setDropoffLng(50.97);
        p.setRequestedAt(LocalDateTime.of(2025, 1, 1, 18, 0));
        p.setStatus(status);
        return p;
    }

    @Test
    void findByStatus_returnsOnlyMatchingPassengers() {
        passengerRepository.save(buildPassenger("p1", 100L, WaitingStatus.WAITING, TripType.IN_CITY));
        passengerRepository.save(buildPassenger("p2", 101L, WaitingStatus.WAITING, TripType.IN_CITY));
        passengerRepository.save(buildPassenger("p3", 102L, WaitingStatus.MATCHED, TripType.IN_CITY));
        passengerRepository.save(buildPassenger("p4", 103L, WaitingStatus.CANCELLED, TripType.IN_CITY));

        List<WaitingPassenger> waiting = passengerRepository.findByStatus(WaitingStatus.WAITING);

        assertEquals(2, waiting.size());
        assertTrue(waiting.stream().allMatch(p -> p.getStatus() == WaitingStatus.WAITING));
    }

    @Test
    void saveAndFindById_roundTrip() {
        WaitingPassenger saved = passengerRepository.save(
                buildPassenger("p1", 100L, WaitingStatus.WAITING, TripType.IN_CITY));

        var found = passengerRepository.findById(saved.getId());
        assertTrue(found.isPresent());

        WaitingPassenger p = found.get();
        assertEquals(100L, p.getPassengerId());
        assertEquals(WaitingStatus.WAITING, p.getStatus());
        assertEquals(TripType.IN_CITY, p.getTripType());
        assertEquals(35.72, p.getPickupLat());
        assertEquals(50.97, p.getDropoffLng());
    }
}

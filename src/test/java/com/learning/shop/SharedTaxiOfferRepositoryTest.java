package com.learning.shop;

import com.learning.shop.document.OfferStatus;
import com.learning.shop.document.SharedTaxiOffer;
import com.learning.shop.document.TripStop;
import com.learning.shop.document.TripStopType;
import com.learning.shop.repository.SharedTaxiOfferRepository;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * تست Repository پیشنهادهای تاکسی اشتراکی با MongoDB لوکال واقعی.
 * از پروفایل sharedtaxi-test (دیتابیس جداگانه sharedtaxidb_test) استفاده می‌کند.
 * Embedded Mongo (Flapdoodle) غیرفعال شده است.
 */
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ActiveProfiles("sharedtaxi-test")
class SharedTaxiOfferRepositoryTest {

    @Autowired
    private SharedTaxiOfferRepository offerRepository;

    @AfterEach
    void cleanUp() {
        offerRepository.deleteAll();
    }

    private List<TripStop> buildProposedStops() {
        List<TripStop> stops = new ArrayList<>();

        TripStop p1 = new TripStop();
        p1.setPassengerId(10L);
        p1.setType(TripStopType.PICKUP);
        p1.setLat(35.72);
        p1.setLng(51.38);
        p1.setSequenceOrder(1);
        p1.setCompleted(true);

        TripStop d1 = new TripStop();
        d1.setPassengerId(10L);
        d1.setType(TripStopType.DROPOFF);
        d1.setLat(35.80);
        d1.setLng(50.95);
        d1.setSequenceOrder(2);
        d1.setCompleted(false);

        TripStop pickupNew = new TripStop();
        pickupNew.setPassengerId(100L);
        pickupNew.setType(TripStopType.PICKUP);
        pickupNew.setLat(35.75);
        pickupNew.setLng(51.20);
        pickupNew.setSequenceOrder(3);
        pickupNew.setCompleted(false);

        TripStop dropoffNew = new TripStop();
        dropoffNew.setPassengerId(100L);
        dropoffNew.setType(TripStopType.DROPOFF);
        dropoffNew.setLat(35.78);
        dropoffNew.setLng(50.90);
        dropoffNew.setSequenceOrder(4);
        dropoffNew.setCompleted(false);

        stops.add(p1);
        stops.add(d1);
        stops.add(pickupNew);
        stops.add(dropoffNew);
        return stops;
    }

    private SharedTaxiOffer buildOffer(String id, OfferStatus status, double extraDistance, double maxDetour) {
        SharedTaxiOffer offer = new SharedTaxiOffer();
        offer.setId(id);
        offer.setActiveTripId("trip-1");
        offer.setWaitingPassengerId("pass-100");
        offer.setProposedStops(buildProposedStops());
        offer.setExtraDistanceMetersForDriver(extraDistance);
        offer.setMaxDetourMetersForExistingPassengers(maxDetour);
        offer.setStatus(status);
        offer.setCreatedAt(LocalDateTime.now());
        return offer;
    }

    @Test
    void saveAndFindById_roundTrip_preservesProposedStops() {
        SharedTaxiOffer saved = offerRepository.save(
                buildOffer("offer-1", OfferStatus.PENDING, 2500.0, 800.0));

        var found = offerRepository.findById(saved.getId());
        assertTrue(found.isPresent());

        SharedTaxiOffer offer = found.get();
        assertEquals("trip-1", offer.getActiveTripId());
        assertEquals("pass-100", offer.getWaitingPassengerId());
        assertEquals(OfferStatus.PENDING, offer.getStatus());
        assertEquals(2500.0, offer.getExtraDistanceMetersForDriver());
        assertEquals(800.0, offer.getMaxDetourMetersForExistingPassengers());

        // لیست توقف‌های پیشنهادی embedded باید ذخیره و بازیابی شود
        assertEquals(4, offer.getProposedStops().size());
        assertEquals(TripStopType.PICKUP, offer.getProposedStops().get(0).getType());
        assertEquals(100L, offer.getProposedStops().get(2).getPassengerId());
        assertEquals(true, offer.getProposedStops().get(0).getCompleted());
    }

    @Test
    void saveAndFindById_offerWithoutNumericFields() {
        SharedTaxiOffer saved = offerRepository.save(buildOffer("offer-2", OfferStatus.REJECTED, 0.0, 0.0));

        var found = offerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(OfferStatus.REJECTED, found.get().getStatus());
        assertEquals(0.0, found.get().getExtraDistanceMetersForDriver());
    }
}

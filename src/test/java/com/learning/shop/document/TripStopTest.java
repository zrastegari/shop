package com.learning.shop.document;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TripStopTest {

    @Test
    void newTripStop_hasNonNullId() {
        TripStop stop = new TripStop();
        assertNotNull(stop.getId());
    }

    @Test
    void twoNewTripStops_haveDifferentIds() {
        TripStop stop1 = new TripStop();
        TripStop stop2 = new TripStop();
        assertNotEquals(stop1.getId(), stop2.getId());
    }

    @Test
    void copy_preservesOriginalId() {
        TripStop original = new TripStop();
        original.setPassengerId(1L);
        original.setType(TripStopType.PICKUP);
        original.setLat(35.7);
        original.setLng(51.4);
        original.setSequenceOrder(1);
        original.setCompleted(false);

        String originalId = original.getId();
        TripStop copied = original.copy();

        assertEquals(originalId, copied.getId());
    }

    @Test
    void copy_doesNotGenerateNewUuidOnMultipleCopies() {
        TripStop original = new TripStop();
        TripStop copy1 = original.copy();
        TripStop copy2 = original.copy();

        // هر دو کپی باید همون id اصلی رو داشته باشن، نه id متفاوت از هم
        assertEquals(copy1.getId(), copy2.getId());
        assertEquals(original.getId(), copy1.getId());
    }
}
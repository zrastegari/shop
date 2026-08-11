package com.learning.shop.dto.sharedtaxi;

/**
 * پاسخ ثبت یک سفر فعال جدید (Driver-initiated).
 * شامل سفر ثبت‌شده و — اگر پیدا شد — بهترین offer برای مسافری منتظر.
 */
public class CreateActiveTripResponse {

    private ActiveTripResponse trip;

    /** بهترین offer پیدا شده برای این سفر (اگر هیچ نبود null) */
    private OfferResponse offer;

    public CreateActiveTripResponse(ActiveTripResponse trip, OfferResponse offer) {
        this.trip = trip;
        this.offer = offer;
    }

    // ---- getter / setter ----

    public ActiveTripResponse getTrip() {
        return trip;
    }

    public void setTrip(ActiveTripResponse trip) {
        this.trip = trip;
    }

    public OfferResponse getOffer() {
        return offer;
    }

    public void setOffer(OfferResponse offer) {
        this.offer = offer;
    }
}

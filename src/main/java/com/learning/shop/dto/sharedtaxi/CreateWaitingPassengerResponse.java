package com.learning.shop.dto.sharedtaxi;

/**
 * پاسخ ثبت یک مسافر منتظر جدید (Passenger-initiated).
 * شامل مسافر ثبت‌شده و — اگر پیدا شد — بهترین offer برای او.
 */
public class CreateWaitingPassengerResponse {

    private WaitingPassengerResponse passenger;

    /** بهترین offer پیدا شده برای این مسافر (اگر هیچ نبود null) */
    private OfferResponse offer;

    public CreateWaitingPassengerResponse(WaitingPassengerResponse passenger, OfferResponse offer) {
        this.passenger = passenger;
        this.offer = offer;
    }

    // ---- getter / setter ----

    public WaitingPassengerResponse getPassenger() {
        return passenger;
    }

    public void setPassenger(WaitingPassengerResponse passenger) {
        this.passenger = passenger;
    }

    public OfferResponse getOffer() {
        return offer;
    }

    public void setOffer(OfferResponse offer) {
        this.offer = offer;
    }
}

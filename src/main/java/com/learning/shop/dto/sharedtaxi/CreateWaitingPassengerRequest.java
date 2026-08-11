package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.TripType;
import jakarta.validation.constraints.NotNull;

/**
 * درخواست ثبت یک مسافر منتظر تاکسی اشتراکی.
 */
public class CreateWaitingPassengerRequest {

    @NotNull(message = "passengerId نمی‌تواند خالی باشد")
    private Long passengerId;

    @NotNull(message = "tripType ضروری است")
    private TripType tripType;

    @NotNull(message = "pickupLat ضروری است")
    private Double pickupLat;

    @NotNull(message = "pickupLng ضروری است")
    private Double pickupLng;

    @NotNull(message = "dropoffLat ضروری است")
    private Double dropoffLat;

    @NotNull(message = "dropoffLng ضروری است")
    private Double dropoffLng;

    // ---- getter / setter ----

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public Double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(Double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public Double getDropoffLng() {
        return dropoffLng;
    }

    public void setDropoffLng(Double dropoffLng) {
        this.dropoffLng = dropoffLng;
    }
}

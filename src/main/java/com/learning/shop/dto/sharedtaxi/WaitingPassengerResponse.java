package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.TripType;
import com.learning.shop.document.WaitingPassenger;
import com.learning.shop.document.WaitingStatus;

import java.time.LocalDateTime;

/**
 * نمایش یک مسافر منتظر تاکسی اشتراکی — تا Document مستقیم expose نشود.
 */
public class WaitingPassengerResponse {

    private String id;
    private Long passengerId;
    private TripType tripType;
    private Double pickupLat;
    private Double pickupLng;
    private Double dropoffLat;
    private Double dropoffLng;
    private LocalDateTime requestedAt;
    private WaitingStatus status;

    /** سازنده‌ی استاتیک برای تبدیل از Document به Response */
    public static WaitingPassengerResponse from(WaitingPassenger passenger) {
        WaitingPassengerResponse r = new WaitingPassengerResponse();
        r.id = passenger.getId();
        r.passengerId = passenger.getPassengerId();
        r.tripType = passenger.getTripType();
        r.pickupLat = passenger.getPickupLat();
        r.pickupLng = passenger.getPickupLng();
        r.dropoffLat = passenger.getDropoffLat();
        r.dropoffLng = passenger.getDropoffLng();
        r.requestedAt = passenger.getRequestedAt();
        r.status = passenger.getStatus();
        return r;
    }

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public WaitingStatus getStatus() {
        return status;
    }

    public void setStatus(WaitingStatus status) {
        this.status = status;
    }
}

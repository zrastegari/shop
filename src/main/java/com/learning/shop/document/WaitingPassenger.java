package com.learning.shop.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * مدل یک مسافر منتظر تاکسی اشتراکی.
 * <p>
 * این سند نمایانگر مسافری است که درخواست تاکسی اشتراکی ثبت کرده و هنوز
 * به هیچ سفر فعالی وصل نشده است (وضعیت WAITING).
 */
@Document(collection = "waiting_passengers")
public class WaitingPassenger {

    @Id
    private String id;

    /** شناسه مسافر */
    private Long passengerId;

    /** نوع سفر (IN_CITY / OUT_CITY) */
    private TripType tripType;

    // ---- نقطه‌ی سوار شدن ----
    private Double pickupLat;
    private Double pickupLng;

    // ---- نقطه‌ی پیاده شدن ----
    private Double dropoffLat;
    private Double dropoffLng;

    /** زمان ثبت درخواست */
    private LocalDateTime requestedAt;

    /** وضعیت مسافر */
    private WaitingStatus status;

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

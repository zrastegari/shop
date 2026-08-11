package com.learning.shop.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * مدل یک پیشنهاد تاکسی اشتراکی (SharedTaxiOffer).
 * <p>
 * این سند، پیشنهادِ «سوار کردن یک مسافر منتظر به یک سفر فعال» را نگه می‌دارد.
 * سیستم فقط پیشنهاد می‌دهد (status=PENDING) و به‌صورت خودکار قبول نمی‌کند.
 */
@Document(collection = "shared_taxi_offers")
public class SharedTaxiOffer {

    @Id
    private String id;

    /** اشاره به سند ActiveSharedTrip */
    private String activeTripId;

    /** اشاره به سند WaitingPassenger */
    private String waitingPassengerId;

    /** ترتیب جدید کامل توقف‌ها (توقف‌های قبلی + دو توقف جدید مسافر) */
    private List<TripStop> proposedStops = new ArrayList<>();

    /** مسافت اضافه شده به مسیر کل راننده (متر) */
    private Double extraDistanceMetersForDriver;

    /** بیشترین انحراف تحمیل‌شده به مسافر(های) فعلی سوار (متر) — اگر کسی سوار نیست صفر */
    private Double maxDetourMetersForExistingPassengers;

    /** وضعیت پیشنهاد */
    private OfferStatus status;

    /** زمان ساخت پیشنهاد */
    private LocalDateTime createdAt;

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveTripId() {
        return activeTripId;
    }

    public void setActiveTripId(String activeTripId) {
        this.activeTripId = activeTripId;
    }

    public String getWaitingPassengerId() {
        return waitingPassengerId;
    }

    public void setWaitingPassengerId(String waitingPassengerId) {
        this.waitingPassengerId = waitingPassengerId;
    }

    public List<TripStop> getProposedStops() {
        return proposedStops;
    }

    public void setProposedStops(List<TripStop> proposedStops) {
        this.proposedStops = proposedStops;
    }

    public Double getExtraDistanceMetersForDriver() {
        return extraDistanceMetersForDriver;
    }

    public void setExtraDistanceMetersForDriver(Double extraDistanceMetersForDriver) {
        this.extraDistanceMetersForDriver = extraDistanceMetersForDriver;
    }

    public Double getMaxDetourMetersForExistingPassengers() {
        return maxDetourMetersForExistingPassengers;
    }

    public void setMaxDetourMetersForExistingPassengers(Double maxDetourMetersForExistingPassengers) {
        this.maxDetourMetersForExistingPassengers = maxDetourMetersForExistingPassengers;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

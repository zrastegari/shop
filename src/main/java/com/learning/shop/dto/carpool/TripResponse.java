package com.learning.shop.dto.carpool;

import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.TripStatus;
import com.learning.shop.document.TripType;

import java.time.LocalDateTime;

/**
 * پاسخ نمایش یک سفر هم‌پیمایی.
 * <p>
 * از روی Document ساخته می‌شود تا سند مستقیم expose نشود.
 */
public class TripResponse {

    private String id;
    private Long userId;
    private Double originLat;
    private Double originLng;
    private Integer originRadiusMeters;
    private Double destLat;
    private Double destLng;
    private Integer destRadiusMeters;
    private TripType tripType;
    private LocalDateTime earliestDepartureTime;
    private LocalDateTime latestDepartureTime;
    private TripStatus status;
    private LocalDateTime createdAt;

    /** سازنده‌ی استاتیک برای تبدیل از Document به Response */
    public static TripResponse from(CarpoolTrip trip) {
        TripResponse r = new TripResponse();
        r.id = trip.getId();
        r.userId = trip.getUserId();
        r.originLat = trip.getOriginLat();
        r.originLng = trip.getOriginLng();
        r.originRadiusMeters = trip.getOriginRadiusMeters();
        r.destLat = trip.getDestLat();
        r.destLng = trip.getDestLng();
        r.destRadiusMeters = trip.getDestRadiusMeters();
        r.tripType = trip.getTripType();
        r.earliestDepartureTime = trip.getEarliestDepartureTime();
        r.latestDepartureTime = trip.getLatestDepartureTime();
        r.status = trip.getStatus();
        r.createdAt = trip.getCreatedAt();
        return r;
    }

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(Double originLng) {
        this.originLng = originLng;
    }

    public Integer getOriginRadiusMeters() {
        return originRadiusMeters;
    }

    public void setOriginRadiusMeters(Integer originRadiusMeters) {
        this.originRadiusMeters = originRadiusMeters;
    }

    public Double getDestLat() {
        return destLat;
    }

    public void setDestLat(Double destLat) {
        this.destLat = destLat;
    }

    public Double getDestLng() {
        return destLng;
    }

    public void setDestLng(Double destLng) {
        this.destLng = destLng;
    }

    public Integer getDestRadiusMeters() {
        return destRadiusMeters;
    }

    public void setDestRadiusMeters(Integer destRadiusMeters) {
        this.destRadiusMeters = destRadiusMeters;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public LocalDateTime getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public void setEarliestDepartureTime(LocalDateTime earliestDepartureTime) {
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public LocalDateTime getLatestDepartureTime() {
        return latestDepartureTime;
    }

    public void setLatestDepartureTime(LocalDateTime latestDepartureTime) {
        this.latestDepartureTime = latestDepartureTime;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.learning.shop.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * مدل یک سفر هم‌پیمایی که در MongoDB ذخیره می‌شود.
 * <p>
 * برخلاف JPA که از {@code @Entity} و کلید Long استفاده می‌کند،
 * در دنیای MongoDB این کلاس‌ها <b>Document</b> نامیده می‌شوند و
 * شناسه (id) معمولاً یک رشته (String) است — چون Mongo به‌صورت خودکار
 * یک ObjectId رشته‌ای برای هر سند تولید می‌کند.
 * <p>
 * مبدأ و مقصد به‌صورت «نقطه + شعاع» ثبت می‌شوند (نه یک نقطه دقیق).
 */
@Document(collection = "carpool_trips")
public class CarpoolTrip {

    @Id
    private String id;

    /** شناسه کاربر (راننده یا مسافر) که این سفر را ثبت کرده است */
    private Long userId;

    // ---- مبدأ (نقطه + شعاع) ----
    private Double originLat;
    private Double originLng;
    private Integer originRadiusMeters;

    // ---- مقصد (نقطه + شعاع) ----
    private Double destLat;
    private Double destLng;
    private Integer destRadiusMeters;

    /** نوع سفر: داخل‌شهری یا برون‌شهری */
    private TripType tripType;

    /** آغاز و پایان بازه‌ی زمانی مطلوب سفر */
    private LocalDateTime earliestDepartureTime;
    private LocalDateTime latestDepartureTime;

    /** وضعیت فعلی سفر */
    private TripStatus status;

    /** زمان ثبت سفر */
    private LocalDateTime createdAt;

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

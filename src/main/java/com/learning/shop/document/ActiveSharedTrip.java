package com.learning.shop.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * مدل یک سفر فعال تاکسی اشتراکی.
 * <p>
 * برخلاف JPA که از {@code @Entity} و کلید Long استفاده می‌کند، این کلاس یک
 * <b>Document</b> در MongoDB است (پکیج {@code document} و {@code @Document}).
 * شناسه به‌صورت خودکار یک ObjectId رشته‌ای (String) است.
 * <p>
 * ظرفیت اضافه (extraCapacity) را راننده هنگام شروع سفر اعلام می‌کند و ظرفیت
 * باقی‌مانده همیشه <b>محاسبه‌شده</b> است (از روی توقف‌های completed)، نه یک
 * فیلد دستیِ جدا.
 */
@Document(collection = "active_shared_trips")
public class ActiveSharedTrip {

    @Id
    private String id;

    /** شناسه راننده */
    private Long driverId;

    /** نوع سفر (IN_CITY / OUT_CITY) — از enum موجود فاز ۱ استفاده می‌شود */
    private TripType tripType;

    /** ظرفیت اضافه‌ای که راننده هنگام شروع سفر اعلام کرده (۱ یا ۲ نفر) */
    private Integer extraCapacity;

    // ---- مبدأ و مقصد اولیه سفر راننده ----
    private Double originLat;
    private Double originLng;
    private Double finalDestLat;
    private Double finalDestLng;

    // ---- موقعیت فعلی راننده (یک API دیگر این را آپدیت می‌کند؛ فعلاً فقط فیلد ساخته می‌شود) ----
    private Double currentLat;
    private Double currentLng;

    /** ترتیب فعلی توقف‌های سفر (embedded، نه Document جدا) */
    private List<TripStop> stops = new ArrayList<>();

    /** وضعیت سفر */
    private SharedTripStatus status;

    /** زمان ثبت/شروع سفر */
    private LocalDateTime createdAt;

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public Integer getExtraCapacity() {
        return extraCapacity;
    }

    public void setExtraCapacity(Integer extraCapacity) {
        this.extraCapacity = extraCapacity;
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

    public Double getFinalDestLat() {
        return finalDestLat;
    }

    public void setFinalDestLat(Double finalDestLat) {
        this.finalDestLat = finalDestLat;
    }

    public Double getFinalDestLng() {
        return finalDestLng;
    }

    public void setFinalDestLng(Double finalDestLng) {
        this.finalDestLng = finalDestLng;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }

    public List<TripStop> getStops() {
        return stops;
    }

    public void setStops(List<TripStop> stops) {
        this.stops = stops;
    }

    public SharedTripStatus getStatus() {
        return status;
    }

    public void setStatus(SharedTripStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

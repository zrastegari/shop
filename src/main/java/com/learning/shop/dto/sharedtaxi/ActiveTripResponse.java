package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.ActiveSharedTrip;
import com.learning.shop.document.SharedTripStatus;
import com.learning.shop.document.TripType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * نمایش یک سفر فعال تاکسی اشتراکی — تا Document مستقیم expose نشود.
 */
public class ActiveTripResponse {

    private String id;
    private Long driverId;
    private TripType tripType;
    private Integer extraCapacity;
    private Double originLat;
    private Double originLng;
    private Double finalDestLat;
    private Double finalDestLng;
    private Double currentLat;
    private Double currentLng;
    private SharedTripStatus status;
    private LocalDateTime createdAt;
    private List<TripStopResponse> stops = new ArrayList<>();

    /** سازنده‌ی استاتیک برای تبدیل از Document به Response */
    public static ActiveTripResponse from(ActiveSharedTrip trip) {
        ActiveTripResponse r = new ActiveTripResponse();
        r.id = trip.getId();
        r.driverId = trip.getDriverId();
        r.tripType = trip.getTripType();
        r.extraCapacity = trip.getExtraCapacity();
        r.originLat = trip.getOriginLat();
        r.originLng = trip.getOriginLng();
        r.finalDestLat = trip.getFinalDestLat();
        r.finalDestLng = trip.getFinalDestLng();
        r.currentLat = trip.getCurrentLat();
        r.currentLng = trip.getCurrentLng();
        r.status = trip.getStatus();
        r.createdAt = trip.getCreatedAt();
        if (trip.getStops() != null) {
            r.stops = trip.getStops().stream()
                    .map(TripStopResponse::from)
                    .collect(Collectors.toList());
        }
        return r;
    }

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

    public List<TripStopResponse> getStops() {
        return stops;
    }

    public void setStops(List<TripStopResponse> stops) {
        this.stops = stops;
    }
}

package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.TripType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * درخواست ثبت یک سفر فعال تاکسی اشتراکی توسط راننده.
 */
public class CreateActiveTripRequest {

    @NotNull(message = "driverId نمی‌تواند خالی باشد")
    private Long driverId;

    @NotNull(message = "tripType ضروری است")
    private TripType tripType;

    @NotNull(message = "extraCapacity ضروری است")
    @Min(value = 0, message = "ظرفیت اضافه نمی‌تواند منفی باشد")
    private Integer extraCapacity;

    @NotNull(message = "originLat ضروری است")
    private Double originLat;

    @NotNull(message = "originLng ضروری است")
    private Double originLng;

    @NotNull(message = "finalDestLat ضروری است")
    private Double finalDestLat;

    @NotNull(message = "finalDestLng ضروری است")
    private Double finalDestLng;

    // ---- getter / setter ----

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
}

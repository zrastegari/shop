package com.learning.shop.dto.carpool;

import com.learning.shop.document.TripType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * درخواست ثبت یک سفر هم‌پیمایی جدید.
 * <p>
 * اعتبارسنجی (@NotNull, @Min و ...) دقیقاً روی همین DTO انجام می‌شود
 * و Document ها مستقیم expose نمی‌شوند.
 */
public class CreateTripRequest {

    @NotNull(message = "userId نمی‌تواند خالی باشد")
    private Long userId;

    @NotNull(message = "originLat ضروری است")
    private Double originLat;

    @NotNull(message = "originLng ضروری است")
    private Double originLng;

    @NotNull(message = "originRadiusMeters ضروری است")
    @Min(value = 0, message = "شعاع مبدأ نمی‌تواند منفی باشد")
    private Integer originRadiusMeters;

    @NotNull(message = "destLat ضروری است")
    private Double destLat;

    @NotNull(message = "destLng ضروری است")
    private Double destLng;

    @NotNull(message = "destRadiusMeters ضروری است")
    @Min(value = 0, message = "شعاع مقصد نمی‌تواند منفی باشد")
    private Integer destRadiusMeters;

    @NotNull(message = "tripType ضروری است")
    private TripType tripType;

    @NotNull(message = "earliestDepartureTime ضروری است")
    private LocalDateTime earliestDepartureTime;

    @NotNull(message = "latestDepartureTime ضروری است")
    private LocalDateTime latestDepartureTime;

    // ---- getter / setter ----

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
}

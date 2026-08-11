package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.TripStop;
import com.learning.shop.document.TripStopType;

/**
 * نمایش یک توقف در پاسخ — تا Document مستقیم expose نشود.
 */
public class TripStopResponse {

    private Long passengerId;
    private TripStopType type;
    private Double lat;
    private Double lng;
    private Integer sequenceOrder;
    private Boolean completed;

    /** سازنده‌ی استاتیک برای تبدیل از TripStop به Response */
    public static TripStopResponse from(TripStop stop) {
        TripStopResponse r = new TripStopResponse();
        r.passengerId = stop.getPassengerId();
        r.type = stop.getType();
        r.lat = stop.getLat();
        r.lng = stop.getLng();
        r.sequenceOrder = stop.getSequenceOrder();
        r.completed = stop.getCompleted();
        return r;
    }

    // ---- getter / setter ----

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public TripStopType getType() {
        return type;
    }

    public void setType(TripStopType type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}

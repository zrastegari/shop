package com.learning.shop.document;

/**
 * یک توقف در مسیر تاکسی اشتراکی.
 * <p>
 * این یک کلاس توکار (embedded) است — نه یک Document جدا. یعنی داخل
 * سند {@link ActiveSharedTrip} به‌صورت یک زیرمجموعه (Subdocument) ذخیره می‌شود.
 * یک مسافر دو توقف دارد: یکی {type=PICKUP} و یکی {type=DROPOFF}.
 */
public class TripStop {

    /** شناسه‌ی یکتای این توقف (برای پیدا کردنش در endpoint تکمیل توقف) */
    private String id = java.util.UUID.randomUUID().toString();

    /** شناسه مسافر مربوط به این توقف */
    private Long passengerId;

    /** نوع توقف: سوار شدن یا پیاده شدن */
    private TripStopType type;

    /** عرض جغرافیایی توقف */
    private Double lat;

    /** طول جغرافیایی توقف */
    private Double lng;

    /** ترتیب توقف در مسیر (از ۱ شروع می‌شود) */
    private Integer sequenceOrder;

    /** آیا این توقف قبلاً انجام شده است؟ */
    private Boolean completed;

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

    /** یک کپی عمیق (deep copy) از این توقف برمی‌گرداند — برای enumeration استفاده می‌شود */
    public TripStop copy() {
        TripStop copy = new TripStop();
        copy.setId(this.id); // مهم: id قبلی حفظ می‌شود، UUID جدید تولید نمی‌شود
        copy.setPassengerId(this.passengerId);
        copy.setType(this.type);
        copy.setLat(this.lat);
        copy.setLng(this.lng);
        copy.setSequenceOrder(this.sequenceOrder);
        copy.setCompleted(this.completed);
        return copy;
    }
}
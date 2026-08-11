package com.learning.shop.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * مدل یک پیشنهاد تطابق هم‌پیمایی (Match) بین دو سفر.
 * <p>
 * این سند دو سفر (CarpoolTrip) را به هم معرفی می‌کند — یادمان باشد
 * سیستم فقط «پیشنهاد/معرفی» می‌دهد و به‌صورت خودکار دو طرف را الزام نمی‌کند.
 * <p>
 * tripOneId و tripTwoId به id رشته‌ای اسناد CarpoolTrip اشاره می‌کنند
 * (در Mongo شناسه‌ها رشته هستند، پس این‌ها هم String هستند).
 */
@Document(collection = "carpool_matches")
public class CarpoolMatch {

    @Id
    private String id;

    /** شناسه سفر اول */
    private String tripOneId;

    /** شناسه سفر دوم */
    private String tripTwoId;

    /** زمان ثبت/match شدن */
    private LocalDateTime matchedAt;

    /** مسافت واقعی جاده‌ای محاسبه‌شده (متر) — می‌تواند null باشد */
    private Double distanceScoreMeters;

    /** وضعیت پیشنهاد */
    private MatchStatus status;

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripOneId() {
        return tripOneId;
    }

    public void setTripOneId(String tripOneId) {
        this.tripOneId = tripOneId;
    }

    public String getTripTwoId() {
        return tripTwoId;
    }

    public void setTripTwoId(String tripTwoId) {
        this.tripTwoId = tripTwoId;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }

    public Double getDistanceScoreMeters() {
        return distanceScoreMeters;
    }

    public void setDistanceScoreMeters(Double distanceScoreMeters) {
        this.distanceScoreMeters = distanceScoreMeters;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }
}

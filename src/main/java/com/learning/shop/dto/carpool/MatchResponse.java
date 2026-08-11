package com.learning.shop.dto.carpool;

import com.learning.shop.document.CarpoolMatch;
import com.learning.shop.document.MatchStatus;

import java.time.LocalDateTime;

/**
 * پاسخ نمایش یک پیشنهاد تطابق هم‌پیمایی.
 */
public class MatchResponse {

    private String id;
    private String tripOneId;
    private String tripTwoId;
    private LocalDateTime matchedAt;
    private Double distanceScoreMeters;
    private MatchStatus status;

    /** سازنده‌ی استاتیک برای تبدیل از Document به Response */
    public static MatchResponse from(CarpoolMatch match) {
        MatchResponse r = new MatchResponse();
        r.id = match.getId();
        r.tripOneId = match.getTripOneId();
        r.tripTwoId = match.getTripTwoId();
        r.matchedAt = match.getMatchedAt();
        r.distanceScoreMeters = match.getDistanceScoreMeters();
        r.status = match.getStatus();
        return r;
    }

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

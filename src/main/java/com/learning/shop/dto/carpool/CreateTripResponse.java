package com.learning.shop.dto.carpool;

import java.util.List;

/**
 * پاسخ ثبت یک سفر جدید.
 * شامل سفر ثبت‌شده و فهرست match های پیدا شده در همان لحظه.
 */
public class CreateTripResponse {

    private TripResponse trip;

    private List<MatchResponse> matches;

    public CreateTripResponse(TripResponse trip, List<MatchResponse> matches) {
        this.trip = trip;
        this.matches = matches;
    }

    // ---- getter / setter ----

    public TripResponse getTrip() {
        return trip;
    }

    public void setTrip(TripResponse trip) {
        this.trip = trip;
    }

    public List<MatchResponse> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchResponse> matches) {
        this.matches = matches;
    }
}

package com.learning.shop.dto.sharedtaxi;

import com.learning.shop.document.OfferStatus;
import com.learning.shop.document.SharedTaxiOffer;
import com.learning.shop.document.TripStop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * نمایش یک پیشنهاد تاکسی اشتراکی — تا Document مستقیم expose نشود.
 */
public class OfferResponse {

    private String id;
    private String activeTripId;
    private String waitingPassengerId;
    private List<TripStopResponse> proposedStops = new ArrayList<>();
    private Double extraDistanceMetersForDriver;
    private Double maxDetourMetersForExistingPassengers;
    private OfferStatus status;
    private LocalDateTime createdAt;

    /** سازنده‌ی استاتیک برای تبدیل از Document به Response */
    public static OfferResponse from(SharedTaxiOffer offer) {
        OfferResponse r = new OfferResponse();
        r.id = offer.getId();
        r.activeTripId = offer.getActiveTripId();
        r.waitingPassengerId = offer.getWaitingPassengerId();
        if (offer.getProposedStops() != null) {
            r.proposedStops = offer.getProposedStops().stream()
                    .map(TripStopResponse::from)
                    .collect(Collectors.toList());
        }
        r.extraDistanceMetersForDriver = offer.getExtraDistanceMetersForDriver();
        r.maxDetourMetersForExistingPassengers = offer.getMaxDetourMetersForExistingPassengers();
        r.status = offer.getStatus();
        r.createdAt = offer.getCreatedAt();
        return r;
    }

    // ---- getter / setter ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveTripId() {
        return activeTripId;
    }

    public void setActiveTripId(String activeTripId) {
        this.activeTripId = activeTripId;
    }

    public String getWaitingPassengerId() {
        return waitingPassengerId;
    }

    public void setWaitingPassengerId(String waitingPassengerId) {
        this.waitingPassengerId = waitingPassengerId;
    }

    public List<TripStopResponse> getProposedStops() {
        return proposedStops;
    }

    public void setProposedStops(List<TripStopResponse> proposedStops) {
        this.proposedStops = proposedStops;
    }

    public Double getExtraDistanceMetersForDriver() {
        return extraDistanceMetersForDriver;
    }

    public void setExtraDistanceMetersForDriver(Double extraDistanceMetersForDriver) {
        this.extraDistanceMetersForDriver = extraDistanceMetersForDriver;
    }

    public Double getMaxDetourMetersForExistingPassengers() {
        return maxDetourMetersForExistingPassengers;
    }

    public void setMaxDetourMetersForExistingPassengers(Double maxDetourMetersForExistingPassengers) {
        this.maxDetourMetersForExistingPassengers = maxDetourMetersForExistingPassengers;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

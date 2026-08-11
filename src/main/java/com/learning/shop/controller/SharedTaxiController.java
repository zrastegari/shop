package com.learning.shop.controller;

import com.learning.shop.document.ActiveSharedTrip;
import com.learning.shop.document.SharedTaxiOffer;
import com.learning.shop.document.WaitingPassenger;
import com.learning.shop.dto.sharedtaxi.*;
import com.learning.shop.service.SharedTaxiMatchingService;
import com.learning.shop.service.SharedTaxiTripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * کنترلر REST سرویس تاکسی اشتراکی (Shared Taxi).
 * <p>
 * نقطه ورود عملیات: ثبت سفر فعال، ثبت مسافر منتظر، بازیابی، پذیرش/رد offer،
 * و تکمیل توقف. Document ها مستقیم expose نمی‌شوند — همه‌چیز از طریق DTO.
 */
@RestController
@RequestMapping("/api/sharedtaxi")
public class SharedTaxiController {

    private final SharedTaxiTripService tripService;
    private final SharedTaxiMatchingService matchingService;

    public SharedTaxiController(SharedTaxiTripService tripService,
                                SharedTaxiMatchingService matchingService) {
        this.tripService = tripService;
        this.matchingService = matchingService;
    }

    // ======================================================================
    //  Driver-initiated: ثبت سفر فعال + جستجوی فوری بین مسافرهای منتظر
    // ======================================================================

    /**
     * POST /api/sharedtaxi/active-trips
     * راننده سفر فعال جدید ثبت می‌کند؛ بلافاصله بین مسافرهای منتظر (WAITING)
     * بهترین کاندیدا جستجو می‌شود و offer (اگر هست) در پاسخ برمی‌گردد.
     */
    @PostMapping("/active-trips")
    public ResponseEntity<CreateActiveTripResponse> createActiveTrip(
            @Valid @RequestBody CreateActiveTripRequest request) {

        ActiveSharedTrip savedTrip = tripService.createActiveTrip(request);

        // Driver-initiated: بین مسافرهای منتظر بگرد (همان تابع تصمیم مرکزی)
        SharedTaxiOffer bestOffer = null;
        double bestExtra = Double.MAX_VALUE;
        for (WaitingPassenger p : tripService.findAllWaitingPassengers()) {
            SharedTaxiOffer offer = matchingService.findBestOffer(savedTrip, p);
            if (offer != null && offer.getExtraDistanceMetersForDriver() < bestExtra) {
                bestOffer = offer;
                bestExtra = offer.getExtraDistanceMetersForDriver();
            }
        }

        CreateActiveTripResponse response = new CreateActiveTripResponse(
                ActiveTripResponse.from(savedTrip),
                bestOffer == null ? null : OfferResponse.from(bestOffer));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ======================================================================
    //  Passenger-initiated: ثبت مسافر منتظر + جستجوی فوری بین سفرهای فعال
    // ======================================================================

    /**
     * POST /api/sharedtaxi/waiting-passengers
     * مسافر درخواست جدید ثبت می‌کند؛ بلافاصله بین سفرهای فعال (ACTIVE با ظرفیت
     * خالی) بهترین گزینه جستجو می‌شود و offer (اگر هست) در پاسخ برمی‌گردد.
     */
    @PostMapping("/waiting-passengers")
    public ResponseEntity<CreateWaitingPassengerResponse> createWaitingPassenger(
            @Valid @RequestBody CreateWaitingPassengerRequest request) {

        WaitingPassenger savedPassenger = tripService.createWaitingPassenger(request);

        // Passenger-initiated: بین سفرهای فعال بگرد (همان تابع تصمیم مرکزی)
        SharedTaxiOffer bestOffer = null;
        double bestExtra = Double.MAX_VALUE;
        for (ActiveSharedTrip trip : tripService.findAllActiveTrips()) {
            SharedTaxiOffer offer = matchingService.findBestOffer(trip, savedPassenger);
            if (offer != null && offer.getExtraDistanceMetersForDriver() < bestExtra) {
                bestOffer = offer;
                bestExtra = offer.getExtraDistanceMetersForDriver();
            }
        }

        CreateWaitingPassengerResponse response = new CreateWaitingPassengerResponse(
                WaitingPassengerResponse.from(savedPassenger),
                bestOffer == null ? null : OfferResponse.from(bestOffer));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ======================================================================
    //  عملیات بازیابی و تغییر وضعیت
    // ======================================================================

    /** GET /api/sharedtaxi/active-trips/{id} */
    @GetMapping("/active-trips/{id}")
    public ResponseEntity<ActiveTripResponse> getActiveTrip(@PathVariable String id) {
        ActiveSharedTrip trip = tripService.getActiveTrip(id);
        return ResponseEntity.ok(ActiveTripResponse.from(trip));
    }

    /** GET /api/sharedtaxi/offers/{id} */
    @GetMapping("/offers/{id}")
    public ResponseEntity<OfferResponse> getOffer(@PathVariable String id) {
        SharedTaxiOffer offer = tripService.getOffer(id);
        return ResponseEntity.ok(OfferResponse.from(offer));
    }

    /** POST /api/sharedtaxi/offers/{id}/accept */
    @PostMapping("/offers/{id}/accept")
    public ResponseEntity<OfferResponse> acceptOffer(@PathVariable String id) {
        SharedTaxiOffer offer = tripService.acceptOffer(id);
        return ResponseEntity.ok(OfferResponse.from(offer));
    }

    /** POST /api/sharedtaxi/offers/{id}/reject */
    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<OfferResponse> rejectOffer(@PathVariable String id) {
        SharedTaxiOffer offer = tripService.rejectOffer(id);
        return ResponseEntity.ok(OfferResponse.from(offer));
    }

    /** POST /api/sharedtaxi/active-trips/{id}/stops/{stopId}/complete */
    @PostMapping("/active-trips/{id}/stops/{stopId}/complete")
    public ResponseEntity<ActiveTripResponse> completeStop(@PathVariable String id,
                                                           @PathVariable String stopId) {
        ActiveSharedTrip trip = tripService.completeStop(id, stopId);
        return ResponseEntity.ok(ActiveTripResponse.from(trip));
    }
}

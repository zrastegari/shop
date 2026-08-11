package com.learning.shop.controller;

import com.learning.shop.document.CarpoolMatch;
import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.dto.carpool.CreateTripRequest;
import com.learning.shop.dto.carpool.CreateTripResponse;
import com.learning.shop.dto.carpool.MatchResponse;
import com.learning.shop.dto.carpool.TripResponse;
import com.learning.shop.service.CarpoolMatchingService;
import com.learning.shop.service.CarpoolTripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * کنترلر REST سرویس هم‌پیمایی (Carpool).
 * <p>
 * نقطه ورود تمام عملیات: ثبت/بازیابی/لغو سفر و تأیید/رد match.
 * Document ها مستقیم expose نمی‌شوند — همه‌چیز از طریق DTO های {@code dto.carpool}.
 */
@RestController
@RequestMapping("/api/carpool")
public class CarpoolController {

    private final CarpoolTripService tripService;
    private final CarpoolMatchingService matchingService;

    public CarpoolController(CarpoolTripService tripService,
                             CarpoolMatchingService matchingService) {
        this.tripService = tripService;
        this.matchingService = matchingService;
    }

    /**
     * ثبت یک سفر هم‌پیمایی جدید و سپس اجرای فوری الگوریتم matching.
     * <p>
     * POST /api/carpool/trips
     * پاسخ شامل سفر ثبت‌شده و match های پیدا شده است.
     */
    @PostMapping("/trips")
    public ResponseEntity<CreateTripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        // ۱) ذخیره‌ی سفر جدید (ACTIVE)
        CarpoolTrip savedTrip = tripService.createTrip(request);

        // ۲) اجرای الگوریتم matching برای سفر جدید
        List<CarpoolMatch> matches = matchingService.findAndCreateMatchesForNewTrip(savedTrip);

        List<MatchResponse> matchResponses = matches.stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());

        CreateTripResponse response = new CreateTripResponse(TripResponse.from(savedTrip), matchResponses);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * بازیابی یک سفر مشخص.
     * GET /api/carpool/trips/{id}
     */
    @GetMapping("/trips/{id}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable String id) {
        CarpoolTrip trip = tripService.getTrip(id);
        return ResponseEntity.ok(tripService.toResponse(trip));
    }

    /**
     * دریافت همه match های یک سفر.
     * GET /api/carpool/trips/{id}/matches
     */
    @GetMapping("/trips/{id}/matches")
    public ResponseEntity<List<MatchResponse>> getTripMatches(@PathVariable String id) {
        List<CarpoolMatch> matches = matchingService.findMatchesForTrip(id);
        List<MatchResponse> responses = matches.stream()
                .map(MatchResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * تأیید یک match پیشنهادی.
     * POST /api/carpool/matches/{id}/confirm
     */
    @PostMapping("/matches/{id}/confirm")
    public ResponseEntity<MatchResponse> confirmMatch(@PathVariable String id) {
        CarpoolMatch match = matchingService.confirmMatch(id, tripService);
        return ResponseEntity.ok(MatchResponse.from(match));
    }

    /**
     * رد کردن یک match پیشنهادی.
     * POST /api/carpool/matches/{id}/reject
     */
    @PostMapping("/matches/{id}/reject")
    public ResponseEntity<MatchResponse> rejectMatch(@PathVariable String id) {
        CarpoolMatch match = matchingService.rejectMatch(id);
        return ResponseEntity.ok(MatchResponse.from(match));
    }

    /**
     * لغو یک سفر (وضعیت -> CANCELLED).
     * DELETE /api/carpool/trips/{id}
     */
    @DeleteMapping("/trips/{id}")
    public ResponseEntity<Void> cancelTrip(@PathVariable String id) {
        tripService.cancelTrip(id);
        return ResponseEntity.noContent().build();
    }
}

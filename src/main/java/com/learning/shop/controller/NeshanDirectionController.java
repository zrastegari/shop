package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanDirectionResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/neshan/direction")
public class NeshanDirectionController {

    private final Neshanroutingservice neshanRoutingService;

    public NeshanDirectionController(Neshanroutingservice neshanRoutingService) {
        this.neshanRoutingService = neshanRoutingService;
    }

    // ============================================================
    //   سرویس ۱: مسیریابی با ترافیک (Direction with Traffic)
    //   GET /api/neshan/direction/with-traffic
    // ============================================================
    @GetMapping("/with-traffic")
    public ResponseEntity<NeshanDirectionResponse> getDirectionWithTraffic(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "car") String type,
            @RequestParam(required = false) String waypoints,
            @RequestParam(defaultValue = "false") boolean avoidTrafficZone,
            @RequestParam(defaultValue = "false") boolean avoidOddEvenZone,
            @RequestParam(defaultValue = "false") boolean alternative,
            @RequestParam(required = false) Integer bearing) {

        NeshanDirectionResponse response = neshanRoutingService.getDirectionWithTraffic(
                origin, destination, type, waypoints,
                avoidTrafficZone, avoidOddEvenZone, alternative, bearing);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۲: مسیریابی بدون ترافیک (Direction No Traffic)
    //   GET /api/neshan/direction/no-traffic
    // ============================================================
    @GetMapping("/no-traffic")
    public ResponseEntity<NeshanDirectionResponse> getDirectionNoTraffic(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String waypoints,
            @RequestParam(defaultValue = "false") boolean avoidTrafficZone,
            @RequestParam(defaultValue = "false") boolean avoidOddEvenZone,
            @RequestParam(defaultValue = "false") boolean alternative,
            @RequestParam(required = false) Integer bearing) {

        NeshanDirectionResponse response = neshanRoutingService.getDirectionNoTraffic(
                origin, destination, waypoints,
                avoidTrafficZone, avoidOddEvenZone, alternative, bearing);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۳: مسیریابی براساس الگوی ترافیک (Typical Routing)
    //   GET /api/neshan/direction/typical
    // ============================================================
    @GetMapping("/typical")
    public ResponseEntity<NeshanDirectionResponse> getDirectionTypical(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "car") String type,
            @RequestParam(required = false) String waypoints,
            @RequestParam(defaultValue = "false") boolean avoidTrafficZone,
            @RequestParam(defaultValue = "false") boolean avoidOddEvenZone,
            @RequestParam(defaultValue = "false") boolean alternative,
            @RequestParam(required = false) Integer bearing) {

        NeshanDirectionResponse response = neshanRoutingService.getDirectionTypical(
                origin, destination, type, waypoints,
                avoidTrafficZone, avoidOddEvenZone, alternative, bearing);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۴: مسیریابی عابر پیاده (Pedestrian)
    //   محاسبه بهترین مسیر برای عابر پیاده
    //   GET /api/neshan/direction/pedestrian
    // ============================================================
    @GetMapping("/pedestrian")
    public ResponseEntity<NeshanDirectionResponse> getDirectionPedestrian(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false) String waypoints,
            @RequestParam(defaultValue = "false") boolean avoidTrafficZone,
            @RequestParam(defaultValue = "false") boolean avoidOddEvenZone,
            @RequestParam(defaultValue = "false") boolean alternative,
            @RequestParam(required = false) Integer bearing) {

        NeshanDirectionResponse response = neshanRoutingService.getDirectionPedestrian(
                origin, destination, waypoints,
                avoidTrafficZone, avoidOddEvenZone, alternative, bearing);

        return ResponseEntity.ok(response);
    }
}

package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanPredictiveResponse;
import com.learning.shop.service.NeshanPredictiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * کنترلر سرویس مسیریابی پیش‌بینی (Predictive Routing)
 *
 * مسیر پایه: /api/neshan/direction/predictive
 *
 * این سرویس با تحلیل داده‌های ترافیکی گذشته، بهترین مسیر را بر اساس
 * زمان مشخص‌شده (از حالا تا یک هفته آینده) پیشنهاد می‌دهد.
 */
@RestController
@RequestMapping("/api/neshan/direction")
public class NeshanPredictiveController {

    private final NeshanPredictiveService predictiveService;

    public NeshanPredictiveController(NeshanPredictiveService predictiveService) {
        this.predictiveService = predictiveService;
    }

    /**
     * مسیریابی پیش‌بینی - با تمام پارامترها
     *
     * مثال:
     * GET /api/neshan/direction/predictive
     *   ?origin=35.7208,51.4323
     *   &destination=35.6703,51.2984
     *   &routingType=DepartAt
     *   &dateTime=2026-06-03T14:00
     *   &avoidTrafficZone=false
     *   &alternative=false
     */
    @GetMapping("/predictive")
    public ResponseEntity<NeshanPredictiveResponse> getPredictiveDirection(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String routingType,
            @RequestParam String dateTime,
            @RequestParam(defaultValue = "false") boolean avoidTrafficZone,
            @RequestParam(defaultValue = "false") boolean alternative) {

        NeshanPredictiveResponse response = predictiveService.getPredictiveDirection(
                origin, destination, routingType, dateTime, avoidTrafficZone, alternative);
        return ResponseEntity.ok(response);
    }

    /**
     * مسیریابی پیش‌بینی - نسخه ساده با حداقل پارامترها
     *
     * مثال:
     * GET /api/neshan/direction/predictive/simple
     *   ?origin=35.7208,51.4323
     *   &destination=35.6703,51.2984
     *   &routingType=DepartAt
     *   &dateTime=2026-06-03T14:00
     */
    @GetMapping("/predictive/simple")
    public ResponseEntity<NeshanPredictiveResponse> getPredictiveDirectionSimple(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String routingType,
            @RequestParam String dateTime) {

        NeshanPredictiveResponse response = predictiveService.getPredictiveDirection(
                origin, destination, routingType, dateTime);
        return ResponseEntity.ok(response);
    }
}

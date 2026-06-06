package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanMapMatchingResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/neshan/map-matching")
public class NeshanMapMatchingController {

    private final Neshanroutingservice neshanroutingservice;

    public NeshanMapMatchingController(Neshanroutingservice neshanroutingservice) {
        this.neshanroutingservice = neshanroutingservice;
    }

    /**
     * نگاشت نقطه بر نقشه (Map Matching)
     * مجموعه نقاط GPS رو دریافت کرده و به محتمل‌ترین مسیر روی نقشه نگاشت می‌کنه
     *
     * نمونه درخواست:
     * POST /api/neshan/map-matching
     * {
     *   "path": "35.703983747058494,51.3213872909546|35.70363307719029,51.32144361734391"
     * }
     */
    @PostMapping
    public ResponseEntity<NeshanMapMatchingResponse> getMapMatching(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        NeshanMapMatchingResponse response = neshanroutingservice.getMapMatching(path);
        return ResponseEntity.ok(response);
    }
}

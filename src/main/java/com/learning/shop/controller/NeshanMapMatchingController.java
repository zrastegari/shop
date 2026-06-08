package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanMapMatchingResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/neshan/mapmatching")
public class NeshanMapMatchingController {

    private final Neshanroutingservice neshanroutingservice;

    public NeshanMapMatchingController(Neshanroutingservice neshanroutingservice) {
        this.neshanroutingservice = neshanroutingservice;
    }

    @GetMapping
    public ResponseEntity<String> getMapMatchingTest() {
        try {
            String testPath = "35.703983747058494,51.3213872909546|35.70363307719029,51.32144361734391";
            NeshanMapMatchingResponse response = neshanroutingservice.getMapMatching(testPath);
            return ResponseEntity.ok("✅ سرویس Map Matching کار می‌کند! " + response.getSnappedPoints().size() + " نقطه");
        } catch (Exception e) {
            return ResponseEntity.ok("❌ خطا: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<NeshanMapMatchingResponse> getMapMatching(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        NeshanMapMatchingResponse response = neshanroutingservice.getMapMatching(path);
        return ResponseEntity.ok(response);
    }
}

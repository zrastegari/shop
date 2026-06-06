package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanDistanceMatrixResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/neshan")
public class NeshanDistanceMatrixController {

    private final Neshanroutingservice neshanRoutingService;

    public NeshanDistanceMatrixController(Neshanroutingservice neshanRoutingService) {
        this.neshanRoutingService = neshanRoutingService;
    }

    // ============================================================
    //   سرویس ماتریس فاصله (Distance Matrix)
    //   GET /api/neshan/distance-matrix
    //
    //   محاسبه همزمان فاصله و زمان بین چندین مبدأ و چندین مقصد
    //
    //   پارامترها:
    //     origins (اجباری)      - نقاط مبدأ جدا شده با ;   مثال: 35.72,51.43;35.70,51.35
    //     destinations (اجباری)  - نقاط مقصد جدا شده با ;   مثال: 35.67,51.29;35.68,51.31
    //     type (اختیاری)         - نوع وسیله نقلیه (پیش‌فرض: car)
    // ============================================================
    @GetMapping("/distance-matrix")
    public ResponseEntity<NeshanDistanceMatrixResponse> getDistanceMatrix(
            @RequestParam String origins,
            @RequestParam String destinations,
            @RequestParam(defaultValue = "car") String type) {

        // جایگزین کردن ; با | (pipe) برای جداسازی چندین مختصات
        // encode مستقیم انجام نمیدیم چون سرویس URLEncoder رو انجام میده
        String formattedOrigins = origins.replace(";", "|");
        String formattedDestinations = destinations.replace(";", "|");

        NeshanDistanceMatrixResponse response = neshanRoutingService.getDistanceMatrix(
                formattedOrigins, formattedDestinations, type);

        return ResponseEntity.ok(response);
    }
}

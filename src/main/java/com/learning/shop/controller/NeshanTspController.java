package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanTspResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/neshan")
public class NeshanTspController {

    private final Neshanroutingservice neshanRoutingService;

    public NeshanTspController(Neshanroutingservice neshanRoutingService) {
        this.neshanRoutingService = neshanRoutingService;
    }

    // ============================================================
    //   سرویس TSP: فروشنده دوره‌گرد (Traveling Salesman Problem)
    //   GET /api/neshan/tsp
    //
    //   محاسبه بهترین ترتیب برای بازدید از نقاط
    //   پارامترها:
    //     waypoints (اجباری)     - نقاط جدا شده با |   مثال: 35.72,51.43|35.67,51.29
    //     roundTrip (اختیاری)    - بازگشت به مبدأ (پیش‌فرض: true)
    //     sourceIsAnyPoint (اختیاری) - انتخاب خودکار مبدأ (پیش‌فرض: true)
    //     lastIsAnyPoint (اختیاری)   - انتخاب خودکار مقصد (پیش‌فرض: true)
    // ============================================================
    @GetMapping("/tsp")
    public ResponseEntity<NeshanTspResponse> getTsp(
            @RequestParam String waypoints,
            @RequestParam(defaultValue = "true") boolean roundTrip,
            @RequestParam(defaultValue = "true") boolean sourceIsAnyPoint,
            @RequestParam(defaultValue = "true") boolean lastIsAnyPoint) {

        // جایگزین کردن ; با | تا کاربر بتونه توی مرورگر راحت استفاده کنه
        String formattedWaypoints = waypoints.replace(";", "|");
        NeshanTspResponse response = neshanRoutingService.getTsp(
                formattedWaypoints, roundTrip, sourceIsAnyPoint, lastIsAnyPoint);

        return ResponseEntity.ok(response);
    }
}

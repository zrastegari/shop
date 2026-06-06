package com.learning.shop.controller;

import com.learning.shop.dto.neshan.NeshanIsochroneResponse;
import com.learning.shop.service.Neshanroutingservice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * کنترلر سرویس محدوده در دسترس (Isochrone)

 * مسیر پایه: /api/neshan/isochrone

 * این سرویس محدوده قابل دسترسی از یک نقطه مرکزی را در زمان یا مسافت مشخص
 * به صورت GeoJSON برمی‌گرداند. کاربردها:
 *   - تحلیل دسترسی به خدمات شهری
 *   - تعیین محدوده سرویس‌دهی
 *   - برنامه‌ریزی مسیرهای اضطراری

 * مستندات: https://platform.neshan.org/docs/api/routing-category/isochrone/
 */
@RestController
@RequestMapping("/api/neshan")
public class NeshanIsochroneController {

    private final Neshanroutingservice neshanRoutingService;

    public NeshanIsochroneController(Neshanroutingservice neshanRoutingService) {
        this.neshanRoutingService = neshanRoutingService;
    }

    // ============================================================
    //   سرویس ۷: محدوده در دسترس بر اساس مسافت (Isochrone by Distance)
    //   GET /api/neshan/isochrone/distance
    //
    //   محاسبه محدوده قابل دسترسی با حداکثر مسافت مشخص
    //
    //   پارامترها:
    //     location (اجباری)    - مختصات مرکز "Lat,Lng"   مثال: 35.7208,51.4323
    //     distance (اجباری)    - حداکثر مسافت بر حسب کیلومتر   مثال: 5
    //     polygon (اختیاری)    - true => Polygon ، false => LineString (پیش‌فرض: false)
    //     denoise (اختیاری)    - عدد 0 تا 1 برای ساده‌سازی (پیش‌فرض: 0)
    //
    //   مثال:
    //   GET /api/neshan/isochrone/distance?location=35.7208,51.4323&distance=5&polygon=true
    // ============================================================
    @GetMapping("/isochrone/distance")
    public ResponseEntity<NeshanIsochroneResponse> getIsochroneByDistance(
            @RequestParam String location,
            @RequestParam Double distance,
            @RequestParam(required = false) Boolean polygon,
            @RequestParam(required = false) Double denoise) {

        NeshanIsochroneResponse response = neshanRoutingService.getIsochroneByDistance(

                location, distance, polygon, denoise);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۷: محدوده در دسترس بر اساس زمان (Isochrone by Time)
    //   GET /api/neshan/isochrone/time
    //
    //   محاسبه محدوده قابل دسترسی با حداکثر زمان مشخص
    //
    //   پارامترها:
    //     location (اجباری)    - مختصات مرکز "Lat,Lng"   مثال: 35.7208,51.4323
    //     time (اجباری)        - حداکثر زمان بر حسب دقیقه   مثال: 10
    //     polygon (اختیاری)    - true => Polygon ، false => LineString (پیش‌فرض: false)
    //     denoise (اختیاری)    - عدد 0 تا 1 برای ساده‌سازی (پیش‌فرض: 0)
    //
    //   مثال:
    //   GET /api/neshan/isochrone/time?location=35.7208,51.4323&time=10&polygon=true
    // ============================================================
    @GetMapping("/isochrone/time")
    public ResponseEntity<NeshanIsochroneResponse> getIsochroneByTime(
            @RequestParam String location,
            @RequestParam Double time,
            @RequestParam(required = false) Boolean polygon,
            @RequestParam(required = false) Double denoise) {

        NeshanIsochroneResponse response = neshanRoutingService.getIsochroneByTime(
                location, time, polygon, denoise);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۷: محدوده در دسترس - نسخه ساده بر اساس مسافت
    //   GET /api/neshan/isochrone/distance/simple
    //
    //   فقط با پارامترهای اجباری (بدون polygon و denoise)
    //
    //   مثال:
    //   GET /api/neshan/isochrone/distance/simple?location=35.7208,51.4323&distance=5
    // ============================================================
    @GetMapping("/isochrone/distance/simple")
    public ResponseEntity<NeshanIsochroneResponse> getIsochroneByDistanceSimple(
            @RequestParam String location,
            @RequestParam Double distance) {

        NeshanIsochroneResponse response = neshanRoutingService.getIsochroneByDistance(
                location, distance);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    //   سرویس ۷: محدوده در دسترس - نسخه ساده بر اساس زمان
    //   GET /api/neshan/isochrone/time/simple
    //
    //   فقط با پارامترهای اجباری (بدون polygon و denoise)
    //
    //   مثال:
    //   GET /api/neshan/isochrone/time/simple?location=35.7208,51.4323&time=10
    // ============================================================
    @GetMapping("/isochrone/time/simple")
    public ResponseEntity<NeshanIsochroneResponse> getIsochroneByTimeSimple(
            @RequestParam String location,
            @RequestParam Double time) {

        NeshanIsochroneResponse response = neshanRoutingService.getIsochroneByTime(
                location, time);

        return ResponseEntity.ok(response);
    }
}

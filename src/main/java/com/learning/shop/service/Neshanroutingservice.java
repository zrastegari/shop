package com.learning.shop.service;

import com.learning.shop.dto.neshan.NeshanDirectionResponse;
import com.learning.shop.dto.neshan.NeshanDistanceMatrixResponse;
import com.learning.shop.dto.neshan.NeshanIsochroneResponse;
import com.learning.shop.dto.neshan.NeshanMapMatchingResponse;
import com.learning.shop.dto.neshan.NeshanTspResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class Neshanroutingservice {

    private final RestTemplate restTemplate;

    @Value("${neshan.api-key}")
    private String apiKey;

    private static final String BASE_URL_TRAFFIC = "https://api.neshan.org/v4/direction";
    private static final String BASE_URL_NO_TRAFFIC = "https://api.neshan.org/v4/direction/no-traffic";
    private static final String BASE_URL_TYPICAL     = "https://api.neshan.org/v1/direction/typical";
    private static final String BASE_URL_TSP         = "https://api.neshan.org/v3/trip";
    private static final String BASE_URL_DISTANCE_MATRIX = "https://api.neshan.org/v1/distance-matrix";
    private static final String BASE_URL_ISOCHRONE      = "https://api.neshan.org/v1/isochrone";

    public Neshanroutingservice() {
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        return headers;
    }

    private NeshanDirectionResponse sendDirectionRequest(String baseUrl, String origin,
                                                          String destination, String type,
                                                          String waypoints,
                                                          boolean avoidTrafficZone,
                                                          boolean avoidOddEvenZone,
                                                          boolean alternative,
                                                          Integer bearing) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("type", type);

        if (waypoints != null && !waypoints.isBlank()) {
            builder.queryParam("waypoints", URLEncoder.encode(waypoints, StandardCharsets.UTF_8));
        }
        builder.queryParam("avoidTrafficZone", avoidTrafficZone);
        builder.queryParam("avoidOddEvenZone", avoidOddEvenZone);
        builder.queryParam("alternative", alternative);
        if (bearing != null) builder.queryParam("bearing", bearing);

        String url = builder.build(true).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<NeshanDirectionResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NeshanDirectionResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public NeshanDirectionResponse getDirectionWithTraffic(String origin, String destination) {
        return getDirectionWithTraffic(origin, destination, "car", null, false, false, false, null);
    }

    public NeshanDirectionResponse getDirectionWithTraffic(String origin,
                                                           String destination,
                                                           String type,
                                                           String waypoints,
                                                           boolean avoidTrafficZone,
                                                           boolean avoidOddEvenZone,
                                                           boolean alternative,
                                                           Integer bearing) {
        return sendDirectionRequest(BASE_URL_TRAFFIC, origin, destination, type,
                waypoints, avoidTrafficZone, avoidOddEvenZone, alternative, bearing);
    }

    // =====================================================
    //   سرویس ۲: مسیریابی بدون ترافیک (Direction No Traffic)
    //   دریافت بهترین مسیر بدون در نظر گرفتن ترافیک (مسیر کوتاه‌ترین)
    // =====================================================

    public NeshanDirectionResponse getDirectionNoTraffic(String origin, String destination) {
        return getDirectionNoTraffic(origin, destination, null, false, false, false, null);
    }

    public NeshanDirectionResponse getDirectionNoTraffic(String origin,
                                                         String destination,
                                                         String waypoints,
                                                         boolean avoidTrafficZone,
                                                         boolean avoidOddEvenZone,
                                                         boolean alternative,
                                                         Integer bearing) {
        return sendDirectionRequest(BASE_URL_NO_TRAFFIC, origin, destination, "car",
                waypoints, avoidTrafficZone, avoidOddEvenZone, alternative, bearing);
    }

    // ================================================================
    //   سرویس ۳: مسیریابی براساس الگوی ترافیک (Typical Routing)
    //   محاسبه مسیر بر اساس الگوی ترافیکی معابر (بروزرسانی هر ۱۵ دقیقه)
    //   این سرویس عملاً نقش تخمین زمان سفر (ETA) را نیز ایفا می‌کند
    // ================================================================

    /** نسخه ساده (فقط مبدأ و مقصد) */
    public NeshanDirectionResponse getDirectionTypical(String origin, String destination) {
        return getDirectionTypical(origin, destination, "car", null, false, false, false, null);
    }

    /** نسخه کامل با تمام پارامترها */
    public NeshanDirectionResponse getDirectionTypical(String origin,
                                                       String destination,
                                                       String type,
                                                       String waypoints,
                                                       boolean avoidTrafficZone,
                                                       boolean avoidOddEvenZone,
                                                       boolean alternative,
                                                       Integer bearing) {
        return sendDirectionRequest(BASE_URL_TYPICAL, origin, destination, type,
                waypoints, avoidTrafficZone, avoidOddEvenZone, alternative, bearing);
    }

    // ================================================================
    //   سرویس ۴: مسیریابی عابر پیاده (Pedestrian Routing)
    //   محاسبه بهترین مسیر برای عابر پیاده با استفاده از type=pedestrian
    //   در endpoint اصلی /v4/direction
    // ================================================================

    /** نسخه ساده (فقط مبدأ و مقصد) */
    public NeshanDirectionResponse getDirectionPedestrian(String origin, String destination) {
        return sendDirectionRequest(BASE_URL_TRAFFIC, origin, destination, "pedestrian",
                null, false, false, false, null);
    }

    /** نسخه کامل با تمام پارامترها */
    public NeshanDirectionResponse getDirectionPedestrian(String origin,
                                                          String destination,
                                                          String waypoints,
                                                          boolean avoidTrafficZone,
                                                          boolean avoidOddEvenZone,
                                                          boolean alternative,
                                                          Integer bearing) {
        return sendDirectionRequest(BASE_URL_TRAFFIC, origin, destination, "pedestrian",
                waypoints, avoidTrafficZone, avoidOddEvenZone, alternative, bearing);
    }

    // ================================================================
    //   سرویس ۵: فروشنده دوره‌گرد (TSP - Traveling Salesman Problem)
    //   بهینه‌سازی مسیر بازدید از چندین مقصد با استفاده از الگوریتم TSP
    //   محاسبه بهترین ترتیب برای بازدید از نقاط (با یا بدون بازگشت به مبدأ)
    //   مستندات: https://platform.neshan.org/docs/api/routing-category/tsp/
    // ================================================================

    /**
     * متد خصوصی برای ارسال درخواست TSP
     */
    private NeshanTspResponse sendTspRequest(String waypoints,
                                              boolean roundTrip,
                                              boolean sourceIsAnyPoint,
                                              boolean lastIsAnyPoint) {
        // ساختن URL با encode کردن waypoints (| به %7C تبدیل میشه)
        String urlStr = BASE_URL_TSP + "?waypoints=" + URLEncoder.encode(waypoints, StandardCharsets.UTF_8)
                + "&roundTrip=" + roundTrip
                + "&sourceIsAnyPoint=" + sourceIsAnyPoint
                + "&lastIsAnyPoint=" + lastIsAnyPoint;

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            java.net.URI uri = new java.net.URI(urlStr);
            ResponseEntity<NeshanTspResponse> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, NeshanTspResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString() + " [URL: " + urlStr + "]");
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage() + " [URL: " + urlStr + "]");
        }
    }

    /**
     * نسخه کامل با تمام پارامترها
     *
     * @param waypoints       نقاط مورد نظر جدا شده با |
     *                        مثال: "35.7208,51.4323|35.6703,51.2984"
     * @param roundTrip       آیا مسیر به نقطه شروع بازگردد؟ (پیش‌فرض: true)
     * @param sourceIsAnyPoint اگر true، بهینه‌ترین نقطه شروع انتخاب شود (پیش‌فرض: true)
     * @param lastIsAnyPoint   اگر true، بهینه‌ترین نقطه پایان انتخاب شود (پیش‌فرض: true)
     */
    public NeshanTspResponse getTsp(String waypoints,
                                     boolean roundTrip,
                                     boolean sourceIsAnyPoint,
                                     boolean lastIsAnyPoint) {
        return sendTspRequest(waypoints, roundTrip, sourceIsAnyPoint, lastIsAnyPoint);
    }

    /**
     * نسخه ساده (فقط waypoints، با پیش‌فرض‌های پیشنهادی نشون)
     */
    public NeshanTspResponse getTsp(String waypoints) {
        return getTsp(waypoints, true, true, true);
    }

    // ================================================================
    //   سرویس ۷: محدوده در دسترس (Isochrone)
    //   محاسبه محدوده قابل دسترسی از یک نقطه مرکزی در زمان یا مسافت مشخص
    //   خروجی به صورت GeoJSON با قابلیت نمایش روی نقشه
    //   موارد کاربرد: تحلیل دسترسی، تعیین محدوده سرویس‌دهی، برنامه‌ریزی شهری
    //   مستندات: https://platform.neshan.org/docs/api/routing-category/isochrone/
    // ================================================================

    /**
     * متد خصوصی برای ارسال درخواست Isochrone
     *
     * @param location مختصات مرکز به صورت "Lat,Lng"  مثال: "35.7208,51.4323"
     * @param distance حداکثر مسافت بر حسب کیلومتر (اختیاری در صورت وجود time)
     * @param time     حداکثر زمان بر حسب دقیقه (اختیاری در صورت وجود distance)
     * @param polygon  true برای خروجی Polygon، false برای LineString (پیش‌فرض: null)
     * @param denoise  عدد 0 تا 1 برای ساده‌سازی محدوده (پیش‌فرض: 0)
     */
    private NeshanIsochroneResponse sendIsochroneRequest(String location,
                                                         Double distance,
                                                         Double time,
                                                         Boolean polygon,
                                                         Double denoise) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL_ISOCHRONE)
                .queryParam("location", location);

        if (distance != null) {
            builder.queryParam("distance", distance);
        }
        if (time != null) {
            builder.queryParam("time", time);
        }
        if (polygon != null) {
            builder.queryParam("polygon", polygon);
        }
        if (denoise != null) {
            builder.queryParam("denoise", denoise);
        }

        String url = builder.build(true).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<NeshanIsochroneResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NeshanIsochroneResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    /**
     * محدوده در دسترس بر اساس مسافت (Isochrone by Distance)
     *
     * @param location مختصات مرکز
     * @param distance حداکثر مسافت بر حسب کیلومتر
     * @param polygon  true برای Polygon
     * @param denoise  عدد 0 تا 1 برای ساده‌سازی
     */
    public NeshanIsochroneResponse getIsochroneByDistance(String location,
                                                          Double distance,
                                                          Boolean polygon,
                                                          Double denoise) {
        return sendIsochroneRequest(location, distance, null, polygon, denoise);
    }

    /**
     * محدوده در دسترس بر اساس زمان (Isochrone by Time)
     *
     * @param location مختصات مرکز
     * @param time     حداکثر زمان بر حسب دقیقه
     * @param polygon  true برای Polygon
     * @param denoise  عدد 0 تا 1 برای ساده‌سازی
     */
    public NeshanIsochroneResponse getIsochroneByTime(String location,
                                                      Double time,
                                                      Boolean polygon,
                                                      Double denoise) {
        return sendIsochroneRequest(location, null, time, polygon, denoise);
    }

    /**
     * محدوده در دسترس بر اساس مسافت - نسخه ساده (فقط پارامترهای اجباری)
     *
     * @param location مختصات مرکز
     * @param distance حداکثر مسافت بر حسب کیلومتر
     */
    public NeshanIsochroneResponse getIsochroneByDistance(String location,
                                                          Double distance) {
        return sendIsochroneRequest(location, distance, null, null, null);
    }

    /**
     * محدوده در دسترس بر اساس زمان - نسخه ساده (فقط پارامترهای اجباری)
     *
     * @param location مختصات مرکز
     * @param time     حداکثر زمان بر حسب دقیقه
     */
    public NeshanIsochroneResponse getIsochroneByTime(String location,
                                                      Double time) {
        return sendIsochroneRequest(location, null, time, null, null);
    }

    // ================================================================
    //   سرویس ۶: ماتریس فاصله (Distance Matrix)
    //   محاسبه همزمان فاصله و زمان بین چندین مبدأ و چندین مقصد
      /**
     * متد خصوصی برای ارسال درخواست Distance Matrix
     *
     * @param origins      نقاط مبدأ جدا شده با |  مثال: "35.7208,51.4323|35.7000,51.3500"
     * @param destinations نقاط مقصد جدا شده با |  مثال: "35.6703,51.2984|35.6800,51.3100"
     * @param type         نوع وسیله نقلیه (پیش‌فرض: car)
     */
    private NeshanDistanceMatrixResponse sendDistanceMatrixRequest(String origins,
                                                                    String destinations,
                                                                    String type) {
        String urlStr = BASE_URL_DISTANCE_MATRIX + "?origins=" + URLEncoder.encode(origins, StandardCharsets.UTF_8)
                + "&destinations=" + URLEncoder.encode(destinations, StandardCharsets.UTF_8);

        if (type != null && !type.isBlank()) {
            urlStr += "&type=" + URLEncoder.encode(type, StandardCharsets.UTF_8);
        }

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            // استفاده از URI برای جلوگیری از double encoding توسط RestTemplate
            java.net.URI uri = new java.net.URI(urlStr);
            ResponseEntity<NeshanDistanceMatrixResponse> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, NeshanDistanceMatrixResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString() + " [URL: " + urlStr + "]");
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage() + " [URL: " + urlStr + "]");
        }
    }

    /**
     * نسخه کامل
     *
     * @param origins      نقاط مبدأ (جدا شده با | )   مثال: "35.7208,51.4323;35.7000,51.3500"
     * @param destinations نقاط مقصد (جدا شده با | )   مثال: "35.6703,51.2984;35.6800,51.3100"
     * @param type         نوع وسیله نقلیه (car, motorcycle, ...)
     */
    public NeshanDistanceMatrixResponse getDistanceMatrix(String origins,
                                                           String destinations,
                                                           String type) {
        return sendDistanceMatrixRequest(origins, destinations, type);
    }

    /**
     * نسخه ساده (فقط مبدأ و مقصد، با وسیله نقلیه car)
     */
    public NeshanDistanceMatrixResponse getDistanceMatrix(String origins,
                                                           String destinations) {
        return getDistanceMatrix(origins, destinations, "car");
    }

    // ================================================================
    //   سرویس ۸: نگاشت نقطه بر نقشه (Map Matching)
    //   دریافت مجموعه‌ای از نقاط GPS خام و نگاشت آن‌ها به محتمل‌ترین
    //   مسیر واقعی روی نقشه (اصلاح خطاهای مکانی نقاط)
    //   حداکثر ۱۰۰۰ نقطه در هر درخواست
    //   مستندات: https://platform.neshan.org/docs/api/routing-category/map-matching/
    // ================================================================

    private static final String BASE_URL_MAP_MATCHING = "https://api.neshan.org/v3/map-matching";

    public NeshanMapMatchingResponse getMapMatching(String path) {
        // استفاده از JSON (طبق تست مستقیم API نشون، form-urlencoded پشتیبانی نمی‌شه)
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("path", path);

        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<java.util.Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<NeshanMapMatchingResponse> response = restTemplate.exchange(
                    BASE_URL_MAP_MATCHING,
                    HttpMethod.POST,
                    entity,
                    NeshanMapMatchingResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}

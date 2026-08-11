package com.learning.shop.service;

import com.learning.shop.dto.mapir.MapIrDirectionResponse;
import com.learning.shop.dto.mapir.MapIrTspResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * ======================================================================
 * سرویس فراخوانی APIهای مسیریابی شرکت مپ (map.ir)
 * ----------------------------------------------------------------------
 * این سرویس برای ارتباط با APIهای مسیریابی و بهینه‌سازی مسیر مپ طراحی شده است.
 *
 * سرویس‌های فعلی:
 *   ۱) سرویس مسیریابی (Direction / Routing)
 *      - محاسبه بهترین مسیر بین مبدأ و مقصد (با نقاط میانی اختیاری)
 *      - پشتیبانی از انواع: خودرو، طرح ترافیک، زوج و فرد، دوچرخه، پیاده
 *      - مستندات: https://help.map.ir/route-api/
 *
 *   ۲) سرویس TSP (فروشنده دوره‌گرد / Traveling Salesman Problem)
 *      - بهینه‌سازی ترتیب بازدید از چندین نقطه
 *      - مستندات: https://support.map.ir/developers/api/tsp/
 *
 * API مپ بر خلاف نشون:
 *   - احراز هویت: هدر x-api-key
 *   - coordinates: lng,lat;lng,lat (path parameter با جداکننده ;)
 * ======================================================================
 */
@Service
public class MapRoutingService {

    private final RestTemplate restTemplate;

    @Value("${map.api-key}")
    private String apiKey;

    // ============================================================
    //   Base URL ها
    // ============================================================
    private static final String BASE_URL_TSP = "https://map.ir/routes/vrp/tsp/v1/driving";
    private static final String BASE_URL_ROUTE = "https://map.ir/routes/route/v1/driving";
    private static final String BASE_URL_TARH = "https://map.ir/routes/tarh/v1/driving";
    private static final String BASE_URL_ZOOJOFARD = "https://map.ir/routes/zojofard/v1/driving";
    private static final String BASE_URL_BICYCLE = "https://map.ir/routes/bicycle/v1/driving";
    private static final String BASE_URL_FOOT = "https://map.ir/routes/foot/v1/driving";

    public MapRoutingService() {
        this.restTemplate = new RestTemplate();
    }

    // ============================================================
    //   هدرها
    // ============================================================
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        return headers;
    }

    // ================================================================
    //   سرویس مسیریابی (Direction)
    // ================================================================
    private MapIrDirectionResponse sendDirectionRequest(String baseUrl,
                                                         String coordinates,
                                                         Boolean alternatives,
                                                         Boolean steps,
                                                         String overview,
                                                         String geometries) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/" + coordinates);
        if (alternatives != null) builder.queryParam("alternatives", alternatives);
        if (steps != null) builder.queryParam("steps", steps);
        if (overview != null && !overview.isBlank()) builder.queryParam("overview", overview);
        if (geometries != null && !geometries.isBlank()) builder.queryParam("geometries", geometries);

        String url = builder.build(true).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<MapIrDirectionResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MapIrDirectionResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("خطا در مسیریابی مپ - کد " + e.getStatusCode() +
                    ": " + e.getResponseBodyAsString() + " [URL: " + url + "]");
        } catch (Exception e) {
            throw new RuntimeException("خطا در سرویس مسیریابی مپ: " + e.getMessage() + " [URL: " + url + "]");
        }
    }

    public MapIrDirectionResponse getRoute(String coordinates,
                                            Boolean alternatives,
                                            Boolean steps,
                                            String overview,
                                            String geometries) {
        return sendDirectionRequest(BASE_URL_ROUTE, coordinates, alternatives, steps, overview, geometries);
    }

    public MapIrDirectionResponse getRoute(String coordinates) {
        return getRoute(coordinates, null, null, null, null);
    }

    public MapIrDirectionResponse getRouteWithTarh(String coordinates,
                                                    Boolean alternatives,
                                                    Boolean steps,
                                                    String overview,
                                                    String geometries) {
        return sendDirectionRequest(BASE_URL_TARH, coordinates, alternatives, steps, overview, geometries);
    }

    public MapIrDirectionResponse getRouteWithZojofard(String coordinates,
                                                        Boolean alternatives,
                                                        Boolean steps,
                                                        String overview,
                                                        String geometries) {
        return sendDirectionRequest(BASE_URL_ZOOJOFARD, coordinates, alternatives, steps, overview, geometries);
    }

    public MapIrDirectionResponse getBicycleRoute(String coordinates,
                                                   Boolean alternatives,
                                                   Boolean steps,
                                                   String overview,
                                                   String geometries) {
        return sendDirectionRequest(BASE_URL_BICYCLE, coordinates, alternatives, steps, overview, geometries);
    }

    public MapIrDirectionResponse getFootRoute(String coordinates,
                                                Boolean alternatives,
                                                Boolean steps,
                                                String overview,
                                                String geometries) {
        return sendDirectionRequest(BASE_URL_FOOT, coordinates, alternatives, steps, overview, geometries);
    }

    // ================================================================
    //   سرویس TSP (فروشنده دوره‌گرد)
    // ================================================================
    private MapIrTspResponse sendTspRequest(String coordinates,
                                             Boolean roundtrip,
                                             String source,
                                             String destination,
                                             Boolean steps,
                                             String annotations,
                                             String geometries,
                                             String overview) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL_TSP + "/" + coordinates);
        if (roundtrip != null) builder.queryParam("roundtrip", roundtrip);
        if (source != null && !source.isBlank()) builder.queryParam("source", source);
        if (destination != null && !destination.isBlank()) builder.queryParam("destination", destination);
        if (steps != null) builder.queryParam("steps", steps);
        if (annotations != null && !annotations.isBlank()) builder.queryParam("annotations", annotations);
        if (geometries != null && !geometries.isBlank()) builder.queryParam("geometries", geometries);
        if (overview != null && !overview.isBlank()) builder.queryParam("overview", overview);

        String url = builder.build(true).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<MapIrTspResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, MapIrTspResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("خطا در TSP مپ - کد " + e.getStatusCode() +
                    ": " + e.getResponseBodyAsString() + " [URL: " + url + "]");
        } catch (Exception e) {
            throw new RuntimeException("خطا در TSP مپ: " + e.getMessage() + " [URL: " + url + "]");
        }
    }

    public MapIrTspResponse getTsp(String coordinates,
                                    Boolean roundtrip,
                                    String source,
                                    String destination,
                                    Boolean steps,
                                    String annotations,
                                    String geometries,
                                    String overview) {
        return sendTspRequest(coordinates, roundtrip, source, destination,
                steps, annotations, geometries, overview);
    }

    public MapIrTspResponse getTsp(String coordinates) {
        return getTsp(coordinates, null, null, null, null, null, null, null);
    }

    public MapIrTspResponse getTsp(String coordinates,
                                    Boolean roundtrip,
                                    String source,
                                    String destination) {
        return getTsp(coordinates, roundtrip, source, destination, null, null, null, null);
    }

    // ================================================================
    //   متدهای کمکی
    // ================================================================
    public String buildCoordinatesFromPoints(java.util.List<String> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("لیست نقاط نمی‌تواند خالی باشد");
        }
        return String.join(";", points);
    }

    public String buildCoordinates(String origin, String destination, String... waypoints) {
        StringBuilder sb = new StringBuilder(origin);
        if (waypoints != null) {
            for (String wp : waypoints) {
                sb.append(";").append(wp);
            }
        }
        sb.append(";").append(destination);
        return sb.toString();
    }
}
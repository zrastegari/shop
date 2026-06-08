package com.learning.shop.service;

import com.learning.shop.dto.neshan.NeshanPredictiveResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * سرویس مسیریابی پیش‌بینی (Predictive Routing)
 *
 * این سرویس با تحلیل داده‌های ترافیکی جمع‌آوری‌شده، بهترین مسیر را بر اساس
 * زمان حرکت از مبدأ (routingType=DepartAt) یا زمان رسیدن به مقصد (routingType=ArriveAt)
 * پیشنهاد می‌دهد. بازه زمانی معتبر: از لحظه حال تا یک هفته آینده.
 */
 @Service
public class NeshanPredictiveService {

    private final RestTemplate restTemplate;

    @Value("${neshan.api-key}")
    private String apiKey;

    // Endpoint سرویس مسیریابی پیش‌بینی
    private static final String BASE_URL = "https://api.neshan.org/v1/direction/historical";

    public NeshanPredictiveService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * ایجاد هدرهای مورد نیاز (شامل Api-Key)
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        return headers;
    }

    /**
     * متد اصلی برای ارسال درخواست مسیریابی پیش‌بینی
     *
     * @param origin           مختصات مبدأ (Lat,Lng)
     * @param destination      مختصات مقصد (Lat,Lng)
     * @param routingType      نوع تخمین: "DepartAt" (بر اساس زمان حرکت) یا "ArriveAt" (بر اساس زمان رسیدن)
     * @param dateTime         زمان و تاریخ با فرمت YYYY-MM-DDThh:mm (بازه: از حالا تا یک هفته آینده)
     * @param avoidTrafficZone عبور از محدوده طرح ترافیک (پیش‌فرض: false)
     * @param alternative      ارائه مسیرهای جایگزین (پیش‌فرض: false)
     */
    public NeshanPredictiveResponse getPredictiveDirection(
            String origin,
            String destination,
            String routingType,
            String dateTime,
            boolean avoidTrafficZone,
            boolean alternative) {

        // ساخت URL با پارامترهای مورد نیاز
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("routingType", routingType)
                .queryParam("dateTime", dateTime)
                .queryParam("avoidTrafficZone", avoidTrafficZone)
                .queryParam("alternative", alternative);

        String url = builder.build(true).toUriString();
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<NeshanPredictiveResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NeshanPredictiveResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    /**
     * متد ساده‌شده: مسیریابی پیش‌بینی با تنظیمات پیش‌فرض
     *
     * @param origin      مختصات مبدأ (Lat,Lng)
     * @param destination مختصات مقصد (Lat,Lng)
     * @param routingType "DepartAt" یا "ArriveAt"
     * @param dateTime    زمان با فرمت YYYY-MM-DDThh:mm
     */
    public NeshanPredictiveResponse getPredictiveDirection(
            String origin,
            String destination,
            String routingType,
            String dateTime) {
        return getPredictiveDirection(origin, destination, routingType, dateTime, false, false);
    }
}

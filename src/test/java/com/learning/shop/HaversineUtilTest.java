package com.learning.shop;

import com.learning.shop.service.HaversineUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * تست واحد فرمول Haversine.
 */
class HaversineUtilTest {

    /**
     * فاصله بین دو نقطه‌ی مساوی باید صفر باشد.
     */
    @Test
    void distanceBetweenSamePoints_isZero() {
        double d = HaversineUtil.distanceMeters(35.7208, 51.4323, 35.7208, 51.4323);
        assertEquals(0.0, d, 0.001);
    }

    /**
     * فاصلهٔ بین دو نقطه‌ی خیلی نزدیک باید یک عدد خیلی کوچک غیرصفر باشد.
     */
    @Test
    void distanceBetweenNearPoints_isSmallButPositive() {
        double d = HaversineUtil.distanceMeters(35.7208, 51.4323, 35.7210, 51.4325);
        assertTrue(d > 0);
        assertTrue(d < 100, "دو نقطه‌ی نزدیک باید کمتر از ۱۰۰ متر باشند، اما " + d + " باشد");
    }

    /**
     * فاصله بین دو شهر مشخص (تهران تا کرج) باید یک مقدار معقول (چند ده کیلومتر) باشد.
     * مختصات تقریبی: تهران 35.7000,51.3500 و کرج 35.8287,50.9970
     */
    @Test
    void distanceBetweenTehranAndKaraj_isReasonable() {
        double d = HaversineUtil.distanceMeters(35.7000, 51.3500, 35.8287, 50.9970);
        // حدود ۳۳ کیلومتر با تلورانس
        assertTrue(d > 20000 && d < 50000, "باید حدود ۳۰-۴۰ کیلومتر باشد، اما " + d + " متر بود");
    }
}

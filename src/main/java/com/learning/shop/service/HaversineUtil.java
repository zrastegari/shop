package com.learning.shop.service;

/**
 * متدهای کمکی برای محاسبه‌ی فاصله‌ی جغرافیایی.
 * <p>
 * اینجا از فرمول Haversine استفاده می‌شود که فاصله‌ی «هوایی» (مستقیم)
 * میان دو نقطه روی کره‌ی زمین را بر حسب متر محاسبه می‌کند.
 * هیچ کتابخانه‌ی خارجی لازم نیست.
 */
public final class HaversineUtil {

    /** شعاع کره‌ی زمین بر حسب متر */
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private HaversineUtil() {
        // کلاس utility — نمونه‌ساز نمی‌شود
    }

    /**
     * محاسبه‌ی فاصله‌ی هوایی (متر) بین دو نقطه با مختصات جغرافیایی.
     *
     * @param lat1 عرض جغرافیایی نقطه ۱
     * @param lng1 طول جغرافیایی نقطه ۱
     * @param lat2 عرض جغرافیایی نقطه ۲
     * @param lng2 طول جغرافیایی نقطه ۲
     * @return فاصله بر حسب متر
     */
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}

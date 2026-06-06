package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * پاسخ سرویس Map Matching (نگاشت نقطه بر نقشه)
 *
 * ساختار پاسخ:
 * {
 *   "snappedPoints": [
 *     { "location": [latitude, longitude], "originalIndex": 0 },
 *     ...
 *   ],
 *   "geometry": "encoded_polyline_string"
 * }
 *
 * مستندات: https://platform.neshan.org/docs/api/routing-category/map-matching/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanMapMatchingResponse {

    private List<SnappedPoint> snappedPoints;   // نقاط نگاشت شده روی مسیر واقعی
    private String geometry;                    // Polyline انکود شده کل مسیر

    public List<SnappedPoint> getSnappedPoints() { return snappedPoints; }
    public void setSnappedPoints(List<SnappedPoint> snappedPoints) { this.snappedPoints = snappedPoints; }

    public String getGeometry() { return geometry; }
    public void setGeometry(String geometry) { this.geometry = geometry; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SnappedPoint {
        private List<Double> location;      // [latitude, longitude]
        private int originalIndex;          // اندیس متناظر در نقاط ورودی (شروع از 0)

        public List<Double> getLocation() { return location; }
        public void setLocation(List<Double> location) { this.location = location; }

        public int getOriginalIndex() { return originalIndex; }
        public void setOriginalIndex(int originalIndex) { this.originalIndex = originalIndex; }
    }
}

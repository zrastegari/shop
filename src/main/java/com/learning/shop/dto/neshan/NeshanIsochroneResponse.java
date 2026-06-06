package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * پاسخ سرویس محدوده در دسترس (Isochrone)
 *
 * ساختار پاسخ به صورت GeoJSON FeatureCollection:
 * {
 *   "type": "FeatureCollection",
 *   "features": [
 *     {
 *       "type": "Feature",
 *       "properties": { "metric": "distance" },
 *       "geometry": {
 *         "type": "MultiPolygon",
 *         "coordinates": [ [ [ [lng, lat], [lng, lat], ... ] ] ]
 *       }
 *     }
 *   ]
 * }
 *
 * مستندات: https://platform.neshan.org/docs/api/routing-category/isochrone/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanIsochroneResponse {

    private String type;                    // همیشه "FeatureCollection"
    private List<Feature> features;         // آرایه‌ای از نواحی دسترسی

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String type;                // همیشه "Feature"
        private Properties properties;      // مشخصات شامل metric (distance یا time)
        private Geometry geometry;          // هندسه ناحیه (Polygon یا MultiPolygon)

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Properties getProperties() { return properties; }
        public void setProperties(Properties properties) { this.properties = properties; }

        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String metric;             // "distance" یا "time"

        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private String type;                          // "Polygon" یا "MultiPolygon" یا "LineString"
        private List<List<List<List<Double>>>> coordinates; // ۴ سطح تو رفتگی برای MultiPolygon

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public List<List<List<List<Double>>>> getCoordinates() { return coordinates; }
        public void setCoordinates(List<List<List<List<Double>>>> coordinates) { this.coordinates = coordinates; }
    }
}

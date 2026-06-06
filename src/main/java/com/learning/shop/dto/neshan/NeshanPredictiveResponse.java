package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO مخصوص سرویس مسیریابی پیش‌بینی (Predictive Routing)
 * مستندات: https://platform.neshan.org/docs/api/routing-category/routing-historical/
 *
 * تفاوت‌های کلیدی با سرویس مسیریابی معمولی:
 * - duration و distance مستقیماً در سطح route قرار دارند (برحسب دقیقه و متر)
 * - فیلد geometry مستقیماً در route هست (نه داخل overview_polyline)
 * - فیلد dateTime در سطح route وجود دارد (زمان رسیدن یا حرکت)
 * - استپ‌ها از direction، location (شامل longitude/latitude) استفاده می‌کنند
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanPredictiveResponse {

    private List<Route> routes;

    public List<Route> getRoutes() { return routes; }
    public void setRoutes(List<Route> routes) { this.routes = routes; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        private Double duration;       // دقیقه
        private Double distance;       // متر
        private String geometry;       // Encoded Polyline کل مسیر
        private String dateTime;       // زمان رسیدن یا حرکت
        private List<Leg> legs;

        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }

        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }

        public String getGeometry() { return geometry; }
        public void setGeometry(String geometry) { this.geometry = geometry; }

        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }

        public List<Leg> getLegs() { return legs; }
        public void setLegs(List<Leg> legs) { this.legs = legs; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        private String summary;
        private Double distance;       // متر
        private Double duration;       // دقیقه
        private List<Step> steps;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }

        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }

        public List<Step> getSteps() { return steps; }
        public void setSteps(List<Step> steps) { this.steps = steps; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String direction;      // جهت جغرافیایی (N, E, S, W, ...)
        private String type;           // نوع مانور (depart, turn, rotary, exit rotary, arrive, fork, merge, ...)
        private String modifier;       // جهت تغییر مسیر (straight, right, left, slight right, ...)
        private Location location;     // نقطه شروع گام
        private Double distance;       // متر
        private Double duration;       // دقیقه
        private String name;           // نام معبر
        private String geometry;       // Encoded Polyline این گام
        private Integer exit;          // شماره خروجی (فقط در typeهای مرتبط با میدان)
        private String rotaryName;     // نام میدان (فقط در typeهای مرتبط با میدان)

        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getModifier() { return modifier; }
        public void setModifier(String modifier) { this.modifier = modifier; }

        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }

        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }

        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGeometry() { return geometry; }
        public void setGeometry(String geometry) { this.geometry = geometry; }

        public Integer getExit() { return exit; }
        public void setExit(Integer exit) { this.exit = exit; }

        public String getRotaryName() { return rotaryName; }
        public void setRotaryName(String rotaryName) { this.rotaryName = rotaryName; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double longitude;
        private Double latitude;

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
    }
}

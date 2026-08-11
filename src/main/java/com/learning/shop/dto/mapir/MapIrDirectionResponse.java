package com.learning.shop.dto.mapir;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * پاسخ سرویس مسیریابی (Direction) شرکت مپ (map.ir)
 *
 * این سرویس با دریافت مختصات مبدأ و مقصد (و نقاط میانی اختیاری)، بهترین
 * مسیر را محاسبه کرده و جزئیات کاملی از مسیر شامل معابر، مانورها،
 * مسافت، زمان تخمینی و ... ارائه می‌دهد.
 *
 * مستندات: https://help.map.ir/route-api/
 *
 * ساختار پاسخ بر اساس فرمت OSRM می‌باشد:
 * {
 *   "code": "Ok",
 *   "routes": [ { "geometry": "...", "legs": [...], "distance": ..., "duration": ..., ... } ],
 *   "waypoints": [ { "waypoint_index": 0, "trips_index": 0, "location": [lng, lat], "name": "...", ... }, ... ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapIrDirectionResponse {

    /** کد وضعیت: "Ok" به معنی موفقیت، "NoTrip" در صورت عدم وجود مسیر */
    private String code;

    /** آرایه‌ای از مسیرها (معمولاً یک مسیر، در صورت alternatives=true چند مسیر) */
    @JsonProperty("routes")
    private List<Trip> trips;

    /** آرایه‌ای از نقاط مسیر با اطلاعات هر نقطه */
    private List<Waypoint> waypoints;

    // ============================================================
    //   Getters & Setters
    // ============================================================

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    /**
     * بررسی موفقیت‌آمیز بودن درخواست
     */
    public boolean isSuccess() {
        return "Ok".equalsIgnoreCase(code);
    }

    // ============================================================
    //   کلاس‌های داخلی (Inner Classes)
    // ============================================================

    /**
     * نمایانگر یک Trip (سفر / مسیر)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Trip {

        /** مسیر رمزگذاری شده (Polyline encoded) کل سفر */
        private String geometry;

        /** آرایه‌ای از Legها (بخش‌های مسیر بین هر دو نقطه متوالی) */
        private List<Leg> legs;

        /** کل مسافت طی شده بر حسب متر */
        private Double distance;

        /** کل زمان سفر بر حسب ثانیه */
        private Double duration;

        /** نام وزن‌دهی (معمولاً "routability") */
        @JsonProperty("weight_name")
        private String weightName;

        /** وزن کل مسیر */
        private Double weight;

        // Getters & Setters

        public String getGeometry() {
            return geometry;
        }

        public void setGeometry(String geometry) {
            this.geometry = geometry;
        }

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }

        public String getWeightName() {
            return weightName;
        }

        public void setWeightName(String weightName) {
            this.weightName = weightName;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }
    }

    /**
     * نمایانگر یک Leg (بخش مسیر بین دو نقطه متوالی)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {

        /** آرایه‌ای از Stepها (گام‌های مسیر) */
        private List<Step> steps;

        /** مسافت این بخش بر حسب متر */
        private Double distance;

        /** زمان این بخش بر حسب ثانیه */
        private Double duration;

        /** خلاصه مسیر (نام خیابان‌های اصلی) */
        private String summary;

        /** وزن این بخش */
        private Double weight;

        // Getters & Setters

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(List<Step> steps) {
            this.steps = steps;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }
    }

    /**
     * نمایانگر یک Step (گام مسیر)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {

        /** آرایه‌ای از تقاطع‌ها */
        private List<Intersection> intersections;

        /** طرف رانندگی (معمولاً "right") */
        @JsonProperty("driving_side")
        private String drivingSide;

        /** مسیر رمزگذاری شده این گام */
        private String geometry;

        /** حالت حمل و نقل (معمولاً "driving") */
        private String mode;

        /** زمان این گام بر حسب ثانیه */
        private Double duration;

        /** اطلاعات مانور */
        private Maneuver maneuver;

        /** وزن این گام */
        private Double weight;

        /** مسافت این گام بر حسب متر */
        private Double distance;

        /** نام خیابان */
        private String name;

        /** مقصد (اختیاری) */
        private String destinations;

        // Getters & Setters

        public List<Intersection> getIntersections() {
            return intersections;
        }

        public void setIntersections(List<Intersection> intersections) {
            this.intersections = intersections;
        }

        public String getDrivingSide() {
            return drivingSide;
        }

        public void setDrivingSide(String drivingSide) {
            this.drivingSide = drivingSide;
        }

        public String getGeometry() {
            return geometry;
        }

        public void setGeometry(String geometry) {
            this.geometry = geometry;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }

        public Maneuver getManeuver() {
            return maneuver;
        }

        public void setManeuver(Maneuver maneuver) {
            this.maneuver = maneuver;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDestinations() {
            return destinations;
        }

        public void setDestinations(String destinations) {
            this.destinations = destinations;
        }
    }

    /**
     * نمایانگر یک تقاطع (Intersection)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Intersection {

        /** ایندکس خروجی */
        private Integer out;

        /** آرایه بولی مجاز بودن ورود به هر جهت */
        private List<Boolean> entry;

        /** جهت‌های بلبرینگ موجود */
        private List<Integer> bearings;

        /** مختصات تقاطع [lng, lat] */
        private List<Double> location;

        /** ایندکس ورودی */
        private Integer in;

        // Getters & Setters

        public Integer getOut() { return out; }
        public void setOut(Integer out) { this.out = out; }
        public List<Boolean> getEntry() { return entry; }
        public void setEntry(List<Boolean> entry) { this.entry = entry; }
        public List<Integer> getBearings() { return bearings; }
        public void setBearings(List<Integer> bearings) { this.bearings = bearings; }
        public List<Double> getLocation() { return location; }
        public void setLocation(List<Double> location) { this.location = location; }
        public Integer getIn() { return in; }
        public void setIn(Integer in) { this.in = in; }

        public double getLng() { return location != null && location.size() > 0 ? location.get(0) : 0; }
        public double getLat() { return location != null && location.size() > 1 ? location.get(1) : 0; }
    }

    /**
     * نمایانگر مانور (Maneuver)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Maneuver {

        @JsonProperty("bearing_after")
        private Integer bearingAfter;

        /** نوع مانور: depart, turn, arrive, merge, "end of road", "off ramp", ... */
        private String type;

        /** اصلاح‌کننده: left, right, slight left, slight right, ... */
        private String modifier;

        @JsonProperty("bearing_before")
        private Integer bearingBefore;

        /** مختصات محل مانور [lng, lat] */
        private List<Double> location;

        // Getters & Setters

        public Integer getBearingAfter() { return bearingAfter; }
        public void setBearingAfter(Integer bearingAfter) { this.bearingAfter = bearingAfter; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getModifier() { return modifier; }
        public void setModifier(String modifier) { this.modifier = modifier; }
        public Integer getBearingBefore() { return bearingBefore; }
        public void setBearingBefore(Integer bearingBefore) { this.bearingBefore = bearingBefore; }
        public List<Double> getLocation() { return location; }
        public void setLocation(List<Double> location) { this.location = location; }
        public double getLng() { return location != null && location.size() > 0 ? location.get(0) : 0; }
        public double getLat() { return location != null && location.size() > 1 ? location.get(1) : 0; }
    }

    /**
     * نمایانگر یک Waypoint (نقطه مسیر)
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Waypoint {

        @JsonProperty("waypoint_index")
        private Integer waypointIndex;

        @JsonProperty("trips_index")
        private Integer tripsIndex;

        /** مختصات نقطه [lng, lat] */
        private List<Double> location;

        /** نام مکان */
        private String name;

        /** فاصله (متر) */
        private Double distance;

        /** راهنمای تطبیق مسیر */
        private String hint;

        // Getters & Setters

        public Integer getWaypointIndex() { return waypointIndex; }
        public void setWaypointIndex(Integer waypointIndex) { this.waypointIndex = waypointIndex; }
        public Integer getTripsIndex() { return tripsIndex; }
        public void setTripsIndex(Integer tripsIndex) { this.tripsIndex = tripsIndex; }
        public List<Double> getLocation() { return location; }
        public void setLocation(List<Double> location) { this.location = location; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }
        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }
        public double getLng() { return location != null && location.size() > 0 ? location.get(0) : 0; }
        public double getLat() { return location != null && location.size() > 1 ? location.get(1) : 0; }
    }
}

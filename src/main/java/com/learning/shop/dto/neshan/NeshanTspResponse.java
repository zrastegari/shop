package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * پاسخ سرویس TSP (فروشنده دوره‌گرد / Traveling Salesman Problem)
 *
 * ساختار پاسخ:
 * {
 *   "points": [ { "name": "...", "location": [lng, lat], "index": 0 }, ... ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanTspResponse {

    private List<TspPoint> points;

    public List<TspPoint> getPoints() {
        return points;
    }

    public void setPoints(List<TspPoint> points) {
        this.points = points;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TspPoint {

        private String name;

        @JsonProperty("location")
        private List<Double> location;

        private int index;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Double> getLocation() {
            return location;
        }

        public void setLocation(List<Double> location) {
            this.location = location;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * طول جغرافیایی (longitude) - اولین عنصر location
         */
        public double getLng() {
            return location != null && location.size() > 0 ? location.get(0) : 0;
        }

        /**
         * عرض جغرافیایی (latitude) - دومین عنصر location
         */
        public double getLat() {
            return location != null && location.size() > 1 ? location.get(1) : 0;
        }
    }
}

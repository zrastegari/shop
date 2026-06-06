package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * پاسخ سرویس ماتریس فاصله (Distance Matrix)
 *
 * ساختار پاسخ:
 * {
 *   "rows": [
 *     {
 *       "elements": [
 *         {
 *           "distance": { "value": 574.0, "text": "۵۷۵ متر" },
 *           "duration": { "value": 392.0, "text": "۷ دقیقه" },
 *           "status": "OK"
 *         }
 *       ]
 *     }
 *   ],
 *   "origin_addresses": ["تهران، ..."],
 *   "destination_addresses": ["تهران، ..."]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanDistanceMatrixResponse {

    private List<MatrixRow> rows;
    private List<String> origin_addresses;
    private List<String> destination_addresses;

    public List<MatrixRow> getRows() {
        return rows;
    }

    public void setRows(List<MatrixRow> rows) {
        this.rows = rows;
    }

    public List<String> getOrigin_addresses() {
        return origin_addresses;
    }

    public void setOrigin_addresses(List<String> origin_addresses) {
        this.origin_addresses = origin_addresses;
    }

    public List<String> getDestination_addresses() {
        return destination_addresses;
    }

    public void setDestination_addresses(List<String> destination_addresses) {
        this.destination_addresses = destination_addresses;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatrixRow {
        private List<MatrixElement> elements;

        public List<MatrixElement> getElements() {
            return elements;
        }

        public void setElements(List<MatrixElement> elements) {
            this.elements = elements;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatrixElement {
        private DistanceDuration distance;
        private DistanceDuration duration;
        private String status;

        public DistanceDuration getDistance() {
            return distance;
        }

        public void setDistance(DistanceDuration distance) {
            this.distance = distance;
        }

        public DistanceDuration getDuration() {
            return duration;
        }

        public void setDuration(DistanceDuration duration) {
            this.duration = duration;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DistanceDuration {
        private double value;
        private String text;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}

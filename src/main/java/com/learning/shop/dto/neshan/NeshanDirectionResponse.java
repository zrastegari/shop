package com.learning.shop.dto.neshan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeshanDirectionResponse {

    private List<Route> routes;
    public List<Route> getRoutes() { return routes; }
    public void setRoutes(List<Route> routes) { this.routes = routes; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        @JsonProperty("overview_polyline")
        private OverviewPolyline overviewPolyline;
        private List<Leg> legs;

        public OverviewPolyline getOverviewPolyline() { return overviewPolyline; }
        public void setOverviewPolyline(OverviewPolyline overviewPolyline) { this.overviewPolyline = overviewPolyline; }

        public List<Leg> getLegs() { return legs; }
        public void setLegs(List<Leg> legs) { this.legs = legs; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverviewPolyline {
        private String points;

        public String getPoints() { return points; }
        public void setPoints(String points) { this.points = points; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        private String summary;
        private DistanceDuration distance;
        private DistanceDuration duration;
        private List<Step> steps;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public DistanceDuration getDistance() { return distance; }
        public void setDistance(DistanceDuration distance) { this.distance = distance; }

        public DistanceDuration getDuration() { return duration; }
        public void setDuration(DistanceDuration duration) { this.duration = duration; }

        public List<Step> getSteps() { return steps; }
        public void setSteps(List<Step> steps) { this.steps = steps; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DistanceDuration {
        private double value;
        private String text;

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String name;
        private String instruction;
        @JsonProperty("bearing_after")
        private Integer bearingAfter;
        private String type;
        private String modifier;
        private Integer exit;
        private DistanceDuration distance;
        private DistanceDuration duration;
        private String polyline;
        @JsonProperty("start_location")
        private List<Double> startLocation;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }

        public Integer getBearingAfter() { return bearingAfter; }
        public void setBearingAfter(Integer bearingAfter) { this.bearingAfter = bearingAfter; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getModifier() { return modifier; }
        public void setModifier(String modifier) { this.modifier = modifier; }

        public Integer getExit() { return exit; }
        public void setExit(Integer exit) { this.exit = exit; }

        public DistanceDuration getDistance() { return distance; }
        public void setDistance(DistanceDuration distance) { this.distance = distance; }

        public DistanceDuration getDuration() { return duration; }
        public void setDuration(DistanceDuration duration) { this.duration = duration; }

        public String getPolyline() { return polyline; }
        public void setPolyline(String polyline) { this.polyline = polyline; }

        public List<Double> getStartLocation() { return startLocation; }
        public void setStartLocation(List<Double> startLocation) { this.startLocation = startLocation; }
    }
}

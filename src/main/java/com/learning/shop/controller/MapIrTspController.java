package com.learning.shop.controller;

import com.learning.shop.dto.mapir.MapIrDirectionResponse;
import com.learning.shop.dto.mapir.MapIrTspResponse;
import com.learning.shop.service.MapRoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map.ir")
public class MapIrTspController {

    private final MapRoutingService mapRoutingService;

    public MapIrTspController(MapRoutingService mapRoutingService) {
        this.mapRoutingService = mapRoutingService;
    }

    @GetMapping("/route")
    public ResponseEntity<MapIrDirectionResponse> getRoute(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String geometries) {
        return ResponseEntity.ok(mapRoutingService.getRoute(
                coordinates, alternatives, steps, overview, geometries));
    }

    @GetMapping("/route/tarh")
    public ResponseEntity<MapIrDirectionResponse> getRouteWithTarh(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String geometries) {
        return ResponseEntity.ok(mapRoutingService.getRouteWithTarh(
                coordinates, alternatives, steps, overview, geometries));
    }

    @GetMapping("/route/zojofard")
    public ResponseEntity<MapIrDirectionResponse> getRouteWithZojofard(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String geometries) {
        return ResponseEntity.ok(mapRoutingService.getRouteWithZojofard(
                coordinates, alternatives, steps, overview, geometries));
    }

    @GetMapping("/route/bicycle")
    public ResponseEntity<MapIrDirectionResponse> getBicycleRoute(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String geometries) {
        return ResponseEntity.ok(mapRoutingService.getBicycleRoute(
                coordinates, alternatives, steps, overview, geometries));
    }

    @GetMapping("/route/foot")
    public ResponseEntity<MapIrDirectionResponse> getFootRoute(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean alternatives,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String overview,
            @RequestParam(required = false) String geometries) {
        return ResponseEntity.ok(mapRoutingService.getFootRoute(
                coordinates, alternatives, steps, overview, geometries));
    }

    @GetMapping("/tsp")
    public ResponseEntity<MapIrTspResponse> getTsp(
            @RequestParam String coordinates,
            @RequestParam(required = false) Boolean roundtrip,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Boolean steps,
            @RequestParam(required = false) String annotations,
            @RequestParam(required = false) String geometries,
            @RequestParam(required = false) String overview) {
        return ResponseEntity.ok(mapRoutingService.getTsp(
                coordinates, roundtrip, source, destination,
                steps, annotations, geometries, overview));
    }
}
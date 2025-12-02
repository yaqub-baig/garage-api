package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.RevenueSummaryDTO;
import com.autoadda.apis.garage.service.GarageAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@CrossOrigin("*")
@RestController
@RequestMapping("/garage-analytics")
public class GarageAnalyticsController {

    private final GarageAnalyticsService analyticsService;

    public GarageAnalyticsController(GarageAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }


    @GetMapping("/summary/weekly")
    public ResponseEntity<RevenueSummaryDTO> getWeeklySummary() {
        return ResponseEntity.ok ( analyticsService.getWeeklySummary ( ) );
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<RevenueSummaryDTO> getMonthlySummary() {
        return ResponseEntity.ok ( analyticsService.getWeeklySummary ( ) );
    }


}

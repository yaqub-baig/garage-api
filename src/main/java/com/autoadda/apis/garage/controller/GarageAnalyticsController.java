package com.autoadda.apis.garage.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.request.RevenueSummaryDTO;
import com.autoadda.apis.garage.service.GarageAnalyticsService;

@RestController
@RequestMapping("/garage-analytics")
public class GarageAnalyticsController {

	private final GarageAnalyticsService analyticsService;

	public GarageAnalyticsController(GarageAnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

//	@GetMapping("/{garageId}/today")
//	public ResponseEntity<GarageAnalyticsDTO> getTodayStats(@PathVariable Integer garageId) {
//		return ResponseEntity.ok(analyticsService.getTodayAnalytics(garageId));
//	}

	@GetMapping("/summary/weekly")
	public ResponseEntity<RevenueSummaryDTO> getWeeklySummary() {
		return ResponseEntity.ok(analyticsService.getWeeklySummary());
	}

	@GetMapping("/summary/monthly")
	public ResponseEntity<RevenueSummaryDTO> getMonthlySummary() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime monthAgo = now.minusMonths(1);
		return ResponseEntity.ok(analyticsService.getWeeklySummary());
	}

//	@GetMapping("/technician")
//	public ResponseEntity<List<TechnicianSummaryDTO>> getTechnicianBreakdown(
//			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
//			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
//		return ResponseEntity.ok(analyticsService.getTechnicianBreakdown(from, to));
//	}
}

package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.AddWorkingToolRequest;
import com.autoadda.apis.garage.request.UpdateWorkingToolRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.WorkingToolResponse;
import com.autoadda.apis.garage.service.WorkingToolsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/working-tools")
@RequiredArgsConstructor
@Slf4j
public class WorkingToolsController {

    private final WorkingToolsService workingToolsService;

    /**
     * Add a new tool/equipment entry to a garage
     * POST /working-tools
     */
    @PostMapping
    public ResponseEntity<GenericResponse> addTool(
            @RequestBody AddWorkingToolRequest request) {
        log.info("Adding working tool for garage: {}", request.getGarageId());
        WorkingToolResponse response = workingToolsService.addTool(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Working tool added successfully", response));
    }

    /**
     * Get all tools for a garage
     * GET /working-tools/garage/{garageId}
     */
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getGarageTools(
            @PathVariable Integer garageId) {
        log.info("Fetching working tools for garage: {}", garageId);
        List<WorkingToolResponse> responses = workingToolsService.getGarageTools(garageId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Working tools retrieved successfully", responses));
    }

    /**
     * Get a specific tool by ID
     * GET /working-tools/{toolId}
     */
    @GetMapping("/{toolId}")
    public ResponseEntity<GenericResponse> getTool(
            @PathVariable Integer toolId) {
        log.info("Fetching working tool: {}", toolId);
        WorkingToolResponse response = workingToolsService.getTool(toolId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Working tool retrieved successfully", response));
    }

    /**
     * Update tool details
     * PUT /working-tools/{toolId}
     */
    @PutMapping("/{toolId}")
    public ResponseEntity<GenericResponse> updateTool(
            @PathVariable Integer toolId,
            @RequestBody UpdateWorkingToolRequest request) {
        log.info("Updating working tool: {}", toolId);
        WorkingToolResponse response = workingToolsService.updateTool(toolId, request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Working tool updated successfully", response));
    }

    /**
     * Search tools for a garage
     * GET /working-tools/garage/{garageId}/search?q={searchTerm}
     */
    @GetMapping("/garage/{garageId}/search")
    public ResponseEntity<GenericResponse> searchTools(
            @PathVariable Integer garageId,
            @RequestParam String q) {
        log.info("Searching working tools for garage: {} with term: {}", garageId, q);
        List<WorkingToolResponse> responses = workingToolsService.searchTools(garageId, q);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Search completed successfully", responses));
    }

    /**
     * Deactivate a tool (soft delete)
     * DELETE /working-tools/{toolId}
     */
    @DeleteMapping("/{toolId}")
    public ResponseEntity<GenericResponse> deactivateTool(
            @PathVariable Integer toolId) {
        log.info("Deactivating working tool: {}", toolId);
        workingToolsService.deactivateTool(toolId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Working tool deactivated successfully", null));
    }
}

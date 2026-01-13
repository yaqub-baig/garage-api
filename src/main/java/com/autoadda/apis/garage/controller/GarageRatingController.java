package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.CreateGarageRatingRequest;
import com.autoadda.apis.garage.request.GarageRatingResponseRequest;
import com.autoadda.apis.garage.request.UpdateGarageRatingRequest;
import com.autoadda.apis.garage.response.CanRateResponse;
import com.autoadda.apis.garage.response.GarageRatingDto;
import com.autoadda.apis.garage.response.GarageRatingSummaryResponse;
import com.autoadda.apis.garage.service.GarageRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@Tag(name = "Garage Ratings", description = "APIs for managing garage ratings and reviews")
public class GarageRatingController {

    private final GarageRatingService ratingService;

    @Operation(summary = "Create a new rating", description = "Create a rating for a completed work order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rating created successfully",
                    content = @Content(schema = @Schema(implementation = GarageRatingDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or rating already exists"),
            @ApiResponse(responseCode = "401", description = "Not authorized to rate this work order"),
            @ApiResponse(responseCode = "404", description = "Work order not found")
    })
    @PostMapping
    public ResponseEntity<GarageRatingDto> createRating(
            @Valid @RequestBody CreateGarageRatingRequest request,
            @RequestHeader("X-Auto-Adda-Id") Integer customerId) {
        GarageRatingDto rating = ratingService.createRating(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @Operation(summary = "Update a rating", description = "Update an existing rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authorized to update this rating"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    @PutMapping("/{ratingId}")
    public ResponseEntity<GarageRatingDto> updateRating(
            @PathVariable Integer ratingId,
            @Valid @RequestBody UpdateGarageRatingRequest request,
            @RequestHeader("X-Auto-Adda-Id") Integer customerId) {
        GarageRatingDto rating = ratingService.updateRating(ratingId, request, customerId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Delete a rating", description = "Delete an existing rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rating deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authorized to delete this rating"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Integer ratingId,
            @RequestHeader("X-Auto-Adda-Id") Integer customerId) {
        ratingService.deleteRating(ratingId, customerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get rating by ID", description = "Get a specific rating by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating found"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    @GetMapping("/{ratingId}")
    public ResponseEntity<GarageRatingDto> getRatingById(@PathVariable Integer ratingId) {
        GarageRatingDto rating = ratingService.getRatingById(ratingId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get rating by work order", description = "Get rating for a specific work order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating found"),
            @ApiResponse(responseCode = "404", description = "Rating not found for this work order")
    })
    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<GarageRatingDto> getRatingByWorkOrderId(@PathVariable Integer workOrderId) {
        GarageRatingDto rating = ratingService.getRatingByWorkOrderId(workOrderId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Get garage ratings", description = "Get all ratings for a garage with optional star filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Garage not found")
    })
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<Page<GarageRatingDto>> getGarageRatings(
            @PathVariable Integer garageId,
            @Parameter(description = "Filter by star rating (1-5)") @RequestParam(required = false) Integer stars,
            @PageableDefault(size = 10, sort = "createdAt", page = 0) Pageable pageable) {
        Page<GarageRatingDto> ratings = ratingService.getGarageRatings(garageId, stars, pageable);
        return ResponseEntity.ok(ratings);
    }

    @Operation(summary = "Get garage rating summary", description = "Get rating statistics for a garage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GarageRatingSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Garage not found")
    })
    @GetMapping("/garage/{garageId}/summary")
    public ResponseEntity<GarageRatingSummaryResponse> getGarageSummary(@PathVariable Integer garageId) {
        GarageRatingSummaryResponse summary = ratingService.getGarageSummary(garageId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get customer ratings", description = "Get all ratings submitted by the customer")
    @ApiResponse(responseCode = "200", description = "Ratings retrieved successfully")
    @GetMapping("/customer")
    public ResponseEntity<List<GarageRatingDto>> getCustomerRatings(
            @RequestHeader("X-Auto-Adda-Id") Integer customerId) {
        List<GarageRatingDto> ratings = ratingService.getCustomerRatings(customerId);
        return ResponseEntity.ok(ratings);
    }

    @Operation(summary = "Check if can rate", description = "Check if customer can rate a work order")
    @ApiResponse(responseCode = "200", description = "Eligibility check completed",
            content = @Content(schema = @Schema(implementation = CanRateResponse.class)))
    @GetMapping("/can-rate/{workOrderId}")
    public ResponseEntity<CanRateResponse> canRate(
            @PathVariable Integer workOrderId,
            @RequestHeader("X-Auto-Adda-Id") Integer customerId) {
        CanRateResponse response = ratingService.canRate(workOrderId, customerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add owner response", description = "Add a response to a rating (garage owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Response added successfully"),
            @ApiResponse(responseCode = "400", description = "Response already exists"),
            @ApiResponse(responseCode = "401", description = "Not authorized to respond"),
            @ApiResponse(responseCode = "404", description = "Rating not found")
    })
    @PostMapping("/response")
    public ResponseEntity<GarageRatingDto> addOwnerResponse(
            @Valid @RequestBody GarageRatingResponseRequest request,
            @RequestHeader("X-Auto-Adda-Id") Integer ownerId) {
        GarageRatingDto rating = ratingService.addOwnerResponse(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }

    @Operation(summary = "Update owner response", description = "Update an existing response (garage owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response updated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authorized to update"),
            @ApiResponse(responseCode = "404", description = "Response not found")
    })
    @PutMapping("/response/{responseId}")
    public ResponseEntity<GarageRatingDto> updateOwnerResponse(
            @PathVariable Integer responseId,
            @RequestParam String responseText,
            @RequestHeader("X-Auto-Adda-Id") Integer ownerId) {
        GarageRatingDto rating = ratingService.updateOwnerResponse(responseId, responseText, ownerId);
        return ResponseEntity.ok(rating);
    }

    @Operation(summary = "Delete owner response", description = "Delete an existing response (garage owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Response deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authorized to delete"),
            @ApiResponse(responseCode = "404", description = "Response not found")
    })
    @DeleteMapping("/response/{responseId}")
    public ResponseEntity<Void> deleteOwnerResponse(
            @PathVariable Integer responseId,
            @RequestHeader("X-Auto-Adda-Id") Integer ownerId) {
        ratingService.deleteOwnerResponse(responseId, ownerId);
        return ResponseEntity.noContent().build();
    }
}

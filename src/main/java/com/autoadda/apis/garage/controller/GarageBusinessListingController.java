package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.AddGarageBusinessListingRequest;
import com.autoadda.apis.garage.request.FinalizeGarageSaleRequest;
import com.autoadda.apis.garage.request.UpdateGarageBusinessListingRequest;
import com.autoadda.apis.garage.response.GarageBusinessListingResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleInvoiceResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.PagedResponse;
import com.autoadda.apis.garage.service.GarageBusinessListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garage-business-listings")
@RequiredArgsConstructor
@Slf4j
public class GarageBusinessListingController {

    private final GarageBusinessListingService listingService;

    /**
     * Create a new garage business-for-sale listing
     * POST /garage-business-listings
     */
    @PostMapping
    public ResponseEntity<GenericResponse> createListing(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody AddGarageBusinessListingRequest request) {
        log.info("Creating garage business listing for garage: {}", request.getGarageId());
        GarageBusinessListingResponse response = listingService.createListing(autoAddaId, request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garage business listing created successfully", response));
    }

    /**
     * Get a specific listing by ID
     * GET /garage-business-listings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getListing(@PathVariable Integer id) {
        GarageBusinessListingResponse response = listingService.getListing(id);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listing retrieved successfully", response));
    }

    /**
     * Get all listings (any status) for a garage
     * GET /garage-business-listings/garage/{garageId}
     */
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getListingsForGarage(@PathVariable Integer garageId) {
        List<GarageBusinessListingResponse> responses = listingService.getListingsForGarage(garageId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listings retrieved successfully", responses));
    }

    /**
     * Browse active listings (public marketplace), paginated
     * GET /garage-business-listings?page=&size=&sortBy=&sortDir=
     */
    @GetMapping
    public ResponseEntity<GenericResponse> browseActiveListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<GarageBusinessListingResponse> response =
                listingService.browseActiveListings(page, size, sortBy, sortDir);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listings retrieved successfully", response));
    }

    /**
     * Search active listings by keyword and price range, paginated
     * GET /garage-business-listings/search?q=&minPrice=&maxPrice=&page=&size=
     */
    @GetMapping("/search")
    public ResponseEntity<GenericResponse> searchListings(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<GarageBusinessListingResponse> response =
                listingService.searchListings(q, minPrice, maxPrice, page, size, sortBy, sortDir);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Search completed successfully", response));
    }

    /**
     * Update listing details
     * PUT /garage-business-listings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse> updateListing(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody UpdateGarageBusinessListingRequest request) {
        GarageBusinessListingResponse response = listingService.updateListing(id, autoAddaId, request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listing updated successfully", response));
    }

    /**
     * Mark listing as under negotiation
     * PUT /garage-business-listings/{id}/negotiation
     */
    @PutMapping("/{id}/negotiation")
    public ResponseEntity<GenericResponse> markUnderNegotiation(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        GarageBusinessListingResponse response = listingService.markUnderNegotiation(id, autoAddaId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listing marked as under negotiation", response));
    }

    /**
     * Reactivate a listing (back to ACTIVE)
     * PUT /garage-business-listings/{id}/reactivate
     */
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<GenericResponse> reactivateListing(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        GarageBusinessListingResponse response = listingService.reactivateListing(id, autoAddaId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listing reactivated successfully", response));
    }

    /**
     * Withdraw a listing from sale
     * PUT /garage-business-listings/{id}/withdraw
     */
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<GenericResponse> withdrawListing(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        GarageBusinessListingResponse response = listingService.withdrawListing(id, autoAddaId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Listing withdrawn successfully", response));
    }

    /**
     * Finalize a sale for a listing
     * POST /garage-business-listings/{id}/finalize-sale
     */
    @PostMapping("/{id}/finalize-sale")
    public ResponseEntity<GenericResponse> finalizeSale(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody FinalizeGarageSaleRequest request) {
        GarageBusinessSaleResponse response = listingService.finalizeSale(id, autoAddaId, request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Sale finalized successfully", response));
    }

    /**
     * Get a sale by ID
     * GET /garage-business-listings/sales/{saleId}
     */
    @GetMapping("/sales/{saleId}")
    public ResponseEntity<GenericResponse> getSale(@PathVariable Integer saleId) {
        GarageBusinessSaleResponse response = listingService.getSale(saleId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Sale retrieved successfully", response));
    }

    /**
     * Generate an invoice for a sale
     * POST /garage-business-listings/sales/{saleId}/invoice
     */
    @PostMapping("/sales/{saleId}/invoice")
    public ResponseEntity<GenericResponse> generateInvoice(@PathVariable Integer saleId) {
        GarageBusinessSaleInvoiceResponse response = listingService.generateInvoice(saleId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Invoice generated successfully", response));
    }

    /**
     * Get the invoice for a sale
     * GET /garage-business-listings/sales/{saleId}/invoice
     */
    @GetMapping("/sales/{saleId}/invoice")
    public ResponseEntity<GenericResponse> getInvoiceForSale(@PathVariable Integer saleId) {
        GarageBusinessSaleInvoiceResponse response = listingService.getInvoiceForSale(saleId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Invoice retrieved successfully", response));
    }
}

package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.MarketplaceProcurementRequest;
import com.autoadda.apis.garage.response.GaragePurchasingAccessResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.PartPurchaseContextResponse;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.service.GarageResolver;
import com.autoadda.apis.garage.service.PartsPurchasingService;
import com.autoadda.apis.garage.util.GaragePermission;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Buying parts from AutoAdda shops in the garage's name. Called by part-api with the user's X-Auto-Adda-Id;
 * every call needs PARTS_PURCHASE in the garage (the owner always has it).
 */
@RestController
@RequiredArgsConstructor
public class PartsPurchasingController {

    private static final String HEADER = "X-Auto-Adda-Id";

    private final PartsPurchasingService purchasingService;
    private final GarageAccessGuard guard;
    private final GarageResolver garageResolver;

    @Operation(summary = "The garage as a parts buyer (name, phone, delivery address); 403 without PARTS_PURCHASE")
    @GetMapping("/garages/{garageId}/purchasing-access")
    public ResponseEntity<GenericResponse> purchasingAccess(@PathVariable Integer garageId,
                                                            @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageId, GaragePermission.PARTS_PURCHASE);
        GaragePurchasingAccessResponse body = purchasingService.purchasingAccess(garageId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(body));
    }

    @Operation(summary = "A part request item with its job card, work order and vehicle, for ordering it")
    @GetMapping("/part-requests/item/{itemId}/purchase-context")
    public ResponseEntity<GenericResponse> purchaseContext(@PathVariable Integer itemId,
                                                           @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageResolver.ofPartRequestItem(itemId), GaragePermission.PARTS_PURCHASE);
        PartPurchaseContextResponse body = purchasingService.context(itemId);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(body));
    }

    @Operation(summary = "Marketplace order progress for a part request item: ORDERED, CANCELLED or RECEIVED")
    @PostMapping("/part-requests/item/{itemId}/marketplace")
    public ResponseEntity<GenericResponse> marketplace(@PathVariable Integer itemId,
                                                       @RequestBody MarketplaceProcurementRequest request,
                                                       @RequestHeader(value = HEADER, required = false) String caller) {
        guard.require(caller, garageResolver.ofPartRequestItem(itemId), GaragePermission.PARTS_PURCHASE);
        PartPurchaseContextResponse body = purchasingService.marketplaceUpdate(itemId, request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(body));
    }
}

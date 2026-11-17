package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageProcurement;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.PartRequest;
import com.autoadda.apis.garage.entity.PartRequestItem;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageProcurementRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.PartRequestItemRepository;
import com.autoadda.apis.garage.repository.PartRequestRepository;
import com.autoadda.apis.garage.request.GarageProcurementRequest;
import com.autoadda.apis.garage.request.MarketplaceProcurementRequest;
import com.autoadda.apis.garage.response.GaragePurchasingAccessResponse;
import com.autoadda.apis.garage.response.PartPurchaseContextResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import com.autoadda.apis.garage.util.ItemProcurementStatus;
import com.autoadda.apis.garage.util.PartRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The garage side of buying parts from AutoAdda shops (orders and enquiries live in part-api).
 * part-api calls these with the user's X-Auto-Adda-Id; the controller checks PARTS_PURCHASE first.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartsPurchasingService {

    /** The customer agreed that the garage buys the part. */
    private static final Set<PartRequestStatus> GARAGE_BUYS =
            EnumSet.of(PartRequestStatus.GARAGE_WILL_PROCURE, PartRequestStatus.GARAGE_PROCURING);

    private final GarageRepository garageRepository;
    private final PartRequestItemRepository itemRepository;
    private final PartRequestRepository partRequestRepository;
    private final GarageProcurementRepository procurementRepository;
    private final PartRequestService partRequestService;
    private final VehicleClient vehicleClient;

    /** The garage as a buyer. 409 unless it is approved and active. */
    @Transactional(readOnly = true)
    public GaragePurchasingAccessResponse purchasingAccess(Integer garageId) {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
        if (garage.getAccountStatus() != GarageVerificationStatus.APPROVED || !garage.isActive()) {
            throw new ConflictException("Only approved, active garages can buy parts");
        }
        Location location = garage.getGarageLocation();
        return GaragePurchasingAccessResponse.builder()
                .garageId(garage.getId())
                .name(garage.getName())
                .phone(firstNonBlank(garage.getMobileNumber(), garage.getPhoneNumber()))
                .email(garage.getEmail())
                .trnNumber(garage.getTrnNumber())
                .address(location == null ? null : joinNonBlank(location.getStreetAddress1(),
                        location.getStreetAddress2(), location.getArea(), location.getCity(), location.getState()))
                .city(location == null ? null : location.getCity())
                .mapLink(location == null ? null : location.getGmap())
                .build();
    }

    /** A part request item with its job and car. */
    @Transactional(readOnly = true)
    public PartPurchaseContextResponse context(Integer itemId) {
        PartRequestItem item = item(itemId);
        PartRequest request = item.getPartRequest();
        JobCard jobCard = request.getJobCard();
        PartPurchaseContextResponse.PartPurchaseContextResponseBuilder response = PartPurchaseContextResponse.builder()
                .garageId(jobCard.getGarage().getId())
                .workOrderId(jobCard.getWorkOrder() == null ? null : jobCard.getWorkOrder().getId())
                .jobCardId(jobCard.getId())
                .partRequestId(request.getId())
                .itemId(item.getId())
                .partName(item.getPartName())
                .partNumber(item.getPartNumber())
                .quantity(item.getQuantity())
                .description(item.getDescription())
                .partRequestStatus(request.getStatus())
                .procurementStatus(item.getProcurementStatus())
                .purchasable(purchasable(item))
                .marketplaceOrderId(item.getMarketplaceOrderId())
                .marketplaceOrderItemId(item.getMarketplaceOrderItemId())
                .vehicleId(request.getVehicleId());
        try {
            VehicleDetailResponse vehicle = request.getVehicleId() == null ? null
                    : vehicleClient.getVehicleDetailsByIdResponse(request.getVehicleId());
            if (vehicle != null) {
                response.brand(vehicle.getBrand()).model(vehicle.getModel()).year(vehicle.getYear());
            }
        } catch (RuntimeException e) {
            log.warn("Vehicle {} lookup failed for part request item {}: {}", request.getVehicleId(), itemId, e.getMessage());
        }
        return response.build();
    }

    /** Applies a marketplace order step to the item. Repeating the same step for the same order item is a no-op. */
    @Transactional
    public PartPurchaseContextResponse marketplaceUpdate(Integer itemId, MarketplaceProcurementRequest request) {
        if (request.getStage() == null || request.getOrderItemId() == null) {
            throw new BadRequestException("stage and orderItemId are required");
        }
        PartRequestItem item = item(itemId);
        switch (request.getStage()) {
            case ORDERED -> markOrdered(item, request);
            case CANCELLED -> markCancelled(item, request);
            case RECEIVED -> markReceived(item, request);
        }
        return context(itemId);
    }

    private void markOrdered(PartRequestItem item, MarketplaceProcurementRequest request) {
        if (item.getProcurementStatus() == ItemProcurementStatus.ORDERED
                && request.getOrderItemId().equals(item.getMarketplaceOrderItemId())) {
            return;
        }
        if (!purchasable(item)) {
            throw new ConflictException("This part can't be ordered: it is " + item.getProcurementStatus()
                    + " and the request is " + item.getPartRequest().getStatus());
        }
        item.setProcurementStatus(ItemProcurementStatus.ORDERED);
        item.setMarketplaceOrderId(request.getOrderId());
        item.setMarketplaceOrderItemId(request.getOrderItemId());
        item.setMarketplaceSupplierName(request.getSupplierName());
        itemRepository.save(item);

        PartRequest partRequest = item.getPartRequest();
        if (partRequest.getStatus() == PartRequestStatus.GARAGE_WILL_PROCURE) {
            partRequest.updateStatus(PartRequestStatus.GARAGE_PROCURING);
            partRequestRepository.save(partRequest);
        }
        log.info("Part request item {} ordered via marketplace order {}", item.getId(), request.getOrderId());
    }

    private void markCancelled(PartRequestItem item, MarketplaceProcurementRequest request) {
        if (item.getProcurementStatus() != ItemProcurementStatus.ORDERED
                || !request.getOrderItemId().equals(item.getMarketplaceOrderItemId())) {
            return; // not ordered through this order item (any more)
        }
        item.setProcurementStatus(ItemProcurementStatus.PENDING_PROCUREMENT);
        item.setMarketplaceOrderId(null);
        item.setMarketplaceOrderItemId(null);
        item.setMarketplaceSupplierName(null);
        itemRepository.save(item);
        log.info("Part request item {} back to pending: marketplace order {} cancelled", item.getId(), request.getOrderId());
    }

    private void markReceived(PartRequestItem item, MarketplaceProcurementRequest request) {
        GarageProcurement existing = procurementRepository.findByPartRequestItemId(item.getId()).orElse(null);
        if (existing != null) {
            if (request.getOrderItemId().equals(existing.getMarketplaceOrderItemId())) {
                return;
            }
            throw new ConflictException("This part was already procured elsewhere");
        }
        boolean orderedHere = item.getProcurementStatus() == ItemProcurementStatus.ORDERED
                && request.getOrderItemId().equals(item.getMarketplaceOrderItemId());
        if (!orderedHere && !purchasable(item)) {
            throw new ConflictException("This part can't be received as procured: it is " + item.getProcurementStatus());
        }
        if (request.getUnitPrice() == null || request.getUnitPrice() < 0 || request.getCondition() == null
                || request.getSupplierName() == null || request.getSupplierName().isBlank()) {
            throw new BadRequestException("supplierName, unitPrice and condition are required");
        }
        // Same path as a manual procurement: creates the record, marks the item PROCURED, updates the invoice
        partRequestService.addGarageProcurement(GarageProcurementRequest.builder()
                .partRequestItemId(item.getId())
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .purchasePrice(request.getUnitPrice())
                .partCondition(request.getCondition())
                .receiptNumber("AutoAdda order #" + request.getOrderId())
                .notes("Bought on AutoAdda (order #" + request.getOrderId() + ")")
                .build());
        procurementRepository.findByPartRequestItemId(item.getId()).ifPresent(p -> {
            p.setMarketplaceOrderItemId(request.getOrderItemId());
            procurementRepository.save(p);
        });
        PartRequestItem saved = item(item.getId());
        saved.setReceivedAt(LocalDateTime.now());
        saved.setMarketplaceOrderId(request.getOrderId());
        saved.setMarketplaceOrderItemId(request.getOrderItemId());
        saved.setMarketplaceSupplierName(request.getSupplierName());
        itemRepository.save(saved);
        log.info("Part request item {} received from marketplace order {}", item.getId(), request.getOrderId());
    }

    private static boolean purchasable(PartRequestItem item) {
        return GARAGE_BUYS.contains(item.getPartRequest().getStatus())
                && (item.getProcurementStatus() == null
                || item.getProcurementStatus() == ItemProcurementStatus.PENDING_PROCUREMENT);
    }

    private PartRequestItem item(Integer itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Part request item not found: " + itemId));
    }

    private static String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a : b;
    }

    private static String joinNonBlank(String... parts) {
        String joined = Stream.of(parts).filter(Objects::nonNull).map(String::trim).filter(p -> !p.isEmpty())
                .collect(Collectors.joining(", "));
        return joined.isEmpty() ? null : joined;
    }
}

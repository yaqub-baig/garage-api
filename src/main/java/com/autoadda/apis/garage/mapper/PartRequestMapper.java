package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.CustomerPartResponse;
import com.autoadda.apis.garage.entity.GarageProcurement;
import com.autoadda.apis.garage.entity.PartRequest;
import com.autoadda.apis.garage.entity.PartRequestItem;
import com.autoadda.apis.garage.repository.PartRequestItemRepository;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.CustomerChoice;
import com.autoadda.apis.garage.util.PartRequestStatus;
import com.autoadda.apis.garage.util.VoiceNoteHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Part request → response, shared by /part-requests and the work-order responses.
 */
@Component
@RequiredArgsConstructor
public class PartRequestMapper {

    public static final String LEVEL_REQUEST = "REQUEST";
    public static final String LEVEL_ITEM = "ITEM";
    public static final String PART_PROCURED = "PART_PROCURED";
    public static final String PART_RECEIVED_FROM_CUSTOMER = "PART_RECEIVED_FROM_CUSTOMER";
    public static final String PART_ADDED_TO_INVOICE = "PART_ADDED_TO_INVOICE";

    private final PartRequestItemRepository partRequestItemRepo;
    private final VoiceNoteHelper voiceNoteHelper;

    /**
     * @param vehicleDetails short vehicle label ("Toyota Camry - A 12345"), or null when the caller already
     *                       has the vehicle (e.g. inside a work order)
     */
    public PartRequestResponse toResponse(PartRequest partRequest, String vehicleDetails) {
        // Items are read from the repository: right after creation they are not yet in partRequest.getItems().
        List<PartRequestItem> items = partRequestItemRepo.findByPartRequestId(partRequest.getId());

        return PartRequestResponse.builder()
                .id(partRequest.getId())
                .jobCardId(partRequest.getJobCard().getId())
                .jobCardServiceName(serviceName(partRequest))
                .requestedByTechnicianId(partRequest.getRequestedBy().getId())
                .requestedByTechnicianName(partRequest.getRequestedBy().getFirstName())
                .status(partRequest.getStatus())
                .requestDate(partRequest.getRequestDate())
                .customerResponseDate(partRequest.getCustomerResponseDate())
                .customerId(partRequest.getCustomerId())
                .vehicleId(partRequest.getVehicleId())
                .vehicleDetails(vehicleDetails)
                .items(items.stream().map(this::toItemResponse).toList())
                .customerResponse(partRequest.getCustomerResponse() != null
                        ? toCustomerResponse(partRequest.getCustomerResponse()) : null)
                .notes(partRequest.getNotes())
                .voiceNote(voiceNoteHelper.toResponse(partRequest.getVoiceNote()))
                .timeline(buildTimeline(partRequest, items))
                .build();
    }

    public PartRequestItemResponse toItemResponse(PartRequestItem item) {
        GarageProcurement procurement = item.getGarageProcurement();
        return PartRequestItemResponse.builder()
                .id(item.getId())
                .partName(item.getPartName())
                .partNumber(item.getPartNumber())
                .quantity(item.getQuantity())
                .description(item.getDescription())
                .estimatedPrice(item.getEstimatedPrice())
                .procurementStatus(item.getProcurementStatus())
                .addedToInvoice(item.getAddedToInvoice())
                .procurement(procurement != null ? toProcurementResponse(procurement) : null)
                .procuredAt(procurement != null ? procurement.getProcurementDate() : null)
                .receivedAt(item.getReceivedAt())
                .addedToInvoiceAt(item.getAddedToInvoiceAt())
                .build();
    }

    public GarageProcurementResponse toProcurementResponse(GarageProcurement procurement) {
        return GarageProcurementResponse.builder()
                .id(procurement.getId())
                .supplierName(procurement.getSupplierName())
                .supplierContact(procurement.getSupplierContact())
                .purchasePrice(procurement.getPurchasePrice())
                .partCondition(procurement.getPartCondition())
                .receiptNumber(procurement.getReceiptNumber())
                .procurementDate(procurement.getProcurementDate())
                .procuredByUserName(procurement.getProcuredBy() != null ? procurement.getProcuredBy().getFirstName() : null)
                .notes(procurement.getNotes())
                .voiceNote(voiceNoteHelper.toResponse(procurement.getVoiceNote()))
                .build();
    }

    public CustomerPartResponseResponse toCustomerResponse(CustomerPartResponse response) {
        return CustomerPartResponseResponse.builder()
                .id(response.getId())
                .choice(response.getChoice())
                .partCondition(response.getPartCondition())
                .responseDate(response.getResponseDate())
                .expectedDeliveryDate(response.getExpectedDeliveryDate())
                .customerComments(response.getCustomerComments())
                .customerVoiceNote(voiceNoteHelper.toResponse(response.getCustomerVoiceNote()))
                .build();
    }

    /**
     * Request status changes and per-part events, oldest first. Requests created before status times were
     * recorded fall back to the request date and the customer's response date; steps whose time is unknown
     * are left out rather than given a made-up time.
     */
    public List<PartStatusTimelineEntry> buildTimeline(PartRequest partRequest, List<PartRequestItem> items) {
        Map<PartRequestStatus, LocalDateTime> times = new EnumMap<>(PartRequestStatus.class);
        if (partRequest.getStatusTimestamps() != null) {
            times.putAll(partRequest.getStatusTimestamps());
        }
        if (partRequest.getRequestDate() != null) {
            times.putIfAbsent(PartRequestStatus.PENDING_CUSTOMER_RESPONSE, partRequest.getRequestDate());
        }
        if (partRequest.getCustomerResponseDate() != null && partRequest.getCustomerResponse() != null) {
            PartRequestStatus afterChoice = partRequest.getCustomerResponse().getChoice() == CustomerChoice.SELF_PROVIDE
                    ? PartRequestStatus.WAITING_FOR_CUSTOMER_DELIVERY : PartRequestStatus.GARAGE_WILL_PROCURE;
            times.putIfAbsent(afterChoice, partRequest.getCustomerResponseDate());
        }

        List<PartStatusTimelineEntry> timeline = new ArrayList<>();
        times.forEach((status, at) -> timeline.add(PartStatusTimelineEntry.builder()
                .level(LEVEL_REQUEST).event(status.name()).timestamp(at).description(describe(status)).build()));

        for (PartRequestItem item : items) {
            if (item.getGarageProcurement() != null && item.getGarageProcurement().getProcurementDate() != null) {
                timeline.add(itemEntry(item, PART_PROCURED, item.getGarageProcurement().getProcurementDate(),
                        "Garage bought " + item.getPartName()
                                + (item.getGarageProcurement().getSupplierName() != null
                                ? " from " + item.getGarageProcurement().getSupplierName() : "")));
            }
            if (item.getReceivedAt() != null) {
                timeline.add(itemEntry(item, PART_RECEIVED_FROM_CUSTOMER, item.getReceivedAt(),
                        "Customer handed over " + item.getPartName()));
            }
            if (item.getAddedToInvoiceAt() != null) {
                timeline.add(itemEntry(item, PART_ADDED_TO_INVOICE, item.getAddedToInvoiceAt(),
                        item.getPartName() + " added to the invoice"));
            }
        }

        // Request-level entry first when two share a timestamp
        timeline.sort(Comparator.comparing(PartStatusTimelineEntry::getTimestamp)
                .thenComparing(e -> LEVEL_REQUEST.equals(e.getLevel()) ? 0 : 1));
        for (int i = 1; i < timeline.size(); i++) {
            timeline.get(i).setDurationInMinutes((int) ChronoUnit.MINUTES.between(
                    timeline.get(i - 1).getTimestamp(), timeline.get(i).getTimestamp()));
        }
        return timeline;
    }

    private static PartStatusTimelineEntry itemEntry(PartRequestItem item, String event, LocalDateTime at, String description) {
        return PartStatusTimelineEntry.builder()
                .level(LEVEL_ITEM).event(event).timestamp(at).description(description)
                .partRequestItemId(item.getId()).partName(item.getPartName())
                .build();
    }

    static String describe(PartRequestStatus status) {
        return switch (status) {
            case PENDING_CUSTOMER_RESPONSE -> "Parts requested, waiting for the customer to choose";
            case CUSTOMER_WILL_PROVIDE -> "Customer will provide the parts";
            case GARAGE_WILL_PROCURE -> "Customer asked the garage to buy the parts";
            case GARAGE_PROCURING -> "Garage is buying the parts";
            case WAITING_FOR_CUSTOMER_DELIVERY -> "Waiting for the customer to bring the parts";
            case PART_RECEIVED -> "All parts received";
            case ADDED_TO_INVOICE -> "Parts added to the invoice";
            case CANCELLED -> "Part request cancelled";
        };
    }

    private static String serviceName(PartRequest partRequest) {
        try {
            return partRequest.getJobCard().getService().getService().getAvailableService().getName();
        } catch (NullPointerException e) {
            return null;
        }
    }
}

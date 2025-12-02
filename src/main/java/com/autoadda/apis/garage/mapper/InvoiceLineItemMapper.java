package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.InvoiceLineItem;
import com.autoadda.apis.garage.request.InvoiceLineItemRequest;
import com.autoadda.apis.garage.response.InvoiceLineItemResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class InvoiceLineItemMapper {

    public InvoiceLineItem toEntity(InvoiceLineItemRequest request) {
        if (request == null) return null;

        InvoiceLineItem lineItem = new InvoiceLineItem();
        lineItem.setDescription(request.getDescription());
        lineItem.setQuantity(request.getQuantity());
        lineItem.setPrice(request.getPrice());
        lineItem.setCategory(request.getCategory());
        lineItem.setServiceId(request.getServiceId());
        lineItem.setNotes(request.getNotes());

        return lineItem;
    }

    public InvoiceLineItemResponse toResponse(InvoiceLineItem entity) {
        if (entity == null) return null;

        return InvoiceLineItemResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .totalAmount(entity.getPrice() * entity.getQuantity())
                .category(entity.getCategory())
                .serviceId(entity.getServiceId())
                .notes(entity.getNotes())
                .build();
    }

    public List<InvoiceLineItem> toEntityList(List<InvoiceLineItemRequest> requests) {
        if (requests == null) return Collections.emptyList();
        return requests.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<InvoiceLineItemResponse> toResponseList(List<InvoiceLineItem> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
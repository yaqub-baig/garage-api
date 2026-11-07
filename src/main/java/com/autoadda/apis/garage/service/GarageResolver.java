package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Finds which garage a record belongs to, so permission checks can run on endpoints
 * that only receive a job card / invoice / work order id.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarageResolver {

    private final JobCardRepository jobCardRepository;
    private final WorkOrderRepository workOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final AppointmentDetailsRepository appointmentRepository;
    private final PartRequestRepository partRequestRepository;
    private final PartRequestItemRepository partRequestItemRepository;
    private final GarageInventoryItemRepository inventoryItemRepository;
    private final InventoryAlertRepository inventoryAlertRepository;
    private final WorkingToolsRepository workingToolsRepository;
    private final VehicleDeliveryRepository vehicleDeliveryRepository;
    private final VehicleReceivedRepository vehicleReceivedRepository;
    private final GarageServiceRepository garageServiceRepository;
    private final GarageSubscriptionRepository garageSubscriptionRepository;
    private final GarageBusinessListingRepository businessListingRepository;

    public Integer ofJobCard(Integer id) {
        requireId(id);
        return garageId(jobCardRepository.findById(id).map(j -> j.getGarage()), "Job card", id);
    }

    public Integer ofWorkOrder(Integer id) {
        requireId(id);
        return garageId(workOrderRepository.findById(id).map(w -> w.getGarage()), "Work order", id);
    }

    public Integer ofInvoice(Integer id) {
        requireId(id);
        return garageId(invoiceRepository.findById(id).map(i -> i.getWorkOrder() != null
                ? i.getWorkOrder().getGarage()
                : i.getJobCard() != null ? i.getJobCard().getGarage() : null), "Invoice", id);
    }

    public Integer ofAppointment(Integer id) {
        requireId(id);
        return garageId(appointmentRepository.findById(id).map(a -> a.getGarage()), "Appointment", id);
    }

    public Integer ofPartRequest(Integer id) {
        requireId(id);
        return garageId(partRequestRepository.findById(id).map(p -> p.getJobCard().getGarage()), "Part request", id);
    }

    public Integer ofPartRequestItem(Integer id) {
        requireId(id);
        return garageId(partRequestItemRepository.findById(id)
                .map(i -> i.getPartRequest().getJobCard().getGarage()), "Part request item", id);
    }

    public Integer ofInventoryItem(Integer id) {
        requireId(id);
        return garageId(inventoryItemRepository.findById(id).map(i -> i.getGarage()), "Inventory item", id);
    }

    public Integer ofInventoryAlert(Integer id) {
        requireId(id);
        return garageId(inventoryAlertRepository.findById(id)
                .map(a -> a.getGarageInventoryItem().getGarage()), "Alert", id);
    }

    public Integer ofWorkingTool(Integer id) {
        requireId(id);
        return garageId(workingToolsRepository.findById(id).map(t -> t.getGarage()), "Working tool", id);
    }

    public Integer ofDelivery(Integer id) {
        requireId(id);
        return garageId(vehicleDeliveryRepository.findById(id).map(d -> d.getWorkOrder().getGarage()), "Delivery", id);
    }

    public Integer ofVehicleReceived(Integer id) {
        requireId(id);
        return garageId(vehicleReceivedRepository.findById(id).map(v -> v.getAppointment().getGarage()), "Vehicle received record", id);
    }

    public Integer ofGarageService(Integer id) {
        requireId(id);
        return garageId(garageServiceRepository.findById(id).map(s -> s.getGarage()), "Garage service", id);
    }

    public Integer ofSubscriptionPlan(Integer id) {
        requireId(id);
        return garageId(garageSubscriptionRepository.findById(id).map(s -> s.getGarage()), "Subscription plan", id);
    }

    public Integer ofBusinessListing(Integer id) {
        requireId(id);
        return garageId(businessListingRepository.findById(id).map(l -> l.getGarage()), "Listing", id);
    }

    /** Customer (AutoAdda user id) of the work order's appointment. */
    public Integer customerOfWorkOrder(Integer id) {
        return workOrderRepository.findById(id)
                .map(w -> w.getAppointment() != null ? w.getAppointment().getCustomerId() : null)
                .orElse(null);
    }

    /** Customer (AutoAdda user id) of the invoice's work order. */
    public Integer customerOfInvoice(Integer id) {
        return invoiceRepository.findById(id)
                .map(i -> i.getWorkOrder() != null && i.getWorkOrder().getAppointment() != null
                        ? i.getWorkOrder().getAppointment().getCustomerId() : null)
                .orElse(null);
    }

    private static void requireId(Integer id) {
        if (id == null) {
            throw new BadRequestException("Record id is required");
        }
    }

    private static Integer garageId(Optional<Garage> garage, String what, Integer id) {
        return garage.map(Garage::getId)
                .orElseThrow(() -> new ResourceNotFoundException(what + " not found with id: " + id));
    }
}

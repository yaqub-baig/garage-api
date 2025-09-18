package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.request.InvoiceLineItemRequest;
import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceLineItemGeneratorService {

    /**
     * Generates line items from a WorkOrder based on completed JobCards
     *
     * @param workOrder the WorkOrder to generate line items from
     * @return list of InvoiceLineItemRequest objects
     */
    public List<InvoiceLineItemRequest> generateLineItemsFromWorkOrder(WorkOrder workOrder) {
        List<InvoiceLineItemRequest> lineItems = new ArrayList<>();

        // Filter only completed job cards
        List<JobCard> completedJobCards = workOrder.getJobCards().stream()
                .filter(jc -> jc.getStatus() == JobCardStatus.WORK_COMPLETED ||
                             jc.getStatus() == JobCardStatus.QUALITY_CHECK_PASSED ||
                             jc.getStatus() == JobCardStatus.COMPLETED)
                .toList();

        if (completedJobCards.isEmpty()) {
            log.warn("No completed job cards found for WorkOrder ID: {}", workOrder.getId());
            return lineItems;
        }

        // Generate service line items from job cards
        for (JobCard jobCard : completedJobCards) {
            AppointmentServices appointmentService = jobCard.getService();
            if (appointmentService != null && appointmentService.getService() != null) {
                AvailableService availableService = appointmentService.getService().getAvailableService();

                InvoiceLineItemRequest serviceItem = InvoiceLineItemRequest.builder()
                        .description(availableService.getName())
                        .quantity(1)
                        .price(appointmentService.getService().getPrice())
                        .category("SERVICE")
                        .serviceId(availableService.getId())
                        .notes("JobCard ID: " + jobCard.getId())
                        .build();

                lineItems.add(serviceItem);
            }
        }

        // Add labor charges if applicable
        double laborHours = calculateTotalLaborHours(completedJobCards);
        if (laborHours > 0) {
            InvoiceLineItemRequest laborItem = InvoiceLineItemRequest.builder()
                    .description("Labor Charges")
                    .quantity((int) Math.ceil(laborHours))
                    .price(calculateLaborRate(workOrder))
                    .category("LABOR")
                    .notes("Total hours: " + laborHours)
                    .build();

            lineItems.add(laborItem);
        }

        // TODO: Add parts/materials if you have that data structure
        // addPartsLineItems(workOrder, lineItems);

        return lineItems;
    }

    /**
     * Calculates total labor hours from completed job cards
     */
    private double calculateTotalLaborHours(List<JobCard> jobCards) {
        return jobCards.stream()
                .mapToDouble(this::calculateJobCardLaborHours)
                .sum();
    }

    /**
     * Calculates labor hours for a single job card
     */
    private double calculateJobCardLaborHours(JobCard jobCard) {
        if (jobCard.getServiceStartTime() != null && jobCard.getServiceEndTime() != null) {
            // Convert LocalTime to hours (assuming same day)
            double startHours = jobCard.getServiceStartTime().getHour() +
                              jobCard.getServiceStartTime().getMinute() / 60.0;
            double endHours = jobCard.getServiceEndTime().getHour() +
                            jobCard.getServiceEndTime().getMinute() / 60.0;

            double totalHours = endHours - startHours;
            return Math.max(totalHours, 0); // Ensure non-negative
        }

        // Default estimate if no time tracking
        return 1.0; // 1 hour default per service
    }

    /**
     * Calculates labor rate based on garage or service type
     */
    private Double calculateLaborRate(WorkOrder workOrder) {
        // You can implement dynamic labor rate calculation here
        // Based on garage settings, service type, technician level, etc.
        return 50.0; // Default $50/hour
    }

    /**
     * Generates standard line items that can be customized
     */
    public List<InvoiceLineItemRequest> generateStandardLineItems() {
        List<InvoiceLineItemRequest> standardItems = new ArrayList<>();

        // Common service items
        standardItems.add(InvoiceLineItemRequest.builder()
                .description("Diagnostic Fee")
                .quantity(1)
                .price(25.0)
                .category("SERVICE")
                .build());

        standardItems.add(InvoiceLineItemRequest.builder()
                .description("Shop Supplies")
                .quantity(1)
                .price(15.0)
                .category("MATERIALS")
                .build());

        return standardItems;
    }
}
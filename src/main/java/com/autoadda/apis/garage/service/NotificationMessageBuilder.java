package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Invoice;
import com.autoadda.apis.garage.entity.PartRequest;
import com.autoadda.apis.garage.entity.PartRequestItem;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.util.CustomerChoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builder service for creating multi-channel notification content.
 * Creates SMS, Email, and WebPush content for different notification scenarios.
 */
@Slf4j
@Component
public class NotificationMessageBuilder {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final String COLLABORATION_REQUESTS = "/collaboration-requests";

    /**
     * Build notification content for job card started
     */
    public NotificationContent buildJobCardStarted(
            String serviceName,
            LocalDateTime startTime,
            String vehicleNumber) {

        String smsMessage = String.format(
                "Your service '%s' has started at %s for vehicle %s. Track progress in AutoAdda app.",
                serviceName,
                startTime.format(TIME_FORMATTER),
                vehicleNumber
        );

        String emailSubject = "Service Started - AutoAdda";
        String emailBody = buildJobCardStartedEmailHtml(serviceName, startTime, vehicleNumber);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Service Started")
                .webpushBody(String.format("'%s' has started for %s", serviceName, vehicleNumber))
                .webpushUrl("/job-cards")
                .build();
    }

    /**
     * Build notification content for job card completed
     */
    public NotificationContent buildJobCardCompleted(
            String serviceName,
            LocalDateTime completedTime,
            String vehicleNumber) {

        String smsMessage = String.format(
                "Your service '%s' completed at %s for vehicle %s. View details in AutoAdda app.",
                serviceName,
                completedTime.format(TIME_FORMATTER),
                vehicleNumber
        );

        String emailSubject = "Service Completed - AutoAdda";
        String emailBody = buildJobCardCompletedEmailHtml(serviceName, completedTime, vehicleNumber);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Service Completed")
                .webpushBody(String.format("'%s' completed for %s", serviceName, vehicleNumber))
                .webpushUrl("/job-cards")
                .build();
    }

    /**
     * Build notification content for work order created
     */
    public NotificationContent buildWorkOrderCreated(
            Integer workOrderId,
            String vehicleNumber,
            int serviceCount) {

        String smsMessage = String.format(
                "Your work order #%d has been created for %s with %d service(s). " +
                "Please visit your AutoAdda dashboard to proceed.",
                workOrderId, vehicleNumber, serviceCount
        );

        String emailSubject = "Work Order Created - AutoAdda";
        String emailBody = buildWorkOrderCreatedEmailHtml(workOrderId, vehicleNumber, serviceCount);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Work Order Created")
                .webpushBody(String.format("Work order #%d with %d service(s)", workOrderId, serviceCount))
                .webpushUrl("/work-orders/" + workOrderId)
                .build();
    }

    /**
     * Build notification content for invoice ready
     */
    public NotificationContent buildInvoiceReady(
            Invoice invoice,
            VehicleDetailResponse vehicle) {

        String smsMessage = buildInvoiceSmsMessage(invoice, vehicle);
        String emailSubject = "Invoice Ready - AutoAdda";
        String emailBody = buildInvoiceEmailHtml(invoice, vehicle);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Invoice Ready")
                .webpushBody(String.format("Invoice #%d - AED%.2f",
                        invoice.getId(), invoice.getTotalAmount()))
                .webpushUrl("/invoices/" + invoice.getId())
                .build();
    }

    /**
     * Build notification content for part request
     */
    public NotificationContent buildPartRequest(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            List<PartRequestItem> items) {

        String smsMessage = buildPartRequestSmsMessage(partRequest, vehicle, items);
        String emailSubject = "Parts Required - AutoAdda";
        String emailBody = buildPartRequestEmailHtml(partRequest, vehicle, items);

        String serviceName = partRequest.getJobCard().getService().getService()
                .getAvailableService().getName();

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Parts Required in "+serviceName)
                .webpushBody(String.format("Action needed for %s %s",
                        vehicle.getBrand(), vehicle.getModel()))
                .webpushUrl("/part-requests/" + partRequest.getId())
                .build();
    }

    /**
     * Build notification content for part response confirmation
     */
    public NotificationContent buildPartResponseConfirmation(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            CustomerChoice choice) {

        String smsMessage = buildPartResponseSmsMessage(partRequest, vehicle, choice);
        String emailSubject = "Response Confirmed - AutoAdda";
        String emailBody = buildPartResponseEmailHtml(partRequest, vehicle, choice);

        String choiceText = (choice == CustomerChoice.SELF_PROVIDE)
                ? "You'll provide the parts"
                : "Garage will purchase parts";

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Response Confirmed")
                .webpushBody(choiceText)
                .webpushUrl("/part-requests/" + partRequest.getId())
                .build();
    }

    // ========== SMS Message Builders ==========

    private String buildInvoiceSmsMessage(Invoice invoice, VehicleDetailResponse vehicle) {
        StringBuilder message = new StringBuilder();
        message.append("🧾 INVOICE READY - AutoAdda\n\n");
        message.append("Vehicle: ").append(vehicle.getBrand())
                .append(" ").append(vehicle.getModel()).append("\n");
        message.append("Reg. No: ").append(vehicle.getNumber()).append("\n\n");
        message.append("Invoice ID: ").append(invoice.getId()).append("\n");

        if (invoice.getTotalAmount() != null) {
            message.append("Total Amount: AED").append(String.format("%.2f", invoice.getTotalAmount())).append("\n");
        }

        if (invoice.getIsPaid() != null && invoice.getIsPaid()) {
            message.append("Status: ✅ PAID\n");
        } else {
            message.append("Status: ⏳ PENDING PAYMENT\n");
        }

        message.append("\nView full details in your AutoAdda app.");
        return message.toString();
    }

    private String buildPartRequestSmsMessage(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            List<PartRequestItem> items) {

        String serviceName = partRequest.getJobCard().getService().getService()
                .getAvailableService().getName();
        StringBuilder message = new StringBuilder();
        message.append("🔧 PARTS REQUIRED - AutoAdda\n\n");
        message.append("Service: ").append(serviceName).append("\n");
        message.append("Vehicle: ").append(vehicle.getBrand()).append(" ")
                .append(vehicle.getModel()).append("\n");
        message.append("Reg No: ").append(vehicle.getNumber()).append("\n\n");
        message.append("Parts needed:\n");

        for (int i = 0; i < Math.min(items.size(), 3); i++) {
            PartRequestItem item = items.get(i);
            message.append("• ").append(item.getPartName())
                    .append(" (Qty: ").append(item.getQuantity()).append(")\n");
        }

        if (items.size() > 3) {
            message.append("...and ").append(items.size() - 3).append(" more\n");
        }

        message.append("\nPlease open the AutoAdda app to choose:\n");
        message.append("1. You'll provide the parts\n");
        message.append("2. Garage should buy (genuine/used)");

        return message.toString();
    }

    private String buildPartResponseSmsMessage(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            CustomerChoice choice) {

        String serviceName = partRequest.getJobCard().getService().getService()
                .getAvailableService().getName();

        StringBuilder message = new StringBuilder();
        message.append("✅ RESPONSE CONFIRMED - AutoAdda\n\n");
        message.append("Service: ").append(serviceName).append("\n");
        message.append("Your choice: ");
        message.append("Reg No: ").append(vehicle.getNumber()).append("\n\n");


        if (choice == CustomerChoice.SELF_PROVIDE) {
            message.append("You will provide the parts\n\n");
            message.append("Please deliver the parts to the garage soon.\n");
            message.append("Work will resume once parts are received.");
        } else {
            message.append("Garage will purchase\n\n");
            message.append("The garage will procure the parts.\n");
            message.append("Part cost will be added to your invoice.");
        }

        return message.toString();
    }

    // ========== Email HTML Builders ==========

    private String buildJobCardStartedEmailHtml(
            String serviceName,
            LocalDateTime startTime,
            String vehicleNumber) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #2c3e50; margin-top: 0;">Service Started</h2>
                    <p style="font-size: 16px; color: #555;">Your service has started:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Started At:</strong> %s</p>
                    </div>
                    <p style="font-size: 16px; color: #555;">Track your service progress in the AutoAdda app.</p>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #3498db; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Details
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            serviceName,
            vehicleNumber,
            startTime.format(DATETIME_FORMATTER)
        );
    }

    private String buildJobCardCompletedEmailHtml(
            String serviceName,
            LocalDateTime completedTime,
            String vehicleNumber) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #27ae60; margin-top: 0;">✓ Service Completed</h2>
                    <p style="font-size: 16px; color: #555;">Great news! Your service has been completed:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #27ae60; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Completed At:</strong> %s</p>
                    </div>
                    <p style="font-size: 16px; color: #555;">View the full details in your AutoAdda app.</p>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #27ae60; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Details
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            serviceName,
            vehicleNumber,
            completedTime.format(DATETIME_FORMATTER)
        );
    }

    private String buildWorkOrderCreatedEmailHtml(
            Integer workOrderId,
            String vehicleNumber,
            int serviceCount) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #2c3e50; margin-top: 0;">Work Order Created</h2>
                    <p style="font-size: 16px; color: #555;">Your work order has been successfully created:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Work Order ID:</strong> #%d</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Services:</strong> %d service(s)</p>
                    </div>
                    <p style="font-size: 16px; color: #555;">Please visit your AutoAdda dashboard to proceed with the services.</p>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #3498db; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Work Order
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            workOrderId,
            vehicleNumber,
            serviceCount,
            workOrderId
        );
    }

    private String buildInvoiceEmailHtml(Invoice invoice, VehicleDetailResponse vehicle) {
        String statusBadge = (invoice.getIsPaid() != null && invoice.getIsPaid())
                ? "<span style='background-color: #27ae60; color: white; padding: 4px 12px; border-radius: 4px; font-size: 14px;'>PAID</span>"
                : "<span style='background-color: #f39c12; color: white; padding: 4px 12px; border-radius: 4px; font-size: 14px;'>PENDING</span>";

        StringBuilder servicesHtml = new StringBuilder();
        if (invoice.getLineItems() != null && !invoice.getLineItems().isEmpty()) {
            servicesHtml.append("<h3 style='color: #2c3e50; margin-top: 25px;'>Services:</h3><ul style='list-style: none; padding: 0;'>");
            invoice.getLineItems().stream()
                    .limit(3)
                    .forEach(item -> servicesHtml.append("<li style='padding: 8px 0; border-bottom: 1px solid #eee;'>• ")
                            .append(item.getDescription()).append("</li>"));
            if (invoice.getLineItems().size() > 3) {
                servicesHtml.append("<li style='padding: 8px 0; color: #888;'>... and ")
                        .append(invoice.getLineItems().size() - 3).append(" more</li>");
            }
            servicesHtml.append("</ul>");
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #2c3e50; margin-top: 0;">Invoice Ready</h2>
                    <p style="font-size: 16px; color: #555;">Your invoice is now available:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #e74c3c; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s %s</p>
                        <p style="margin: 5px 0;"><strong>Reg. No:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Invoice ID:</strong> #%d</p>
                        <p style="margin: 15px 0 5px 0;"><strong>Total Amount:</strong> <span style="font-size: 24px; color: #e74c3c;">AED%.2f</span></p>
                        <p style="margin: 5px 0;"><strong>Status:</strong> %s</p>
                    </div>
                    %s
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #e74c3c; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Invoice
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getNumber(),
            invoice.getId(),
            invoice.getTotalAmount(),
            statusBadge,
            servicesHtml.toString(),
            invoice.getId()
        );
    }

    private String buildPartRequestEmailHtml(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            List<PartRequestItem> items) {

        String serviceName = partRequest.getJobCard().getService().getService()
                .getAvailableService().getName();

        StringBuilder partsHtml = new StringBuilder("<ul style='list-style: none; padding: 0;'>");
        for (int i = 0; i < Math.min(items.size(), 3); i++) {
            PartRequestItem item = items.get(i);
            partsHtml.append("<li style='padding: 8px 0; border-bottom: 1px solid #eee;'>• ")
                    .append(item.getPartName())
                    .append(" <span style='color: #888;'>(Qty: ")
                    .append(item.getQuantity()).append(")</span></li>");
        }
        if (items.size() > 3) {
            partsHtml.append("<li style='padding: 8px 0; color: #888;'>... and ")
                    .append(items.size() - 3).append(" more</li>");
        }
        partsHtml.append("</ul>");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #e67e22; margin-top: 0;">🔧 Parts Required</h2>
                    <p style="font-size: 16px; color: #555;">We need some parts to continue your service:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #e67e22; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s %s</p>
                        <p style="margin: 5px 0;"><strong>Reg. No:</strong> %s</p>
                    </div>
                    <h3 style="color: #2c3e50; margin-top: 25px;">Parts Needed:</h3>
                    %s
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin-top: 20px;">
                        <p style="margin: 0; font-weight: bold; color: #856404;">Action Required:</p>
                        <p style="margin: 10px 0 5px 0;">Please choose one option:</p>
                        <ol style="margin: 5px 0; padding-left: 20px;">
                            <li>You'll provide the parts yourself</li>
                            <li>Garage should buy (genuine/used options available)</li>
                        </ol>
                    </div>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #e67e22; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        Make Your Choice
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            serviceName,
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getNumber(),
            partsHtml.toString(),
            partRequest.getId()
        );
    }

    private String buildPartResponseEmailHtml(
            PartRequest partRequest,
            VehicleDetailResponse vehicle,
            CustomerChoice choice) {

        String serviceName = partRequest.getJobCard().getService().getService()
                .getAvailableService().getName();

        String choiceDetails;
        String backgroundColor;
        if (choice == CustomerChoice.SELF_PROVIDE) {
            choiceDetails = "<p><strong>You will provide the parts</strong></p>" +
                    "<p style='margin: 10px 0;'>Please deliver the parts to the garage soon. " +
                    "Work will resume once parts are received.</p>";
            backgroundColor = "#d4edda";
        } else {
            choiceDetails = "<p><strong>Garage will purchase the parts</strong></p>" +
                    "<p style='margin: 10px 0;'>The garage will procure the parts. " +
                    "Part cost will be added to your invoice.</p>";
            backgroundColor = "#cce5ff";
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #27ae60; margin-top: 0;">✓ Response Confirmed</h2>
                    <p style="font-size: 16px; color: #555;">Thank you for your response!</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #27ae60; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s %s</p>
                    </div>
                    <div style="background-color: %s; padding: 15px; border-radius: 5px; margin-top: 20px;">
                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">Your Choice:</p>
                        %s
                    </div>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #27ae60; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Details
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            serviceName,
            vehicle.getBrand(),
            vehicle.getModel(),
            backgroundColor,
            choiceDetails,
            partRequest.getId()
        );
    }

    /**
     * Build notification content for technician job assignment
     */
    public NotificationContent buildTechnicianJobAssignment(
            String serviceName,
            String vehicleNumber,
            Integer appointmentId) {

        String smsMessage = String.format(
                "New job assigned: %s for vehicle %s. Check AutoAdda app for details.",
                serviceName, vehicleNumber
        );

        String emailSubject = "New Job Assignment - AutoAdda";
        String emailBody = buildTechnicianAssignmentEmailHtml(serviceName, vehicleNumber, appointmentId);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("New Job Assignment")
                .webpushBody(String.format("%s - %s", serviceName, vehicleNumber))
                .webpushUrl("/technician/jobs/" + appointmentId)
                .build();
    }

    private String buildTechnicianAssignmentEmailHtml(
            String serviceName,
            String vehicleNumber,
            Integer appointmentId) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #3498db; margin-top: 0;">New Job Assignment</h2>
                    <p style="font-size: 16px; color: #555;">You have been assigned to a new job:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Service:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Appointment ID:</strong> #%d</p>
                    </div>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #3498db; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Job Details
                    </a>
                </div>
            </body>
            </html>
            """,
            serviceName, vehicleNumber, appointmentId, appointmentId
        );
    }

    /**
     * Build notification content for garage owner new work order
     */
    public NotificationContent buildGarageOwnerWorkOrder(
            Integer workOrderId,
            String vehicleNumber,
            int serviceCount,
            String garageName) {

        String smsMessage = String.format(
                "New work order #%d at %s: %s with %d service(s). Check AutoAdda dashboard.",
                workOrderId, garageName, vehicleNumber, serviceCount
        );

        String emailSubject = "New Work Order - AutoAdda";
        String emailBody = buildGarageOwnerWorkOrderEmailHtml(workOrderId, vehicleNumber, serviceCount, garageName);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("New Work Order")
                .webpushBody(String.format("WO #%d - %s (%d services)", workOrderId, vehicleNumber, serviceCount))
                .webpushUrl("/garage/work-orders/" + workOrderId)
                .build();
    }

    private String buildGarageOwnerWorkOrderEmailHtml(
            Integer workOrderId,
            String vehicleNumber,
            int serviceCount,
            String garageName) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #27ae60; margin-top: 0;">New Work Order</h2>
                    <p style="font-size: 16px; color: #555;">A new work order has been created at your garage:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #27ae60; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Work Order ID:</strong> #%d</p>
                        <p style="margin: 5px 0;"><strong>Garage:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Vehicle:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Services:</strong> %d service(s)</p>
                    </div>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #27ae60; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Work Order
                    </a>
                </div>
            </body>
            </html>
            """,
            workOrderId, garageName, vehicleNumber, serviceCount, workOrderId
        );
    }

    /**
     * Build notification content for garage-technician collaboration request received
     */
    public NotificationContent buildCollaborationRequestReceived(
            String senderName,
            String senderType,
            String description) {

        String smsMessage = String.format(
                "New collaboration request from %s (%s). %s Check AutoAdda app to accept or reject.",
                senderName, senderType, description != null ? description + ". " : ""
        );

        String emailSubject = "New Collaboration Request - AutoAdda";
        String emailBody = buildCollaborationRequestReceivedEmailHtml(senderName, senderType, description);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("New Collaboration Request")
                .webpushBody(String.format("%s wants to collaborate", senderName))
                .webpushUrl(COLLABORATION_REQUESTS)
                .build();
    }

    private String buildCollaborationRequestReceivedEmailHtml(
            String senderName,
            String senderType,
            String description) {

        String descriptionHtml = (description != null && !description.isBlank())
                ? String.format("<p style='margin: 15px 0; padding: 15px; background-color: #f8f9fa; border-left: 3px solid #3498db;'><strong>Message:</strong> %s</p>", description)
                : "";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #3498db; margin-top: 0;">New Collaboration Request</h2>
                    <p style="font-size: 16px; color: #555;">You have received a collaboration request:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>From:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Type:</strong> %s</p>
                    </div>
                    %s
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin-top: 20px;">
                        <p style="margin: 0; font-weight: bold; color: #856404;">Action Required:</p>
                        <p style="margin: 10px 0;">Please review this request and accept or reject it through your AutoAdda dashboard.</p>
                    </div>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #3498db; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Request
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            senderName,
            senderType,
            descriptionHtml
        );
    }

    /**
     * Build notification content for collaboration request approved
     */
    public NotificationContent buildCollaborationRequestApproved(
            String approverName,
            String approverType) {

        String smsMessage = String.format(
                "Great news! %s (%s) has accepted your collaboration request. You can now work together on AutoAdda.",
                approverName, approverType
        );

        String emailSubject = "Collaboration Request Approved - AutoAdda";
        String emailBody = buildCollaborationRequestApprovedEmailHtml(approverName, approverType);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Request Approved")
                .webpushBody(String.format("%s accepted your request", approverName))
                .webpushUrl(COLLABORATION_REQUESTS)
                .build();
    }

    private String buildCollaborationRequestApprovedEmailHtml(
            String approverName,
            String approverType) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #27ae60; margin-top: 0;">✓ Collaboration Request Approved</h2>
                    <p style="font-size: 16px; color: #555;">Great news! Your collaboration request has been approved:</p>
                    <div style="background-color: #d4edda; padding: 15px; border-left: 4px solid #27ae60; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Approved By:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Type:</strong> %s</p>
                    </div>
                    <p style="font-size: 16px; color: #555;">You can now collaborate and work together on the AutoAdda platform.</p>
                    <a href="https://autoadda.mylstech.com"
                       style="display: inline-block; margin-top: 20px; background-color: #27ae60; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        View Details
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            approverName,
            approverType
        );
    }

    /**
     * Build notification content for collaboration request rejected
     */
    public NotificationContent buildCollaborationRequestRejected(
            String rejecterName,
            String rejecterType) {

        String smsMessage = String.format(
                "%s (%s) has declined your collaboration request on AutoAdda.",
                rejecterName, rejecterType
        );

        String emailSubject = "Collaboration Request Declined - AutoAdda";
        String emailBody = buildCollaborationRequestRejectedEmailHtml(rejecterName, rejecterType);

        return NotificationContent.builder()
                .smsMessage(smsMessage)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .emailIsHtml(true)
                .webpushTitle("Request Declined")
                .webpushBody(String.format("%s declined your request", rejecterName))
                .webpushUrl(COLLABORATION_REQUESTS)
                .build();
    }

    private String buildCollaborationRequestRejectedEmailHtml(
            String rejecterName,
            String rejecterType) {

        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px;">
                    <h2 style="color: #e74c3c; margin-top: 0;">Collaboration Request Declined</h2>
                    <p style="font-size: 16px; color: #555;">Your collaboration request has been declined:</p>
                    <div style="background-color: #f8d7da; padding: 15px; border-left: 4px solid #e74c3c; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Declined By:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Type:</strong> %s</p>
                    </div>
                    <p style="font-size: 16px; color: #555;">You can search for other garages or technicians to collaborate with on the AutoAdda platform.</p>
                    <a href="https://autoadda.mylstech.com
                       style="display: inline-block; margin-top: 20px; background-color: #3498db; color: white;
                              padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        Find Partners
                    </a>
                    <hr style="margin-top: 30px; border: none; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #888; margin-bottom: 0;">
                        This is an automated notification from AutoAdda.
                    </p>
                </div>
            </body>
            </html>
            """,
            rejecterName,
            rejecterType
        );
    }
}

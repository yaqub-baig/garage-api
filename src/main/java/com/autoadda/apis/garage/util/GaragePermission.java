package com.autoadda.apis.garage.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catalogue of actions a garage team member can be allowed to perform.
 * Garage roles ({@link com.autoadda.apis.garage.entity.GarageRole}) are built from these.
 */
@Getter
@RequiredArgsConstructor
public enum GaragePermission {
    TEAM_VIEW("Team", "View team members, roles and invites"),
    TEAM_MANAGE("Team", "Invite, remove and change roles of team members"),
    ROLE_MANAGE("Team", "Create, edit and delete roles"),

    GARAGE_PROFILE_EDIT("Garage", "Edit garage profile"),
    SERVICES_MANAGE("Garage", "Manage garage services and prices"),
    SUBSCRIPTION_MANAGE("Garage", "Manage subscription plans"),
    BUSINESS_LISTING_MANAGE("Garage", "List the garage business for sale"),

    APPOINTMENT_VIEW("Operations", "View appointments"),
    APPOINTMENT_MANAGE("Operations", "Update appointment status"),
    WORK_ORDER_MANAGE("Operations", "Create and update work orders"),
    JOB_CARD_ASSIGN("Operations", "Assign technicians to services"),
    JOB_CARD_UPDATE("Operations", "Update job card status"),
    VEHICLE_CHECKIN_DELIVERY("Operations", "Receive and deliver vehicles"),
    PART_REQUEST_MANAGE("Operations", "Raise and manage part requests"),

    INVENTORY_VIEW("Inventory", "View inventory"),
    INVENTORY_MANAGE("Inventory", "Manage inventory items, stock and alerts"),
    TOOLS_MANAGE("Inventory", "Manage working tools"),

    INVOICE_VIEW("Finance", "View invoices"),
    INVOICE_MANAGE("Finance", "Generate invoices and record payments"),
    ANALYTICS_VIEW("Finance", "View garage analytics");

    private final String group;
    private final String description;
}

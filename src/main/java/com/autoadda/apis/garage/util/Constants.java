package com.autoadda.apis.garage.util;

public class Constants {
    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //member type
    public static final String NEW = "NEW";
    public static final String REQUESTED = "REQUESTED";
    public static final String CONFIRM = "CONFIRM";
    public static final String WORK_INPROGRESS = "WORK_INPROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String PENDING_INVOICE = "PENDING_INVOICE";
    public static final String READY_FOR_DELIVERY = "READY_FOR_DELIVERY";
    public static final String DELIVERED = "DELIVERED";
    public static final String DONE = "DONE";

    public static final String ASSIGNED = "ASSIGNED";
    public static final String WAITING_FOR_SPARES = "WAITING_FOR_SPARES";
    public static final String INPROGRESS = "JOB_INPROGRESS";
    public static final String JOB_COMPLETED = "JOB_COMPLETED";


}

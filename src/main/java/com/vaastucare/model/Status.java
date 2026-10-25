package com.vaastucare.model;

/** The four exact request statuses required by the project brief. */
public final class Status {
    private Status() {}
    public static final String SCHEDULED = "Scheduled";
    public static final String IN_PROGRESS = "In Progress";
    public static final String COMPLETED = "Completed";
    public static final String REJECTED = "Rejected";
    public static final String[] ALL = { SCHEDULED, IN_PROGRESS, COMPLETED, REJECTED };
}

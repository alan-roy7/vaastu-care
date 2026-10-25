package com.vaastucare.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A service request. Status is always one of the four values in {@link Status}.
 * A freshly created request is "Scheduled" with accepted == false, meaning the
 * provider has not yet responded; accepting sets accepted = true.
 */
public class ServiceRequest {
    public String id, propertyId, ownerId, providerId, category, description, notes, status;
    public boolean accepted, reviewed;
    public LocalDate preferredDate, createdOn;
    public final List<StatusEntry> history = new ArrayList<>();
    /** Photo references: "DEMO:n" = generated sample image, "FILE:path" = file chosen in the file dialog. */
    public final List<String> photos = new ArrayList<>();

    public String responseLabel() {
        if (Status.REJECTED.equals(status)) return "Declined";
        return accepted ? "Accepted" : "Awaiting response";
    }

    public boolean awaitingProvider() { return Status.SCHEDULED.equals(status) && !accepted; }
}

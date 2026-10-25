package com.vaastucare.model;

import java.time.LocalDate;

/** A service provider account. Only APPROVED, non-suspended providers appear in the directory. */
public class Provider extends User {
    public static final String PENDING = "Pending", APPROVED = "Approved", REJECTED = "Rejected";

    public String category, area, description, availability, approval = PENDING, rejectionReason = "";
    public int experienceYears;
    public boolean suspended;
    public LocalDate appliedOn = LocalDate.of(2026, 10, 1);

    public Provider(String id, String name, String email, String username, String password,
                    String phone, String city, String category, String area, int experienceYears,
                    String description, String availability, String approval) {
        super(id, Role.PROVIDER, name, email, username, password, phone, city);
        this.category = category; this.area = area; this.experienceYears = experienceYears;
        this.description = description; this.availability = availability; this.approval = approval;
    }

    public boolean isListed() { return APPROVED.equals(approval) && !suspended; }

    public String statusLabel() { return suspended ? "Suspended" : approval; }
}

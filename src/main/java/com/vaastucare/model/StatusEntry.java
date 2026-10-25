package com.vaastucare.model;

import java.time.LocalDateTime;

/** One line in a request's status history. byId is the id of the user who made the change (may be null for old sample rows). */
public class StatusEntry {
    public final String status, note, by, byId;
    public final LocalDateTime time;

    public StatusEntry(String status, String note, String by, LocalDateTime time) {
        this(status, note, by, null, time);
    }

    public StatusEntry(String status, String note, String by, String byId, LocalDateTime time) {
        this.status = status; this.note = note; this.by = by; this.byId = byId; this.time = time;
    }
}

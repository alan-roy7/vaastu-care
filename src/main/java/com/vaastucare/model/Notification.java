package com.vaastucare.model;

import java.time.LocalDateTime;

public class Notification {
    public String id, userId, text, requestId;
    public LocalDateTime time;
    public boolean read;

    public Notification(String id, String userId, String text, String requestId, LocalDateTime time, boolean read) {
        this.id = id; this.userId = userId; this.text = text; this.requestId = requestId;
        this.time = time; this.read = read;
    }
}

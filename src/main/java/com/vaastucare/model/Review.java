package com.vaastucare.model;

import java.time.LocalDate;

public class Review {
    public String id, requestId, ownerId, providerId, text;
    public int rating;
    public LocalDate date;

    /** requestId may be null for reviews that pre-date the demo requests. */
    public Review(String id, String requestId, String ownerId, String providerId,
                  int rating, String text, LocalDate date) {
        this.id = id; this.requestId = requestId; this.ownerId = ownerId; this.providerId = providerId;
        this.rating = rating; this.text = text; this.date = date;
    }
}

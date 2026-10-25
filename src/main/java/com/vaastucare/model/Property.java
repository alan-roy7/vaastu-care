package com.vaastucare.model;

public class Property {
    public String id, ownerId, name, address, city, type, notes;
    public int areaSqft, bedrooms, imageSeed;

    public Property(String id, String ownerId, String name, String address, String city,
                    String type, int areaSqft, int bedrooms, String notes, int imageSeed) {
        this.id = id; this.ownerId = ownerId; this.name = name; this.address = address; this.city = city;
        this.type = type; this.areaSqft = areaSqft; this.bedrooms = bedrooms; this.notes = notes;
        this.imageSeed = imageSeed;
    }

    public String summary() {
        return (bedrooms > 0 ? bedrooms + " bed · " : "") + String.format("%,d sq ft", areaSqft);
    }
}

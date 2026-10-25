package com.vaastucare.model;

public class Category {
    public String id, name, description;
    public boolean active = true;

    public Category(String id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }
}

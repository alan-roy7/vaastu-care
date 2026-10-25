package com.vaastucare.model;

/** Base account. Owners and admins use this class directly; providers use {@link Provider}. */
public class User {
    public String id, name, email, username, password, phone, city;
    public Role role;

    public User(String id, Role role, String name, String email, String username,
                String password, String phone, String city) {
        this.id = id; this.role = role; this.name = name; this.email = email;
        this.username = username; this.password = password; this.phone = phone; this.city = city;
    }

    public String roleLabel() {
        return switch (role) { case OWNER -> "Property Owner"; case PROVIDER -> "Service Provider"; case ADMIN -> "Administrator"; };
    }
}

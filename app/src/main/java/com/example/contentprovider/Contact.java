package com.example.contentprovider;

public class Contact {
    private final String id;
    private final String name;
    private final String phoneNumber;

    public Contact(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getInitial() {
        if (name != null && !name.trim().isEmpty()) {
            return String.valueOf(name.trim().charAt(0)).toUpperCase();
        }
        return "?";
    }
}
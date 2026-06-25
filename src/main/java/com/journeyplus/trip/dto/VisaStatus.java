package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Visa status enumeration (mapped to DB values)")
public enum VisaStatus {
    NOT_REQUIRED("NOT_REQUIRED"),
    PENDING("PENDING"),
    APPLIED("APPLIED"),
    GRANTED("GRANTED"),
    REJECTED("REJECTED");

    private final String value;

    VisaStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VisaStatus fromValue(String value) {
        if (value == null) return null;
        String v = value.trim();
        for (VisaStatus s : VisaStatus.values()) {
            if (s.value.equalsIgnoreCase(v) || s.name().equalsIgnoreCase(v)) {
                return s;
            }
        }
        // Synonyms for backward compatibility
        switch (v.toUpperCase()) {
            case "NOTREQUIRED":
            case "EXEMPT":
            case "EXEMPTED":
                return NOT_REQUIRED;
            case "APPROVED":
                return GRANTED;
            default:
                throw new IllegalArgumentException("Unknown VisaStatus: " + value + ". Allowed: NOT_REQUIRED, PENDING, APPLIED, GRANTED, REJECTED");
        }
    }
}

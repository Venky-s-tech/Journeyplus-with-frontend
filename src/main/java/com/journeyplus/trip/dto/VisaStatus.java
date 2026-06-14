package com.journeyplus.trip.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Visa status enumeration (mapped to DB values)")
public enum VisaStatus {
    PENDING("PENDING"),
    APPLIED("APPLIED"),
    APPROVED("APPROVED"),
    EXEMPTED("EXEMPTED");

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
        // Accept some common synonyms for backward compatibility
        switch (v.toLowerCase()) {
            case "notrequired":
            case "not_required":
            case "exempt":
            case "exempted":
                return EXEMPTED;
            case "granted":
            case "approved":
                return APPROVED;
            case "rejected":
                // map rejected to applied? better to throw - but we map to PENDING as fallback
                throw new IllegalArgumentException("Unsupported status: " + value + ". Allowed: PENDING, APPLIED, APPROVED, EXEMPTED");
            default:
                throw new IllegalArgumentException("Unknown VisaStatus: " + value + ". Allowed: PENDING, APPLIED, APPROVED, EXEMPTED");
        }
    }
}

package com.journeyplus.iam.entity;

public enum Role {
    EMPLOYEE("G1"),
    TRAVEL_DESK("G2"),
    APPROVING_MANAGER("G3"),
    FINANCE("G4"),
    COMPLIANCE("G5"),
    ADMIN("G6");

    // Predefined role -> grade mapping used to auto-assign a grade when a role is selected.
    private final String defaultGradeId;

    Role(String defaultGradeId) {
        this.defaultGradeId = defaultGradeId;
    }

    public String getDefaultGradeId() {
        return defaultGradeId;
    }
}

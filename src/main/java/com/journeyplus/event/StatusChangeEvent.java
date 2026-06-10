package com.journeyplus.event;

public class StatusChangeEvent {

    private final Long userId;
    private final String title;
    private final String message;

    public StatusChangeEvent(Long userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}

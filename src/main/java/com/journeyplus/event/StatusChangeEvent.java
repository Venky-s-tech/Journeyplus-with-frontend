package com.journeyplus.event;

import com.journeyplus.notification.entity.NotificationCategory;

public class StatusChangeEvent {

    private final Long userId;
    private final String title;
    private final String message;
    private final Long actorId;
    private final String actorName;
    private final NotificationCategory category;

    public StatusChangeEvent(Long userId, String title, String message) {
        this(userId, title, message, null, null, NotificationCategory.TripRequest);
    }

    public StatusChangeEvent(Long userId, String title, String message, Long actorId, String actorName) {
        this(userId, title, message, actorId, actorName, NotificationCategory.TripRequest);
    }

    public StatusChangeEvent(Long userId, String title, String message, Long actorId, String actorName, NotificationCategory category) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.actorId = actorId;
        this.actorName = actorName;
        this.category = category;
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

    public Long getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public NotificationCategory getCategory() {
        return category;
    }
}

package com.journeyplus.event;

import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import com.journeyplus.notification.entity.Notification;
import com.journeyplus.notification.repository.NotificationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StatusChangeEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public StatusChangeEventListener(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void handleStatusChangeEvent(StatusChangeEvent event) {
        try {
            Long userId = event.getUserId();
            org.slf4j.LoggerFactory.getLogger(StatusChangeEventListener.class)
                .info("Received StatusChangeEvent for userId={} title={}", userId, event.getTitle());

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for notification: " + userId));

            Notification notification = new Notification(user, event.getTitle(), event.getMessage(), event.getActorId(), event.getActorName());
            if (event.getCategory() != null) {
                notification.setCategory(event.getCategory());
            }
            notificationRepository.save(notification);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(StatusChangeEventListener.class)
                .error("Failed to handle StatusChangeEvent: {}", e.getMessage());
        }
    }
}

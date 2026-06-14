package com.journeyplus.notification.repository;

import com.journeyplus.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n join fetch n.user u where u.id = :userId order by n.createdAt desc")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("select n from Notification n join fetch n.user u where u.id = :userId and n.read = false order by n.createdAt desc")
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(@Param("userId") Long userId);

}

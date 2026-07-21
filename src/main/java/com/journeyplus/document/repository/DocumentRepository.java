package com.journeyplus.document.repository;

import com.journeyplus.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwnerId(Long ownerId);
    List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<Document> findByEntityTypeAndEntityIdAndStatus(String entityType, Long entityId, String status);
}

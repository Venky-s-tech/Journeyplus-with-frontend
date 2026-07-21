package com.journeyplus.document.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "filename", nullable = false)
    private String filename; // Kept for backward compatibility

    @Column(name = "stored_file_name")
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "document_type", length = 50)
    private String documentType = "RECEIPT";

    @Column(name = "entity_type", length = 50)
    private String entityType = "EXPENSE_LINE";

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "status", length = 50)
    private String status = "UPLOADED"; // UPLOADED, REPLACED, DELETED

    @Column(name = "checksum", length = 100)
    private String checksum;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Document() {}

    public Document(String filename, String contentType, String path, Long ownerId) {
        this.originalFileName = filename;
        this.filename = filename;
        this.storedFileName = filename;
        this.contentType = contentType;
        this.path = path;
        this.ownerId = ownerId;
        this.documentType = "RECEIPT";
        this.entityType = "EXPENSE_LINE";
        this.status = "UPLOADED";
        this.createdAt = LocalDateTime.now();
    }

    public Document(String originalFileName, String storedFileName, String contentType, String path, Long fileSize, String documentType, String entityType, Long entityId, Long ownerId) {
        this.originalFileName = originalFileName;
        this.filename = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.path = path;
        this.fileSize = fileSize;
        this.documentType = documentType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.ownerId = ownerId;
        this.status = "UPLOADED";
        this.createdAt = LocalDateTime.now();
    }
}

package com.journeyplus.document.service;

import com.journeyplus.document.entity.Document;
import com.journeyplus.document.repository.DocumentRepository;
import com.journeyplus.document.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    public DocumentService(DocumentRepository documentRepository, StorageService storageService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public Document save(MultipartFile file, Long ownerId) throws IOException {
        String storedPath = storageService.store(file, file.getOriginalFilename());
        String storedFileName = Paths.get(storedPath).getFileName().toString();
        Document doc = new Document(
                file.getOriginalFilename(),
                storedFileName,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                storedPath,
                file.getSize(),
                "RECEIPT",
                "EXPENSE_LINE",
                null,
                ownerId
        );
        return documentRepository.save(doc);
    }

    @Transactional
    public Document saveEntityDocument(MultipartFile file, String documentType, String entityType, Long entityId, Long ownerId) throws IOException {
        // Mark existing active documents for this entity as REPLACED
        if (entityId != null) {
            List<Document> existing = documentRepository.findByEntityTypeAndEntityIdAndStatus(entityType, entityId, "UPLOADED");
            for (Document oldDoc : existing) {
                oldDoc.setStatus("REPLACED");
                documentRepository.save(oldDoc);
            }
        }

        String storedPath = storageService.store(file, file.getOriginalFilename());
        String storedFileName = Paths.get(storedPath).getFileName().toString();
        
        Document doc = new Document(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document",
                storedFileName,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                storedPath,
                file.getSize(),
                documentType != null ? documentType : "RECEIPT",
                entityType != null ? entityType : "EXPENSE_LINE",
                entityId,
                ownerId
        );
        doc.setStatus("UPLOADED");
        return documentRepository.save(doc);
    }

    public byte[] loadContent(Document doc) throws IOException {
        return storageService.load(doc.getPath());
    }

    public List<Document> listForOwner(Long ownerId) {
        return documentRepository.findByOwnerId(ownerId);
    }

    public Optional<Document> findActiveDocumentForEntity(String entityType, Long entityId) {
        List<Document> docs = documentRepository.findByEntityTypeAndEntityIdAndStatus(entityType, entityId, "UPLOADED");
        if (docs != null && !docs.isEmpty()) {
            return Optional.of(docs.get(0));
        }
        return Optional.empty();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    @Transactional
    public boolean deleteEntityDocument(String entityType, Long entityId) throws IOException {
        List<Document> docs = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        boolean deletedAny = false;
        for (Document doc : docs) {
            doc.setStatus("DELETED");
            documentRepository.save(doc);
            try {
                storageService.delete(doc.getPath());
            } catch (Exception e) {}
            deletedAny = true;
        }
        return deletedAny;
    }
}

package com.journeyplus.document.service;

import com.journeyplus.document.entity.Document;
import com.journeyplus.document.repository.DocumentRepository;
import com.journeyplus.document.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    public DocumentService(DocumentRepository documentRepository, StorageService storageService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    public Document save(MultipartFile file, Long ownerId) throws IOException {
        String storedPath = storageService.store(file, System.currentTimeMillis() + "_" + file.getOriginalFilename());
        Document doc = new Document(file.getOriginalFilename(), file.getContentType(), storedPath, ownerId);
        return documentRepository.save(doc);
    }

    public byte[] loadContent(Document doc) throws IOException {
        return storageService.load(doc.getPath());
    }

    public List<Document> listForOwner(Long ownerId) {
        return documentRepository.findByOwnerId(ownerId);
    }

    public Document findById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }
}

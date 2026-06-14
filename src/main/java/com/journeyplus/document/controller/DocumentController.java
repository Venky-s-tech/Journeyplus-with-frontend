package com.journeyplus.document.controller;

import com.journeyplus.document.entity.Document;
import com.journeyplus.document.service.DocumentService;
import com.journeyplus.iam.service.UserService;
import com.journeyplus.iam.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLOYEE','TRAVEL_DESK_COORDINATOR','TRAVEL_ADMIN')")
    public ResponseEntity<Document> upload(@RequestParam("file") MultipartFile file) throws IOException {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        Document saved = documentService.save(file, currentUser.getId());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable Long id) throws IOException {
        Document doc = documentService.findById(id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());

        boolean allowed = currentUser.getId().equals(doc.getOwnerId())
                || auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRAVEL_ADMIN") || a.getAuthority().equals("ROLE_FINANCE_EXECUTIVE") || a.getAuthority().equals("ROLE_COMPLIANCE_OFFICER") || a.getAuthority().equals("ROLE_APPROVING_MANAGER"));

        if (!allowed) {
            return ResponseEntity.status(403).build();
        }

        byte[] data = documentService.loadContent(doc);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType() == null ? "application/octet-stream" : doc.getContentType()))
                .body(data);
    }

    @GetMapping("")
    public ResponseEntity<List<Document>> listOwn() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByUsername(auth.getName());
        return ResponseEntity.ok(documentService.listForOwner(currentUser.getId()));
    }
}

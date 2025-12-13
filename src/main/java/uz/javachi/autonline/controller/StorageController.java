package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.javachi.autonline.service.StorageService;

import java.net.URLConnection;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) {
        try {
            String key = storageService.uploadFile(file, "images").get();
            return ResponseEntity.ok(key);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/file")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'TEACHER')")
    public ResponseEntity<byte[]> getFile(@RequestParam String key) {
        byte[] fileData = storageService.downloadFile(key);

        String contentType = URLConnection.guessContentTypeFromName(key);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileData);
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<String> delete(@RequestParam String key) {
        storageService.deleteFile(key);
        return ResponseEntity.ok("Deleted");
    }
}

package com.kh.login.controller;

import com.kh.login.domain.FileEntity;
import com.kh.login.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload-url")
    public ResponseEntity<UploadUrlResponse> getUploadUrl(@RequestParam String fileName,
                                                        @RequestParam String contentType,
                                                        @RequestParam(required = false, defaultValue = "") String path) {
        // 파일 확장자 추출
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex);
        }
        
        // changeName에 path 포함
        String changeName = path + UUID.randomUUID().toString() + extension;
        String presignedUrl = fileService.generatePresignedUploadUrl(changeName, contentType);
        
        // 파일 메타데이터 저장 (originalName에는 fileName만 저장)
        FileEntity savedFile = fileService.saveFileInfo(fileName, changeName, contentType);
        
        return ResponseEntity.ok(new UploadUrlResponse(presignedUrl, savedFile.getId()));
    }

    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(@PathVariable Long fileId) {
        FileEntity file = fileService.getFile(fileId);
        String presignedUrl = fileService.generatePresignedDownloadUrl(file.getChangeName());
        
        return ResponseEntity.ok(new DownloadUrlResponse(presignedUrl, file.getOriginalName()));
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    // Response DTOs
    record UploadUrlResponse(String presignedUrl, Long fileId) {}
    record DownloadUrlResponse(String presignedUrl, String originalFileName) {}
}

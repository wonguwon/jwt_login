package com.kh.login.controller;

import com.kh.login.domain.FileEntity;
import com.kh.login.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.kh.login.dto.file.UploadUrlResponseDto;
import com.kh.login.dto.file.DownloadUrlResponseDto;
import com.kh.login.dto.file.CompleteUploadRequestDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload-url")
    public ResponseEntity<UploadUrlResponseDto> getUploadUrl(@RequestParam String file_name,
                                                        @RequestParam String content_type,
                                                        @RequestParam(required = false, defaultValue = "") String path) {
        // 파일 확장자 추출
        String extension = "";
        int lastDotIndex = file_name.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = file_name.substring(lastDotIndex);
        }
        
        // changeName에 path 포함
        String changeName = path + UUID.randomUUID().toString() + extension;
        String presignedUrl = fileService.generatePresignedUploadUrl(changeName, content_type);
        
        // Presigned URL만 반환 (DB 저장 X)
        return ResponseEntity.ok(new UploadUrlResponseDto(presignedUrl, changeName));
    }

    // 업로드 완료 후 파일 정보 저장 API
    @PostMapping("/complete")
    public ResponseEntity<FileEntity> completeUpload(@RequestBody CompleteUploadRequestDto request) {
        System.out.println(request);
        FileEntity savedFile = fileService.saveFileInfo(request.getOriginal_name(), request.getChange_name(), request.getContent_type());
        return ResponseEntity.ok(savedFile);
    }

    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<DownloadUrlResponseDto> getDownloadUrl(@PathVariable Long fileId) {
        FileEntity file = fileService.getFile(fileId);
        String presignedUrl = fileService.generatePresignedDownloadUrl(file.getChangeName());
        
        return ResponseEntity.ok(new DownloadUrlResponseDto(presignedUrl, file.getOriginalName()));
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }
}

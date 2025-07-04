package com.kh.login.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteUploadRequestDto {
    private String original_name;
    private String change_name;
    private String content_type;
} 
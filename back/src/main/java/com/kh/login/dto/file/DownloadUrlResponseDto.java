package com.kh.login.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadUrlResponseDto {
    private String presigned_url;
    private String original_file_name;
} 
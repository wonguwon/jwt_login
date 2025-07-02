# Spring 서버 기반 S3 Presigned URL 파일 업로드/다운로드 교안

## 1. AWS S3 및 IAM 설정

### 1-1. S3 버킷 생성
- AWS 콘솔 → S3 → 버킷 만들기
- 버킷 이름 예시: `my-bucket-354918403865`
- 퍼블릭 접근 차단(권장), 필요시 CORS 정책 추가

### 1-2. IAM 사용자 및 권한
- AWS 콘솔 → IAM → 사용자 추가
- S3 전체 접근 권한(예: `AmazonS3FullAccess`) 부여
- Access Key, Secret Key 발급

### 1-3. CORS 설정 예시
```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": []
  }
]
```

---

## 2. Spring Boot 서버 설정

### 2-1. 의존성 추가 (`build.gradle`)
```groovy
implementation 'software.amazon.awssdk:s3:2.20.26'
implementation 'software.amazon.awssdk:s3-transfer-manager:2.20.26'
```

### 2-2. application.yml 설정
```yaml
aws:
  region: ap-northeast-3
  credentials:
    access-key: <발급받은 Access Key>
    secret-key: <발급받은 Secret Key>
  s3:
    bucket: my-bucket-354918403865
```

### 2-3. S3Config.java
```java
@Configuration
public class S3Config {
    @Value("${aws.region}") private String region;
    @Value("${aws.credentials.access-key}") private String accessKey;
    @Value("${aws.credentials.secret-key}") private String secretKey;

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
```

---

## 3. Presigned URL 발급 및 파일 메타데이터 관리

### 3-1. FileEntity.java (DB에 파일 정보 저장)
```java
@Entity
@Table(name = "files")
public class FileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalName;
    private String changeName;
    private String contentType;
    @CreationTimestamp
    private Timestamp createdAt;
}
```

### 3-2. FileService.java (Presigned URL 발급)
```java
public String generatePresignedUploadUrl(String fileName, String contentType) {
    PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(contentType)
            .build();

    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
            r -> r.putObjectRequest(objectRequest)
                    .signatureDuration(Duration.ofMinutes(5))
    );
    return presignedRequest.url().toString();
}

public String generatePresignedDownloadUrl(String fileName) {
    return s3Presigner.presignGetObject(r -> r.getObjectRequest(get -> get
            .bucket(bucket)
            .key(fileName))
            .signatureDuration(Duration.ofMinutes(5)))
            .url()
            .toString();
}
```

### 3-3. FileController.java (API 엔드포인트)
```java
@PostMapping("/upload-url")
public ResponseEntity<UploadUrlResponse> getUploadUrl(@RequestParam String fileName, @RequestParam String contentType, @RequestParam(required = false, defaultValue = "") String path) {
    String extension = fileName.substring(fileName.lastIndexOf("."));
    String changeName = path + UUID.randomUUID().toString() + extension;
    String presignedUrl = fileService.generatePresignedUploadUrl(changeName, contentType);
    FileEntity savedFile = fileService.saveFileInfo(fileName, changeName, contentType);
    return ResponseEntity.ok(new UploadUrlResponse(presignedUrl, savedFile.getId()));
}

@GetMapping("/{fileId}/download-url")
public ResponseEntity<DownloadUrlResponse> getDownloadUrl(@PathVariable Long fileId) {
    FileEntity file = fileService.getFile(fileId);
    String presignedUrl = fileService.generatePresignedDownloadUrl(file.getChangeName());
    return ResponseEntity.ok(new DownloadUrlResponse(presignedUrl, file.getOriginalName()));
}
```

---

## 4. 프론트엔드 연동 (React 예시)

### 4-1. Presigned URL 요청 및 S3 업로드

#### fileApi.js
```js
export const getUploadUrl = async (fileName, contentType, path = '') => {
    const response = await axiosInstance.post('/api/files/upload-url', null, {
        params: { fileName, contentType, path }
    });
    return response.data;
};

export const uploadFileToS3 = async (presignedUrl, file) => {
    await fetch(presignedUrl, {
        method: 'PUT',
        body: file,
        headers: { 'Content-Type': file.type }
    });
};
```

#### FileUpload.jsx (핵심 로직)
```js
const handleUpload = async (path = '') => {
    if (!selectedFile) return;
    setIsUploading(true);
    try {
        // 1. Presigned URL 발급
        const { presignedUrl, fileId } = await getUploadUrl(selectedFile.name, selectedFile.type, path);
        // 2. S3에 파일 업로드
        await uploadFileToS3(presignedUrl, selectedFile);
        // 3. 파일 목록 새로고침 등 후처리
    } catch (error) {
        alert('파일 업로드에 실패했습니다.');
    }
    setIsUploading(false);
};
```

### 4-2. Presigned URL로 파일 다운로드
```js
const handleDownload = async (fileId, originalName) => {
    const { presignedUrl } = await getDownloadUrl(fileId);
    window.open(presignedUrl, '_blank');
};
```

---

## 5. 전체 흐름 요약

1. **프론트엔드**에서 파일 선택 → 백엔드에 Presigned URL 요청
2. **백엔드(Spring)**에서 S3 Presigned URL 생성 후 응답
3. **프론트엔드**에서 해당 URL로 S3에 직접 파일 업로드
4. 파일 메타데이터는 DB에 저장되어 관리
5. 다운로드 시 Presigned URL을 받아 S3에서 직접 다운로드

---

## 6. 보안 및 실전 팁

- Presigned URL의 유효기간은 짧게(5~10분) 설정
- S3 버킷은 퍼블릭 차단, Presigned URL로만 접근 허용
- 파일명은 UUID 등으로 난수화하여 저장(원본명은 DB에 따로 저장)
- IAM 권한 최소화, 키 노출 주의 
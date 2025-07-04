import React, { useState, useEffect } from 'react';
import styled from 'styled-components';
import { getUploadUrl, uploadFileToS3, getAllFiles, getDownloadUrl, completeUpload } from '../api/fileApi';

const CLOUDFRONT_URL = 'https://dwxo8vkl18znf.cloudfront.net/';

const Container = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
`;

const Title = styled.h2`
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #333;
`;

const UploadSection = styled.div`
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 2px dashed #dee2e6;
`;

const FileInput = styled.input`
  display: block;
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background: white;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
`;

const UploadButton = styled.button`
  padding: 0.75rem 1.5rem;
  font-size: 0.875rem;
  color: white;
  background: #28a745;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover:not(:disabled) {
    background: #218838;
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

const ProfileButton = styled.button`
  padding: 0.75rem 1.5rem;
  font-size: 0.875rem;
  color: white;
  background: #007bff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover:not(:disabled) {
    background: #0056b3;
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

const UploadStatus = styled.p`
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #6c757d;
`;

const FileListSection = styled.div`
  margin-top: 2rem;
`;

const FileListTitle = styled.h3`
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
`;

const FileItem = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 0.5rem;
  border: 1px solid #e9ecef;
`;

const FileName = styled.span`
  font-size: 0.875rem;
  color: #495057;
  flex: 1;
`;

const DownloadButton = styled.button`
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  color: #007bff;
  background: transparent;
  border: 1px solid #007bff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: #007bff;
    color: white;
  }
`;

const EmptyMessage = styled.p`
  text-align: center;
  font-size: 0.875rem;
  color: #6c757d;
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 6px;
`;

const ButtonWrapper = styled.div`
  display: flex;
  gap: 0.5rem;
`;

const isImage = (fileName) => /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(fileName);

const FileUpload = () => {
    const [files, setFiles] = useState([]);
    const [isUploading, setIsUploading] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);

    useEffect(() => {
        loadFiles();
    }, []);

    const loadFiles = async () => {
        try {
            const fileList = await getAllFiles();
            setFiles(fileList);
        } catch (error) {
            console.error('파일 목록 로딩 실패:', error);
        }
    };

    const handleFileSelect = (event) => {
        const file = event.target.files[0];
        setSelectedFile(file);
    };

    const handleUpload = async (path = '') => {
        if (!selectedFile) return;

        setIsUploading(true);
        try {
            // 1. Presigned URL 발급 (path와 fileName 분리해서 전송)
            const { presignedUrl, changeName } = await getUploadUrl(selectedFile.name, selectedFile.type, path);
            // 2. S3에 파일 업로드
            await uploadFileToS3(presignedUrl, selectedFile);
            // 3. 업로드 완료 후 DB 저장
            await completeUpload(selectedFile.name, changeName, selectedFile.type);
            // 4. 파일 목록 새로고침
            await loadFiles();
            // 5. 파일 선택 초기화
            setSelectedFile(null);
            alert('파일이 성공적으로 업로드되었습니다.');
        } catch (error) {
            console.error('파일 업로드 실패:', error);
            alert('파일 업로드에 실패했습니다.');
        } finally {
            setIsUploading(false);
        }
    };

    const handleDownload = async (fileId, originalName) => {
        try {
            const { presignedUrl } = await getDownloadUrl(fileId);
            
            // 파일 다운로드
            const response = await fetch(presignedUrl);
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = originalName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error('파일 다운로드 실패:', error);
            alert('파일 다운로드에 실패했습니다.');
        }
    };

    const handlePreview = (changeName) => {
        setPreviewUrl(CLOUDFRONT_URL + changeName);
    };

    return (
        <Container>
            <Title>파일 업로드/다운로드</Title>
            
            {/* 파일 업로드 */}
            <UploadSection>
                <FileInput
                    type="file"
                    onChange={handleFileSelect}
                    disabled={isUploading}
                />
                <ButtonGroup>
                    <UploadButton 
                        onClick={() => handleUpload("")} 
                        disabled={!selectedFile || isUploading}
                    >
                        일반 업로드
                    </UploadButton>
                    <ProfileButton 
                        onClick={() => handleUpload("user-profile/")} 
                        disabled={!selectedFile || isUploading}
                    >
                        프로필 업로드
                    </ProfileButton>
                </ButtonGroup>
                {isUploading && <UploadStatus>업로드 중...</UploadStatus>}
            </UploadSection>

            {/* 파일 목록 */}
            <FileListSection>
                <FileListTitle>파일 목록</FileListTitle>
                {files.length > 0 ? (
                    files.map((file) => (
                        <FileItem key={file.id}>
                            <FileName>{file.originalName}</FileName>
                            <ButtonWrapper>
                                {isImage(file.originalName) && (
                                    <DownloadButton onClick={() => handlePreview(file.changeName)}>
                                        미리보기
                                    </DownloadButton>
                                )}
                                <DownloadButton
                                    onClick={() => handleDownload(file.id, file.originalName)}
                                >
                                    다운로드
                                </DownloadButton>
                            </ButtonWrapper>
                        </FileItem>
                    ))
                ) : (
                    <EmptyMessage>업로드된 파일이 없습니다.</EmptyMessage>
                )}
            </FileListSection>

            {/* 미리보기 모달 */}
            {previewUrl && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
                    background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
                }}>
                    <div style={{ background: '#fff', padding: 20, borderRadius: 8, textAlign: 'center' }}>
                        <img src={previewUrl} alt="미리보기" style={{ maxWidth: 500, maxHeight: 500 }} />
                        <div>
                            <button onClick={() => setPreviewUrl(null)} style={{ marginTop: 10 }}>닫기</button>
                        </div>
                    </div>
                </div>
            )}
        </Container>
    );
};

export default FileUpload; 
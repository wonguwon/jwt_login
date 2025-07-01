import axiosInstance from './axiosInstance';

// 파일 업로드용 presigned url 발급
export const getPresignedUploadUrl = async ({ filename, contentType }) => {
  try {
    const formData = new URLSearchParams();
    formData.append('filename', filename);
    formData.append('contentType', contentType);
    const response = await axiosInstance.post('/api/files/upload-url', formData, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
    if (response.data && response.data.url) {
      return response.data.url;
    } else {
      throw new Error('Presigned URL 발급 실패');
    }
  } catch (error) {
    console.error('Presigned URL 발급 실패:', error);
    throw error;
  }
};

export const getUploadUrl = async (fileName, contentType, path = '') => {
    const response = await axiosInstance.post('/api/files/upload-url', null, {
        params: {
            fileName,
            contentType,
            path
        }
    });
    return response.data;
};

export const uploadFileToS3 = async (presignedUrl, file) => {
    await fetch(presignedUrl, {
        method: 'PUT',
        body: file,
        headers: {
            'Content-Type': file.type
        }
    });
};

export const getDownloadUrl = async (fileId) => {
    const response = await axiosInstance.get(`/api/files/${fileId}/download-url`);
    return response.data;
};

export const getAllFiles = async () => {
    const response = await axiosInstance.get('/api/files');
    return response.data;
}; 
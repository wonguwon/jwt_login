import apiClient from './axiosInstance'

// 내 정보 조회 (JWT 토큰 사용)
export const getMyInfo = async () => {
  try {
    const response = await apiClient.get('/v1/member/me')
    return response.data
  } catch (error) {
    console.error('내 정보 조회 실패:', error)
    throw error
  }
}

// 로그인
export const login = async (loginData) => {
  try {
    const response = await apiClient.post('/v1/member/login', loginData)
    return response.data
  } catch (error) {
    console.error('로그인 실패:', error)
    throw error
  }
}

// 회원가입
export const signup = async (signupData) => {
  try {
    const response = await apiClient.post('/v1/member/signup', signupData)
    return response.data
  } catch (error) {
    console.error('회원가입 실패:', error)
    throw error
  }
}

export const sendEmailCode = async (email) => {
  try {
    const response = await apiClient.post('/auth/email/send', { email });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const verifyEmailCode = async (email, code) => {
  try {
    const response = await apiClient.post('/auth/email/verify', { email, code });
    return response.data;
  } catch (error) {
    throw error;
  }
}; 

// 회원 목록 조회
export const getMemberList = async () => {
  try {
    const response = await apiClient.get('/v1/member/list');
    return response.data;
  } catch (error) {
    console.error('회원 목록 조회 실패:', error);
    throw error;
  }
};

// 그룹 채팅방 생성
export const createGroupChatRoom = async (roomName) => {
  try {
    await apiClient.post(`/v1/chat/room/group/create?roomName=${encodeURIComponent(roomName)}`);
  } catch (error) {
    console.error('그룹 채팅방 생성 실패:', error);
    throw error;
  }
};

// 나의 채팅방 목록 조회
export const getMyChatRooms = async () => {
  try {
    const response = await apiClient.get('/v1/chat/my/rooms');
    return response.data;
  } catch (error) {
    console.error('나의 채팅방 목록 조회 실패:', error);
    throw error;
  }
};

// 그룹 채팅방 나가기
export const leaveGroupChatRoom = async (roomId) => {
  try {
    await apiClient.delete(`/v1/chat/room/group/${roomId}/leave`);
  } catch (error) {
    console.error('그룹 채팅방 나가기 실패:', error);
    throw error;
  }
}; 
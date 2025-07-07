import apiClient from './axiosInstance';

// 개인 채팅방 생성 (상대방 ID)
export const startPrivateChat = async (otherMemberId) => {
  try {
    const response = await apiClient.post(`/v1/chat/room/private/create?other_member_id=${otherMemberId}`);
    return response.data; // roomId 반환
  } catch (error) {
    console.error('개인 채팅 시작 실패:', error);
    throw error;
  }
};

// 그룹 채팅방 목록 조회
export const getGroupChatRooms = async () => {
  try {
    const response = await apiClient.get('/v1/chat/room/group/list');
    return response.data;
  } catch (error) {
    console.error('그룹 채팅방 목록 조회 실패:', error);
    throw error;
  }
};

// 그룹 채팅방 참여
export const joinGroupChatRoom = async (roomId) => {
  try {
    await apiClient.post(`/v1/chat/room/group/${roomId}/join`);
  } catch (error) {
    console.error('그룹 채팅방 참여 실패:', error);
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
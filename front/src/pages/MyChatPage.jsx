import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { getMyChatRooms, leaveGroupChatRoom } from '../api/chatApi';

const Container = styled.div`
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
`;
const Card = styled.div`
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 2rem;
`;
const Title = styled.h2`
  text-align: center;
  margin-bottom: 1.5rem;
`;
const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
`;
const Th = styled.th`
  padding: 12px;
  text-align: left;
  border-bottom: 2px solid #dee2e6;
  background: #f8f9fa;
`;
const Td = styled.td`
  padding: 12px;
  border-bottom: 1px solid #dee2e6;
`;
const ButtonGroup = styled.div`
  display: flex;
  gap: 8px;
`;
const EnterButton = styled.button`
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #115293;
  }
`;
const LeaveButton = styled.button`
  background: #6c757d;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #5a6268;
  }
  &:disabled {
    background: #e9ecef;
    color: #6c757d;
    cursor: not-allowed;
  }
`;
const BackButton = styled.button`
  background: #e3e3e3;
  color: #1976d2;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  margin-bottom: 1.5rem;
  margin-top: 0;
  transition: background 0.2s;
  &:hover {
    background: #bbdefb;
  }
`;

const MyChatPage = () => {
  const [chatList, setChatList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchMyChats();
  }, []);

  const fetchMyChats = async () => {
    try {
      const data = await getMyChatRooms();
      setChatList(data);
    } catch (error) {
      console.error('내 채팅 목록 로드 실패:', error);
    }
  };

  const handleEnterChatRoom = (roomId) => {
    navigate(`/chatpage/${roomId}`);
  };

  const handleLeaveChatRoom = async (roomId, isGroupChat) => {
    if (isGroupChat !== 'Y') return;
    try {
      await leaveGroupChatRoom(roomId);
      setChatList(chatList.filter(chat => chat.roomId !== roomId));
    } catch (error) {
      console.error('채팅방 나가기 실패:', error);
      alert('채팅방 나가기에 실패했습니다.');
    }
  };

  return (
    <Container>
      <BackButton onClick={() => navigate(-1)}>← 뒤로가기</BackButton>
      <Card>
        <Title>나의 채팅 목록</Title>
        <Table>
          <thead>
            <tr>
              <Th>채팅방 이름</Th>
              <Th>읽지 않은 메시지</Th>
              <Th>액션</Th>
            </tr>
          </thead>
          <tbody>
            {chatList.map((chat) => (
              <tr key={chat.roomId}>
                <Td>{chat.roomName}</Td>
                <Td>{chat.unReadCount}</Td>
                <Td>
                  <ButtonGroup>
                    <EnterButton onClick={() => handleEnterChatRoom(chat.roomId)}>
                      입장
                    </EnterButton>
                    <LeaveButton
                      disabled={chat.isGroupChat !== 'Y'}
                      onClick={() => handleLeaveChatRoom(chat.roomId, chat.isGroupChat)}
                    >
                      나가기
                    </LeaveButton>
                  </ButtonGroup>
                </Td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>
    </Container>
  );
};

export default MyChatPage; 
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { getGroupChatRooms, joinGroupChatRoom, createGroupChatRoom } from '../api/chatApi';

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
  display: flex;
  justify-content: space-between;
  align-items: center;
`;
const CreateButton = styled.button`
  background: #6c757d;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #5a6268;
  }
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
const JoinButton = styled.button`
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
const Modal = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;
const ModalCard = styled.div`
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 100%;
  max-width: 400px;
`;
const ModalTitle = styled.h3`
  margin-bottom: 16px;
`;
const ModalInput = styled.input`
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  margin-bottom: 16px;
`;
const ModalButtons = styled.div`
  display: flex;
  gap: 8px;
  justify-content: flex-end;
`;
const CancelButton = styled.button`
  background: #6c757d;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #5a6268;
  }
`;
const ConfirmButton = styled.button`
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #115293;
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

const GroupChattingList = () => {
  const [chatRooms, setChatRooms] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newRoomTitle, setNewRoomTitle] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchChatRooms();
  }, []);

  const fetchChatRooms = async () => {
    try {
      const data = await getGroupChatRooms();
      setChatRooms(data);
    } catch (error) {
      console.error('채팅방 목록 로드 실패:', error);
    }
  };

  const handleJoinChatRoom = async (roomId) => {
    try {
      await joinGroupChatRoom(roomId);
      navigate(`/chatpage/${roomId}`);
    } catch (error) {
      console.error('채팅방 참여 실패:', error);
      alert('채팅방 참여에 실패했습니다.');
    }
  };

  const handleCreateChatRoom = async () => {
    if (!newRoomTitle.trim()) {
      alert('방제목을 입력해주세요.');
      return;
    }
    try {
      await createGroupChatRoom(newRoomTitle);
      setShowCreateModal(false);
      setNewRoomTitle('');
      fetchChatRooms();
    } catch (error) {
      console.error('채팅방 생성 실패:', error);
      alert('채팅방 생성에 실패했습니다.');
    }
  };

  return (
    <Container>
      <BackButton onClick={() => navigate(-1)}>← 뒤로가기</BackButton>
      <Card>
        <Title>
          채팅방목록
          <CreateButton onClick={() => setShowCreateModal(true)}>
            채팅방 생성
          </CreateButton>
        </Title>
        <Table>
          <thead>
            <tr>
              <Th>방번호</Th>
              <Th>방제목</Th>
              <Th>채팅</Th>
            </tr>
          </thead>
          <tbody>
            {chatRooms.map((room) => (
              <tr key={room.roomId}>
                <Td>{room.roomId}</Td>
                <Td>{room.roomName}</Td>
                <Td>
                  <JoinButton onClick={() => handleJoinChatRoom(room.roomId)}>
                    참여하기
                  </JoinButton>
                </Td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>
      {showCreateModal && (
        <Modal>
          <ModalCard>
            <ModalTitle>채팅방 생성</ModalTitle>
            <ModalInput
              type="text"
              placeholder="방제목"
              value={newRoomTitle}
              onChange={(e) => setNewRoomTitle(e.target.value)}
            />
            <ModalButtons>
              <CancelButton onClick={() => setShowCreateModal(false)}>
                취소
              </CancelButton>
              <ConfirmButton onClick={handleCreateChatRoom}>
                생성
              </ConfirmButton>
            </ModalButtons>
          </ModalCard>
        </Modal>
      )}
    </Container>
  );
};

export default GroupChattingList; 
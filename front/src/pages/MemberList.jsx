import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { getMemberList } from '../api/memberApi';
import { startPrivateChat } from '../api/chatApi';

const Container = styled.div`
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
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
const ChatButton = styled.button`
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

const MemberList = () => {
  const [memberList, setMemberList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchMembers();
  }, []);

  const fetchMembers = async () => {
    try {
      const data = await getMemberList();
      setMemberList(data);
    } catch (error) {
      console.error('회원 목록 로드 실패:', error);
    }
  };

  const handleStartChat = async (otherMemberId) => {
    try {
      const roomId = await startPrivateChat(otherMemberId);
      navigate(`/chatpage/${roomId}`);
    } catch (error) {
      console.error('개인 채팅 시작 실패:', error);
      alert('채팅 시작에 실패했습니다.');
    }
  };

  return (
    <Container>
      <BackButton onClick={() => navigate(-1)}>← 뒤로가기</BackButton>
      <Card>
        <Title>회원목록</Title>
        <Table>
          <thead>
            <tr>
              <Th>ID</Th>
              <Th>이름</Th>
              <Th>이메일</Th>
              <Th>채팅</Th>
            </tr>
          </thead>
          <tbody>
            {memberList.map((member) => (
              <tr key={member.id}>
                <Td>{member.id}</Td>
                <Td>{member.name}</Td>
                <Td>{member.email}</Td>
                <Td>
                  <ChatButton onClick={() => handleStartChat(member.id)}>
                    채팅하기
                  </ChatButton>
                </Td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>
    </Container>
  );
};

export default MemberList; 
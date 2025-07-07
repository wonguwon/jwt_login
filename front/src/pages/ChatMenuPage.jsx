import React from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const Container = styled.div`
  max-width: 600px;
  margin: 3rem auto;
  padding: 2rem;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  text-align: center;
`;
const Title = styled.h2`
  margin-bottom: 2rem;
  color: #1976d2;
`;
const ButtonGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  align-items: center;
`;
const MenuButton = styled.button`
  background: #1976d2;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 1rem 2.5rem;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.2s;
  width: 80%;
  max-width: 320px;
  &:hover {
    background: #1565c0;
  }
`;

const ChatMenuPage = () => {
  const navigate = useNavigate();
  return (
    <Container>
      <Title>채팅 메뉴</Title>
      <ButtonGroup>
        <MenuButton onClick={() => navigate('/member/list')}>회원목록</MenuButton>
        <MenuButton onClick={() => navigate('/chat/rooms')}>채팅방목록</MenuButton>
        <MenuButton onClick={() => navigate('/mychats')}>나의 채팅목록</MenuButton>
      </ButtonGroup>
    </Container>
  );
};

export default ChatMenuPage; 
import React, { useState, useEffect, useRef } from 'react';
import styled from 'styled-components';
import { useParams } from 'react-router-dom';

const Wrapper = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
`;
const Card = styled.div`
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.08);
  padding: 2rem;
  width: 100%;
  max-width: 500px;
`;
const Title = styled.h2`
  text-align: center;
  margin-bottom: 1.5rem;
`;
const MessageBox = styled.div`
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
`;
const Message = styled.div`
  font-size: 1rem;
  margin-bottom: 8px;
`;
const FormRow = styled.div`
  display: flex;
  gap: 8px;
`;
const Input = styled.input`
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
`;
const Button = styled.button`
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  &:hover {
    background: #115293;
  }
`;

function getWsUrl() {
  //const base = import.meta.env.VITE_API_BASE_URL;/
  const base = "http://localhost:8001";
  return base.replace(/^http/, 'ws') + '/connect';
}

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [ws, setWs] = useState(null);
  const messageBoxRef = useRef(null);
  const { roomId } = useParams();
  const senderEmail = sessionStorage.getItem('email');

  useEffect(() => {
    const websocket = new WebSocket(getWsUrl());
    websocket.onopen = () => {};
    websocket.onmessage = (event) => {
      setMessages(prev => [...prev, event.data]);
    };
    websocket.onclose = () => {};
    setWs(websocket);
    return () => {
      websocket.close();
    };
  }, []);

  useEffect(() => {
    if (messageBoxRef.current) {
      messageBoxRef.current.scrollTop = messageBoxRef.current.scrollHeight;
    }
  }, [messages]);

  const sendMessage = () => {
    if (newMessage.trim() && ws) {
      ws.send(JSON.stringify({
        roomId: Number(roomId),
        senderEmail: senderEmail,
        message: newMessage
      }));
      setNewMessage('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  return (
    <Wrapper>
      <Card>
        <Title>간단한 WebSocket 채팅</Title>
        <MessageBox ref={messageBoxRef}>
          {messages.map((msg, index) => (
            <Message key={index}>{msg}</Message>
          ))}
        </MessageBox>
        <FormRow>
          <Input
            type="text"
            placeholder="메시지 입력"
            value={newMessage}
            onChange={e => setNewMessage(e.target.value)}
            onKeyPress={handleKeyPress}
          />
          <Button onClick={sendMessage}>전송</Button>
        </FormRow>
      </Card>
    </Wrapper>
  );
};

export default ChatPage; 
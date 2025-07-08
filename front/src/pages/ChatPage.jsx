import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { getChatHistory, readChatRoom } from '../api/chatApi';

const Wrapper = styled.div`
  min-height: calc(100vh - 70px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  position: relative;
`;
const Card = styled.div`
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.08);
  padding: 2rem;
  width: 100%;
  max-width: 600px;
`;
const Title = styled.h2`
  text-align: center;
  margin-bottom: 1.5rem;
`;
const ChatBox = styled.div`
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
`;
const ChatMessage = styled.div`
  margin-bottom: 10px;
  text-align: left;
`;
const Sender = styled.strong`
  color: ${props => props.me ? '#43a047' : '#1976d2'};
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
const BackButton = styled.button`
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: #e3e3e3;
  color: #1976d2;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;
  &:hover {
    background: #bbdefb;
  }
`;

function getWsUrl(roomId) {
  const base = "http://localhost:8001";
  const token = sessionStorage.getItem('token');
  return base.replace(/^http/, 'ws') + `/connect?roomId=${roomId}&token=${token}`;
}

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [ws, setWs] = useState(null);
  const [senderEmail, setSenderEmail] = useState('');
  const { roomId } = useParams();
  const chatBoxRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const email = sessionStorage.getItem("email");
    setSenderEmail(email);
    
    const loadChatHistory = async () => {
      try {
        const data = await getChatHistory(roomId);
        setMessages(data);
      } catch (error) {
        console.error(error);
      }
    };
    loadChatHistory();
    connectWebsocket();
    
    return () => {
      disconnectWebSocket();
    };
    
  }, [roomId]);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  const connectWebsocket = () => {
    const websocket = new WebSocket(getWsUrl(roomId));
    websocket.onopen = () => {
      console.log('WebSocket 연결됨');
    };
    websocket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        setMessages(prev => [...prev, message]);
      } catch (error) {
        console.error('메시지 파싱 실패:', error);
      }
    };
    websocket.onclose = () => {
      console.log('WebSocket 연결 종료');
    };
    setWs(websocket);
  };

  const sendMessage = () => {
    if (newMessage.trim() === "" || !ws) return;
    const message = {
      roomId: Number(roomId),
      senderEmail: senderEmail,
      message: newMessage
    };
    ws.send(JSON.stringify(message));
    setNewMessage('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  const disconnectWebSocket = async () => {
    try {
      await readChatRoom(roomId);
    } catch (error) {
      console.error(error)
    }
    if (ws) {
      ws.close();
    }
  };

  return (
    <Wrapper>
      <BackButton onClick={() => navigate(-1)}>← 뒤로가기</BackButton>
      <Card>
        <Title>채팅</Title>
        <ChatBox ref={chatBoxRef}>
          {messages.map((msg, index) => (
            <ChatMessage key={index}>
              <Sender me={msg.senderEmail === senderEmail}>{msg.senderEmail}: </Sender>{msg.message}
            </ChatMessage>
          ))}
        </ChatBox>
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
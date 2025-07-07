import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import apiClient from '../api/axiosInstance';

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
  text-align: ${props => (props.sent ? 'right' : 'left')};
`;
const Sender = styled.strong`
  color: #1976d2;
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

const StompChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const [senderEmail, setSenderEmail] = useState('');
  const { roomId } = useParams();
  const chatBoxRef = useRef(null);

  useEffect(() => {
    const email = localStorage.getItem("email");
    setSenderEmail(email);
    const loadChatHistory = async () => {
      try {
        const response = await apiClient.get(`/chat/history/${roomId}`);
        setMessages(response.data);
      } catch (error) {}
    };
    loadChatHistory();
    connectWebsocket();
    return () => {
      disconnectWebSocket();
    };
    // eslint-disable-next-line
  }, [roomId]);

  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  const connectWebsocket = () => {
    if (stompClient && stompClient.connected) return;
    const sockJs = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/connect`);
    const client = Stomp.over(sockJs);
    const token = sessionStorage.getItem("token");
    client.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        client.subscribe(
          `/topic/${roomId}`,
          (message) => {
            const parseMessage = JSON.parse(message.body);
            setMessages(prev => [...prev, parseMessage]);
          },
          { Authorization: `Bearer ${token}` }
        );
      }
    );
    setStompClient(client);
  };

  const sendMessage = () => {
    if (newMessage.trim() === "" || !stompClient) return;
    const message = {
      senderEmail: senderEmail,
      message: newMessage
    };
    stompClient.send(`/publish/${roomId}`, JSON.stringify(message));
    setNewMessage('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  const disconnectWebSocket = async () => {
    try {
      await apiClient.post(`/chat/room/${roomId}/read`);
    } catch (error) {}
    if (stompClient && stompClient.connected) {
      stompClient.unsubscribe(`/topic/${roomId}`);
      stompClient.disconnect();
    }
  };

  return (
    <Wrapper>
      <Card>
        <Title>채팅</Title>
        <ChatBox ref={chatBoxRef}>
          {messages.map((msg, index) => (
            <ChatMessage key={index} sent={msg.senderEmail === senderEmail}>
              <Sender>{msg.senderEmail}: </Sender>{msg.message}
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

export default StompChatPage; 
# WebSocket을 활용한 채팅 구현 가이드

## 목차
1. [HTTP vs WebSocket 개념](#http-vs-websocket-개념)
2. [WebSocket 프로토콜 이해](#websocket-프로토콜-이해)
3. [채팅 시스템 아키텍처](#채팅-시스템-아키텍처)
4. [백엔드 구현 (Spring Boot)](#백엔드-구현-spring-boot)
5. [프론트엔드 구현 (React)](#프론트엔드-구현-react)
6. [실제 구현 예시](#실제-구현-예시)
7. [테스트 및 디버깅](#테스트-및-디버깅)

---

## HTTP vs WebSocket 개념

### HTTP (HyperText Transfer Protocol)
- **특징**: 요청-응답 기반의 단방향 통신
- **연결**: 각 요청마다 새로운 연결 생성 후 해제
- **통신 방식**: 클라이언트가 요청 → 서버가 응답 → 연결 종료
- **장점**: 간단하고 안정적
- **단점**: 실시간 양방향 통신 불가능

### WebSocket
- **특징**: 양방향 실시간 통신
- **연결**: 한 번 연결하면 지속적으로 유지
- **통신 방식**: 클라이언트 ↔ 서버 실시간 양방향 통신
- **장점**: 실시간 통신 가능, 낮은 오버헤드
- **단점**: 연결 관리 복잡, 서버 리소스 사용

### 채팅에서의 차이점
```
HTTP 방식:
클라이언트 → "새 메시지 있나?" → 서버
서버 → "네, 3개 있습니다" → 클라이언트
클라이언트 → "메시지 보내줘" → 서버
서버 → "메시지들..." → 클라이언트
(폴링 방식 - 계속 요청해야 함)

WebSocket 방식:
클라이언트 ↔ 서버 (연결 유지)
서버 → "새 메시지 도착!" → 클라이언트 (즉시 전송)
클라이언트 → "메시지 전송" → 서버 (즉시 전송)
```

---

## WebSocket 프로토콜 이해

### WebSocket 핸드셰이크
1. **HTTP Upgrade 요청**
   ```
   GET /ws HTTP/1.1
   Host: localhost:8001
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
   Sec-WebSocket-Version: 13
   ```

2. **서버 응답**
   ```
   HTTP/1.1 101 Switching Protocols
   Upgrade: websocket
   Connection: Upgrade
   Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
   ```

3. **연결 완료 후 양방향 통신**

### WebSocket 메시지 타입
- **Text**: 텍스트 메시지
- **Binary**: 바이너리 데이터
- **Ping/Pong**: 연결 상태 확인
- **Close**: 연결 종료

---

## 채팅 시스템 아키텍처

### 전체 구조
```
┌─────────────┐    WebSocket    ┌─────────────┐
│   Client A  │ ←────────────→ │   Server    │
└─────────────┘                 └─────────────┘
       │                              │
       │                              │
┌─────────────┐                 ┌─────────────┐
│   Client B  │ ←────────────→ │   Database  │
└─────────────┘                 └─────────────┘
```

### 데이터 흐름
1. **연결**: 클라이언트가 WebSocket 연결
2. **메시지 전송**: 클라이언트가 메시지 전송
3. **서버 처리**: 서버가 메시지를 DB에 저장
4. **브로드캐스트**: 서버가 모든 연결된 클라이언트에게 메시지 전송
5. **수신**: 클라이언트들이 메시지 수신

---

## 백엔드 구현 (Spring Boot)

### 1. 의존성 추가 (build.gradle)
```gradle
implementation 'org.springframework.boot:spring-boot-starter-websocket'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'com.fasterxml.jackson.core:jackson-databind'
```

### 2. WebSocket 설정
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final SimpleWebSocketHandler simpleWebSocketHandler;

    public WebSocketConfig(SimpleWebSocketHandler simpleWebSocketHandler) {
        this.simpleWebSocketHandler = simpleWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(simpleWebSocketHandler, "/connect")
                .setAllowedOrigins("http://localhost:3000");
    }
}
```

### 3. WebSocket 핸들러 구현
```java
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {
    
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SimpleWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // 1. JSON 파싱
        ChatMessageDto chatMessageDto = objectMapper.readValue(payload, ChatMessageDto.class);
        
        // 2. DB 저장
        chatService.saveMessage(chatMessageDto.getRoomId(), chatMessageDto);
        
        // 3. 모든 세션에 브로드캐스트
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Disconnected: " + session.getId());
    }
}
```

### 4. DTO 클래스
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long roomId;
    private String message;
    private String senderEmail;
}
```

### 5. 채팅 서비스
```java
@Service
@Transactional
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        // 보낸 사람 조회
        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail())
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
                .build();
        
        chatMessageRepository.save(chatMessage);
    }
}
```

---

## 프론트엔드 구현 (React)

### 1. WebSocket 연결 및 채팅 컴포넌트
```jsx
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import apiClient from '../api/axiosInstance';

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
    
    // 채팅 히스토리 로드
    const loadChatHistory = async () => {
      try {
        const response = await apiClient.get(`/v1/chat/history/${roomId}`);
        setMessages(response.data);
      } catch (error) {
        console.error('채팅 히스토리 로드 실패:', error);
      }
    };
    
    loadChatHistory();
    connectWebsocket();
    
    return () => {
      disconnectWebSocket();
    };
  }, [roomId]);

  // WebSocket 연결
  const connectWebsocket = () => {
    const websocket = new WebSocket(getWsUrl());
    
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

  // 메시지 전송
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

  // WebSocket 연결 해제
  const disconnectWebSocket = async () => {
    try {
      await apiClient.post(`/v1/chat/room/${roomId}/read`);
    } catch (error) {
      console.error('읽음 처리 실패:', error);
    }
    if (ws) {
      ws.close();
    }
  };

  // 자동 스크롤
  useEffect(() => {
    if (chatBoxRef.current) {
      chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
    }
  }, [messages]);

  return (
    <Wrapper>
      <BackButton onClick={() => navigate(-1)}>← 뒤로가기</BackButton>
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
            onKeyPress={e => e.key === 'Enter' && sendMessage()}
          />
          <Button onClick={sendMessage}>전송</Button>
        </FormRow>
      </Card>
    </Wrapper>
  );
};
```

### 2. 스타일링
```jsx
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
```

### 3. WebSocket URL 생성 함수
```jsx
function getWsUrl() {
  const base = import.meta.env.VITE_API_BASE_URL;
  return base.replace(/^http/, 'ws') + '/connect';
}
```

---

## 실제 구현 예시

### 프로젝트 구조
```
front/
├── src/
│   ├── pages/
│   │   ├── ChatPage.jsx          # WebSocket 채팅 페이지
│   │   ├── MemberList.jsx        # 회원 목록
│   │   ├── GroupChattingList.jsx # 그룹 채팅방 목록
│   │   └── MyChatPage.jsx        # 내 채팅 목록
│   ├── api/
│   │   ├── memberApi.js          # 회원 관련 API
│   │   └── chatApi.js            # 채팅 관련 API
│   └── components/
│       └── HeaderComponent.jsx   # 헤더 (채팅 메뉴)

back/
├── src/main/java/com/kh/login/
│   ├── config/
│   │   ├── WebSocketConfig.java      # WebSocket 설정
│   │   └── SimpleWebSocketHandler.java # WebSocket 핸들러
│   ├── service/
│   │   └── ChatService.java          # 채팅 비즈니스 로직
│   ├── domain/
│   │   ├── ChatMessage.java          # 채팅 메시지 엔티티
│   │   ├── ChatRoom.java             # 채팅방 엔티티
│   │   └── Member.java               # 회원 엔티티
│   └── dto/
│       └── chat/
│           └── ChatMessageDto.java   # 채팅 메시지 DTO
```

### 주요 기능
1. **실시간 채팅**: WebSocket을 통한 양방향 실시간 통신
2. **채팅 히스토리**: 페이지 진입 시 기존 메시지 로드
3. **DB 저장**: 모든 메시지를 데이터베이스에 저장
4. **브로드캐스트**: 메시지를 모든 연결된 클라이언트에게 전송
5. **읽음 처리**: 채팅방 나갈 때 읽음 상태 업데이트
6. **자동 스크롤**: 새 메시지가 오면 자동으로 아래로 스크롤

---

## 테스트 및 디버깅

### 1. WebSocket 연결 테스트
```javascript
// 브라우저 콘솔에서 테스트
const ws = new WebSocket('ws://localhost:8001/connect');
ws.onopen = () => console.log('연결됨');
ws.onmessage = (event) => console.log('메시지:', event.data);
ws.send(JSON.stringify({
  roomId: 1,
  senderEmail: "test@email.com",
  message: "테스트 메시지"
}));
```

### 2. 서버 로그 확인
```bash
# Spring Boot 애플리케이션 로그에서 확인
Connected: session-id
received message: {"roomId":1,"senderEmail":"test@email.com","message":"테스트"}
Disconnected: session-id
```

### 3. 일반적인 문제 해결
- **연결 실패**: CORS 설정, 포트 확인
- **메시지 전송 실패**: JSON 형식 확인
- **DB 저장 실패**: 데이터베이스 연결, 엔티티 매핑 확인
- **브로드캐스트 실패**: 세션 관리 확인

### 4. 성능 최적화
- **메시지 배치 처리**: 여러 메시지를 한 번에 처리
- **연결 풀링**: WebSocket 연결 재사용
- **메시지 압축**: 대용량 메시지 압축 전송
- **세션 관리**: 비활성 세션 정리

---

## 결론

WebSocket을 활용한 채팅 시스템은 다음과 같은 장점을 제공합니다:

1. **실시간성**: 즉시 메시지 전송 및 수신
2. **효율성**: 연결 유지로 낮은 오버헤드
3. **확장성**: 다중 클라이언트 지원
4. **안정성**: 연결 상태 관리 및 에러 처리

이 가이드를 따라 구현하면 완전한 실시간 채팅 시스템을 구축할 수 있습니다. 
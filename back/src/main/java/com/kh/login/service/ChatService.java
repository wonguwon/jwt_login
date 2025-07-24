package com.kh.login.service;

import com.kh.login.domain.ChatMessage;
import com.kh.login.domain.ChatParticipant;
import com.kh.login.domain.ChatRoom;
import com.kh.login.domain.Member;
import com.kh.login.domain.ReadStatus;
import com.kh.login.dto.chat.ChatMessageDto;
import com.kh.login.dto.chat.ChatRoomListResDto;
import com.kh.login.dto.chat.MyChatListResDto;
import com.kh.login.repository.MemberRepository;
import com.kh.login.repository.chat.ChatMessageRepository;
import com.kh.login.repository.chat.ChatParticipantRepository;
import com.kh.login.repository.chat.ChatRoomRepository;
import com.kh.login.repository.chat.ReadStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 
 * 주요 기능:
 * - 채팅방 생성 및 관리 (1:1, 그룹 채팅)
 * - 채팅 메시지 저장 및 조회
 * - 채팅방 참여자 관리
 * - 메시지 읽음 상태 관리
 * - 채팅방 목록 조회
 */
@RequiredArgsConstructor
@Service
@Transactional
public class ChatService {

    // 레포지토리들 주입
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    /**
     * [채팅 메시지 저장]
     * 
     * 비즈니스 로직:
     * 1. 채팅방과 발신자 정보를 검증
     * 2. 메시지를 데이터베이스에 저장
     * 3. 해당 채팅방의 모든 참여자에 대해 읽음 상태를 생성
     *    - 발신자 본인은 자동으로 읽음 처리
     *    - 다른 참여자들은 읽지 않음 상태로 초기화
     * 
     * @param roomId 채팅방 ID
     * @param chatMessageReqDto 메시지 정보 (발신자 이메일, 메시지 내용)
     */
    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto) {
        // 채팅방 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 발신자(보낸 사람) 정보 검증
        Member sender = memberRepository.findByEmail(chatMessageReqDto.getSenderEmail())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 메시지 엔티티 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 모든 참여자에 대해 읽음 상태 생성 (배치 처리로 성능 최적화)
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        List<ReadStatus> readStatuses = chatParticipants.stream()
                .map(c -> ReadStatus.builder()
                        .chatRoom(chatRoom)
                        .member(c.getMember())
                        .chatMessage(chatMessage)
                        .isRead(c.getMember().equals(sender))  // 발신자 본인은 읽음 처리
                        .build())
                .toList();
        readStatusRepository.saveAll(readStatuses);
    }

    /**
     * [그룹 채팅방 생성]
     * 
     * 비즈니스 로직:
     * 1. 현재 로그인한 사용자를 생성자로 설정
     * 2. 그룹 채팅방을 생성 (isGroupChat = 'Y')
     * 3. 생성자를 해당 채팅방의 첫 번째 참여자로 등록
     * 
     * @param chatRoomName 생성할 채팅방 이름
     */
    public void createGroupRoom(String chatRoomName) {
        // 현재 로그인한 사용자 조회 (Spring Security 컨텍스트에서 추출)
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 그룹 채팅방 생성 (isGroupChat = 'Y'로 설정)
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);

        // 생성자를 참여자로 등록
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * [그룹 채팅방 목록 조회]
     * 
     * 비즈니스 로직:
     * 1. isGroupChat = 'Y'인 채팅방만 필터링하여 조회
     * 2. 각 채팅방 정보를 DTO로 변환하여 반환
     * 
     * @return 그룹 채팅방 목록 (방 ID, 방 이름)
     */
    public List<ChatRoomListResDto> getGroupchatRooms() {
        // 그룹 채팅방만 조회 (isGroupChat = 'Y')
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        
        // Stream API를 사용하여 DTO 변환 (성능 최적화)
        List<ChatRoomListResDto> dtos = chatRooms.stream()
                .map(c -> ChatRoomListResDto.builder()
                        .roomId(c.getId())
                        .roomName(c.getName())
                        .build())
                .toList();

        return dtos;
    }

    /**
     * [그룹 채팅방 참여]
     * 
     * 비즈니스 로직:
     * 1. 채팅방이 실제로 그룹 채팅방인지 검증
     * 2. 현재 사용자가 이미 참여 중인지 확인
     * 3. 참여하지 않은 경우에만 새로운 참여자로 추가
     * 
     * @param roomId 참여할 채팅방 ID
     */
    public void addParticipantToGroupChat(Long roomId) {
        // 채팅방 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 그룹 채팅방 검증 (단체 채팅이 아닐 경우 예외 발생)
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("그룹채팅이 아닙니다.");
        }

        // 중복 참여 방지: 이미 참여 중인지 확인
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if (!participant.isPresent()) {
            addParticipantToRoom(chatRoom, member);
        }
    }

    /**
     * [채팅방 참여자 추가]
     * 
     * 비즈니스 로직:
     * 1. 해당 사용자가 이미 참여자인지 확인
     * 2. 참여하지 않은 경우에만 새로운 참여자로 추가
     * 
     * @param chatRoom 참여할 채팅방
     * @param member 참여할 사용자
     */
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        // 이미 참여자인 경우 추가하지 않음
        if (chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).isPresent()) {
            return;
        }

        // 새로운 참여자 등록
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * [채팅 내역 조회]
     * 
     * 비즈니스 로직:
     * 1. 채팅방 존재 여부 검증
     * 2. 현재 사용자가 해당 채팅방의 참여자인지 검증
     * 3. 채팅방의 모든 메시지를 시간순으로 조회
     * 4. 메시지 정보를 DTO로 변환하여 반환
     * 
     * @param roomId 조회할 채팅방 ID
     * @return 채팅 메시지 목록 (발신자 이메일, 메시지 내용)
     */
    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 채팅방 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 본인 참여 여부 확인 (보안 검증)
        boolean isParticipant = chatParticipantRepository.findByChatRoom(chatRoom)
                .stream().anyMatch(cp -> cp.getMember().equals(member));
        if (!isParticipant) {
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        }

        // 메시지 조회 (시간순 정렬)
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        
        // Stream API를 사용하여 DTO 변환
        List<ChatMessageDto> chatMessageDtos = chatMessages.stream()
                .map(c -> ChatMessageDto.builder()
                        .message(c.getContent())
                        .senderEmail(c.getMember().getEmail())
                        .build())
                .collect(Collectors.toList());

        return chatMessageDtos;
    }

    /**
     * [특정 사용자의 채팅방 참여 여부 확인]
     * 
     * 비즈니스 로직:
     * 1. 채팅방과 사용자 존재 여부 검증
     * 2. 해당 사용자가 채팅방의 참여자인지 확인
     * 
     * @param email 확인할 사용자 이메일
     * @param roomId 확인할 채팅방 ID
     * @return 참여 여부 (true: 참여 중, false: 참여하지 않음)
     */
    public boolean isRoomPaticipant(String email, Long roomId) {
        // 채팅방과 사용자 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 참여 여부 확인
        return chatParticipantRepository.findByChatRoom(chatRoom)
                .stream().anyMatch(cp -> cp.getMember().equals(member));
    }

    /**
     * [메시지 읽음 처리]
     * 
     * 비즈니스 로직:
     * 1. 채팅방과 사용자 존재 여부 검증
     * 2. 해당 사용자의 모든 읽지 않은 메시지를 읽음 상태로 변경
     * 
     * @param roomId 읽음 처리할 채팅방 ID
     */
    public void messageRead(Long roomId) {
        // 채팅방과 사용자 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 해당 사용자의 모든 읽지 않은 메시지를 읽음 상태로 변경
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom, member);
        for (ReadStatus r : readStatuses) {
            r.updateIsRead(true);
        }
    }

    /**
     * [내 채팅방 목록 조회]
     * 
     * 비즈니스 로직:
     * 1. 현재 로그인한 사용자가 참여 중인 모든 채팅방 조회
     * 2. 각 채팅방마다 읽지 않은 메시지 수를 계산
     * 3. 채팅방 정보와 읽지 않은 메시지 수를 포함하여 반환
     * 
     * @return 내가 참여 중인 채팅방 목록 (방 정보 + 읽지 않은 메시지 수)
     */
    public List<MyChatListResDto> getMyChatRooms() {
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 사용자가 참여 중인 모든 채팅방 조회
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        
        // Stream API를 사용하여 DTO 변환 및 읽지 않은 메시지 수 계산
        List<MyChatListResDto> chatListResDtos = chatParticipants.stream()
            .map(c -> {
                // 각 채팅방의 읽지 않은 메시지 수 조회
                Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), member);
                
                return MyChatListResDto.builder()
                        .roomId(c.getChatRoom().getId())
                        .roomName(c.getChatRoom().getName())
                        .isGroupChat(c.getChatRoom().getIsGroupChat())
                        .unReadCount(count)
                        .build();
            })
            .collect(Collectors.toList());

        return chatListResDtos;
    }

    /**
     * [그룹 채팅방 나가기]
     * 
     * 비즈니스 로직:
     * 1. 채팅방이 그룹 채팅방인지 검증
     * 2. 현재 사용자를 채팅방에서 제거
     * 3. 마지막 참여자가 나간 경우 채팅방 자체를 삭제
     * 
     * @param roomId 나갈 채팅방 ID
     */
    public void leaveGroupChatRoom(Long roomId) {
        // 채팅방과 사용자 존재 여부 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 그룹 채팅방 검증
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }

        // 참여자 정보 조회 및 삭제
        ChatParticipant c = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

        // 남은 참여자가 없다면 채팅방 삭제 (채팅방 정리)
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    /**
     * [1:1 채팅방 생성 또는 조회]
     * 
     * 비즈니스 로직:
     * 1. 두 사용자 간에 이미 존재하는 1:1 채팅방이 있는지 확인
     * 2. 존재하면 기존 채팅방 ID 반환
     * 3. 존재하지 않으면 새로운 1:1 채팅방 생성
     *    - 채팅방 이름: "사용자1-사용자2" 형태
     *    - 두 사용자를 모두 참여자로 등록
     * 
     * @param otherMemberId 상대방 사용자 ID
     * @return 1:1 채팅방 ID (기존 또는 새로 생성된)
     */
    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        // 현재 로그인한 사용자와 상대방 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        Member otherMember = memberRepository.findById(otherMemberId)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 기존 1:1 채팅방 존재 여부 확인
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if (chatRoom.isPresent()) {
            return chatRoom.get().getId();
        }

        // 새로운 1:1 채팅방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")  // 1:1 채팅방 표시
                .name(member.getName() + "-" + otherMember.getName())  // 채팅방 이름 설정
                .build();
        chatRoomRepository.save(newRoom);

        // 두 사용자 모두 참여자로 등록
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }
}



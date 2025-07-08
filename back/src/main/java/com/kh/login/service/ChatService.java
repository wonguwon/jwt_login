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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * - 메시지를 DB에 저장하고
     * - 모든 참여자에 대해 읽음 여부 상태(ReadStatus)를 생성
     */
    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 발신자(보낸 사람) 조회
        Member sender = memberRepository.findByEmail(chatMessageReqDto.getSenderEmail())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 메시지 엔티티 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        // 모든 참여자에 대해 읽음 상태 생성
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant c : chatParticipants) {
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender))  // 발신자 본인은 읽음 처리
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    /**
     * [그룹 채팅방 생성]
     * - 채팅방 이름과 함께 그룹 채팅방을 생성하고
     * - 생성자를 참여자로 등록
     */
    public void createGroupRoom(String chatRoomName) {
        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 채팅방 생성
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
     * - isGroupChat = 'Y'인 채팅방만 반환
     */
    public List<ChatRoomListResDto> getGroupchatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();

        // 조회된 채팅방 리스트를 DTO로 변환
        for (ChatRoom c : chatRooms) {
            dtos.add(ChatRoomListResDto.builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build());
        }

        return dtos;
    }

    /**
     * [그룹 채팅방 참여]
     * - 현재 로그인한 사용자를 해당 그룹 채팅방에 참여자로 추가
     * - 이미 참여 중이라면 추가하지 않음
     */
    public void addParticipantToGroupChat(Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 현재 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 그룹 채팅방이 아닐 경우 예외
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("그룹채팅이 아닙니다.");
        }

        // 중복 참여 방지
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);
        if (!participant.isPresent()) {
            addParticipantToRoom(chatRoom, member);
        }
    }

    /**
     * [채팅방 참여자 추가]
     * - ChatParticipant 생성 및 저장
     * - 이미 참여자인 경우 저장하지 않음
     */
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        if (chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).isPresent()) {
            return;
        }

        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * [채팅 내역 조회]
     * - 특정 채팅방의 메시지 목록 조회
     * - 본인이 참여자인지 검증 포함
     */
    public List<ChatMessageDto> getChatHistory(Long roomId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        // 로그인한 사용자 조회
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        // 본인 참여 여부 확인
        boolean isParticipant = chatParticipantRepository.findByChatRoom(chatRoom)
                .stream().anyMatch(cp -> cp.getMember().equals(member));
        if (!isParticipant) {
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        }

        // 메시지 조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        for (ChatMessage c : chatMessages) {
            chatMessageDtos.add(ChatMessageDto.builder()
                    .message(c.getContent())
                    .senderEmail(c.getMember().getEmail())
                    .build());
        }

        return chatMessageDtos;
    }

    /**
     * [특정 사용자의 채팅방 참여 여부 확인]
     */
    public boolean isRoomPaticipant(String email, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        return chatParticipantRepository.findByChatRoom(chatRoom)
                .stream().anyMatch(cp -> cp.getMember().equals(member));
    }

    /**
     * [메시지 읽음 처리]
     * - 로그인한 사용자의 특정 채팅방 메시지 모두 읽음으로 변경
     */
    public void messageRead(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndMember(chatRoom, member);
        for (ReadStatus r : readStatuses) {
            r.updateIsRead(true);
        }
    }

    /**
     * [내 채팅방 목록 조회]
     * - 로그인한 사용자가 참여 중인 채팅방 목록 조회
     * - 각 채팅방마다 안 읽은 메시지 수 포함
     */
    public List<MyChatListResDto> getMyChatRooms() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();

        for (ChatParticipant c : chatParticipants) {
            Long count = readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), member);

            chatListResDtos.add(MyChatListResDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .build());
        }

        return chatListResDtos;
    }

    /**
     * [그룹 채팅방 나가기]
     * - 사용자가 채팅방을 나가며
     * - 마지막 사용자일 경우 채팅방 자체 삭제
     */
    public void leaveGroupChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }

        ChatParticipant c = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

        // 남은 참여자가 없다면 채팅방 삭제
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    /**
     * [1:1 채팅방 생성 또는 조회]
     * - 두 명 사이에 이미 존재하는 채팅방이 있다면 반환
     * - 없으면 새로 생성하고 참여자로 등록
     */
    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        Member otherMember = memberRepository.findById(otherMemberId)
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(member.getId(), otherMember.getId());
        if (chatRoom.isPresent()) {
            return chatRoom.get().getId();
        }

        // 1:1 채팅방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(member.getName() + "-" + otherMember.getName())
                .build();
        chatRoomRepository.save(newRoom);

        // 두 사용자 모두 참여자로 등록
        addParticipantToRoom(newRoom, member);
        addParticipantToRoom(newRoom, otherMember);

        return newRoom.getId();
    }
}



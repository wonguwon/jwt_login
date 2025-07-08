package com.kh.login.repository.chat;


import com.kh.login.domain.ChatParticipant;
import com.kh.login.domain.ChatRoom;
import com.kh.login.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    List<ChatParticipant> findAllByMember(Member member);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 WHERE cp1.chatRoom.isGroupChat = 'N' AND cp1.chatRoom.id IN (SELECT cp2.chatRoom.id FROM ChatParticipant cp2 WHERE cp2.member.id = :myId OR cp2.member.id = :otherMemberId GROUP BY cp2.chatRoom.id HAVING COUNT(DISTINCT cp2.member.id) = 2)")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId") Long myId, @Param("otherMemberId") Long otherMemberId);
}

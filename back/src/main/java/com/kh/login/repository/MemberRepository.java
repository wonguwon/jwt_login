package com.kh.login.repository;

import com.kh.login.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByPhoneNumber(String phoneNumber);
    Optional<Member> findBySocialId(String socialId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}

package com.kh.login.repository;

import com.kh.login.domain.Member;
import com.kh.login.enums.SocialType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByPhoneNumber(String phoneNumber);
    Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}

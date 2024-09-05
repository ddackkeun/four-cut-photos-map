package com.idea5.four_cut_photos_map.domain.member.repository;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByKakaoId(Long kakaoId);

    List<Member> findAllByOrderByIdAsc();

    boolean existsByNickname(String nickname);

    Optional<Member> findByIdAndStatus(Long id, MemberStatus status);

    Optional<Member> findByNickname(String nickname);
}

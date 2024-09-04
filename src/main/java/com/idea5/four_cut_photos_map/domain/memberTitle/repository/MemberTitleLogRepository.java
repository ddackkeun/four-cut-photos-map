package com.idea5.four_cut_photos_map.domain.memberTitle.repository;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberTitleLogRepository extends JpaRepository<MemberTitleLog, Long> {
    List<MemberTitleLog> findAllByMember(Member member);

    List<MemberTitleLog> findByMember(Member member);

    Optional<MemberTitleLog> findByMemberAndIsMainTrue(Member member);
    List<MemberTitleLog> findAllByMemberAndIsMainTrue(Member member);

    Optional<MemberTitleLog> findByMemberAndMemberTitle(Member member, MemberTitle memberTitle);

    @Query("SELECT mtl FROM MemberTitleLog mtl WHERE mtl.member.id = :memberId AND mtl.memberTitle.id = :memberTitleId")
    Optional<MemberTitleLog> findByMemberIdAndMemberTitleId(Long memberId, Long memberTitleId);
}

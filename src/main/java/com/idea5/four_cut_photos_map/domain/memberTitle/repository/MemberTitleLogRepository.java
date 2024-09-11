package com.idea5.four_cut_photos_map.domain.memberTitle.repository;

import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberTitleLogRepository extends JpaRepository<MemberTitleLog, Long> {
    List<MemberTitleLog> findAllByMemberId(Long memberId);

    @Query("SELECT mtl FROM MemberTitleLog mtl JOIN FETCH mtl.memberTitle WHERE mtl.memberId = :memberId")
    List<MemberTitleLog> findAllByMemberIdWithTitles(@Param("memberId") Long memberId);

    Optional<MemberTitleLog> findByMemberIdAndIsMainTrue(Long memberId);

    Optional<MemberTitleLog> findByMemberIdAndMemberTitle(Long memberId, MemberTitle memberTitle);

    @Query("SELECT mtl FROM MemberTitleLog mtl WHERE mtl.memberId = :memberId AND mtl.memberTitle.id = :memberTitleId")
    Optional<MemberTitleLog> findByMemberIdAndMemberTitleId(Long memberId, Long memberTitleId);

    @Query("SELECT COUNT(mtl) FROM MemberTitleLog mtl WHERE mtl.memberId = :memberId")
    Long countByMemberId(Long memberId);
}

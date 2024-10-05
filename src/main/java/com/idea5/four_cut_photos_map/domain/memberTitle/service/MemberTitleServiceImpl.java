package com.idea5.four_cut_photos_map.domain.memberTitle.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleInfoResponse;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleResponse;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleType;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTitleServiceImpl implements MemberTitleService {
    private final MemberTitleRepository memberTitleRepository;
    private final MemberTitleLogRepository memberTitleLogRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<MemberTitleResponse> getMemberTitles(Long memberId) {
        List<MemberTitleLog> memberTitleLogs = memberTitleLogRepository.findAllByMemberId(memberId);
        Set<Long> ownedMemberTitleIds = getMemberTitleIds(memberTitleLogs);
        Long mainMemberTitleId = getMainMemberTitleId(memberTitleLogs);

        List<MemberTitle> memberTitles = getAllMemberTitles();
        return memberTitles.stream()
                .map(memberTitle -> {
                    boolean isHoldings = ownedMemberTitleIds.contains(memberTitle.getId());
                    boolean isMain = memberTitle.getId().equals(mainMemberTitleId);
                    return MemberTitleResponse.toResponse(memberTitle, isHoldings, isMain);
                })
                .toList();
    }

    @Transactional
    @Override
    public void updateMainMemberTitle(Long memberId, Long memberTitleId) {
        Member member = getRegisteredMemberWithThrows(memberId);
        MemberTitle memberTitle = getRegisteredMemberTitleWithThrows(memberTitleId);

        MemberTitleLog newMainMemberTitleLog = memberTitleLogRepository.findByMemberIdAndMemberTitle(member.getId(), memberTitle)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_TITLE_NOT_HAD));

        if (newMainMemberTitleLog.getIsMain()) {
            throw new BusinessException(ErrorCode.DUPLICATE_MAIN_MEMBER_TITLE);
        }

        memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)
                .ifPresent(MemberTitleLog::cancelMain);

        newMainMemberTitleLog.registerMain();
        member.updateMainTitle(memberTitle.getName());
    }

    @Transactional
    @Override
    public void issueMemberTitleLog(Long memberId, Long memberTitleId) {
        Member member = getRegisteredMemberWithThrows(memberId);
        MemberTitle memberTitle = getRegisteredMemberTitleWithThrows(memberTitleId);

        MemberTitleLog memberTitleLog = MemberTitleLog.create(memberId, memberTitle);
        memberTitleLogRepository.save(memberTitleLog);
    }

    @Override
    public List<MemberTitle> getAllMemberTitles() {
        return memberTitleRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Set<Long> getMemberTitleIds(Long memberId) {
        return memberTitleLogRepository.findAllByMemberId(memberId).stream()
                .map(memberTitleLog -> memberTitleLog.getMemberTitle().getId())
                .collect(Collectors.toSet());
    }

    private Member getRegisteredMemberWithThrows(Long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberTitle getRegisteredMemberTitleWithThrows(Long memberTitleId) {
        return memberTitleRepository.findById(memberTitleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_TITLE_NOT_FOUND));
    }

    private Set<Long> getMemberTitleIds(List<MemberTitleLog> memberTitleLogs) {
        return memberTitleLogs.stream()
                .map(memberTitleLog -> memberTitleLog.getMemberTitle().getId())
                .collect(Collectors.toSet());
    }

    private Long getMainMemberTitleId(List<MemberTitleLog> memberTitleLogs) {
        return memberTitleLogs.stream()
                .filter(MemberTitleLog::getIsMain)
                .map(memberTitleLog -> memberTitleLog.getMemberTitle().getId())
                .findFirst()
                .orElse(null);
    }

}

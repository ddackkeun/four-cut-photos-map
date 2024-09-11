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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberTitleService {
    private final MemberTitleRepository memberTitleRepository;
    private final MemberTitleLogRepository memberTitleLogRepository;
    private final MemberRepository memberRepository;

    public List<MemberTitleResponse> getMemberTitles(Long memberId) {
        List<MemberTitleLog> memberTitleLogs = memberTitleLogRepository.findAllByMemberIdWithTitles(memberId);

        List<MemberTitle> allMemberTitles = memberTitleRepository.findAll();

        Set<Long> ownedTitleIds = memberTitleLogs.stream()
                .map(memberTitleLog -> memberTitleLog.getMemberTitle().getId())
                .collect(Collectors.toSet());

        MemberTitleLog mainMemberTitleLog = memberTitleLogs.stream()
                .filter(MemberTitleLog::getIsMain)
                .findFirst()
                .orElse(null);
        Long mainMemberTitleId = mainMemberTitleLog != null ? mainMemberTitleLog.getMemberTitle().getId() : null;

        return allMemberTitles.stream()
                .map(memberTitle -> {
                    boolean isHoldings = ownedTitleIds.contains(memberTitle.getId());
                    boolean isMain = memberTitle.getId().equals(mainMemberTitleId);
                    return MemberTitleResponse.toResponse(memberTitle, isHoldings, isMain);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMainMemberTitle(Long memberId, Long memberTitleId) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberTitle memberTitle = memberTitleRepository.findById(memberTitleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_TITLE_NOT_FOUND));

        MemberTitleLog currentMainMemberTitleLog = memberTitleLogRepository.findByMemberIdAndIsMainTrue(member.getId()).orElse(null);
        if (currentMainMemberTitleLog != null && currentMainMemberTitleLog.getMemberTitle().equals(memberTitle)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MAIN_MEMBER_TITLE);
        }

        MemberTitleLog newMainMemberTitleLog = memberTitleLogRepository.findByMemberIdAndMemberTitleId(member.getId(), memberTitleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_TITLE_NOT_HAD));

        if (currentMainMemberTitleLog != null) currentMainMemberTitleLog.cancelMain();
        newMainMemberTitleLog.registerMain();
        member.updateMainTitle(memberTitle.getName());
    }

    public List<MemberTitle> findAllMemberTitle() {
        return memberTitleRepository.findAllByOrderByIdAsc();
    }

    public Set<Long> getMemberTitleIds(Long memberId) {
        return memberTitleLogRepository.findAllByMemberIdWithTitles(memberId).stream()
                .map(memberTitleLog -> memberTitleLog.getMemberTitle().getId())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void issueNewbieTitle(Long memberId) {
        issueMemberTitle(memberId, MemberTitleType.NEWBIE.getCode());
    }

    public MemberTitleLog issueMemberTitle(Long memberId, Long memberTitleId) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberTitle memberTitle = memberTitleRepository.findById(memberTitleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_TITLE_NOT_FOUND));
        MemberTitleLog memberTitleLog = MemberTitleLog.builder()
                .memberId(member.getId())
                .memberTitle(memberTitle)
                .isMain(false)
                .build();

        return memberTitleLogRepository.save(memberTitleLog);
    }

    public MemberTitleInfoResponse getMemberTitleInfo(Long memberId) {
        Integer count = memberTitleLogRepository.countByMemberId(memberId).intValue();
        String mainMemberTitleName = memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)
                .map(MemberTitleLog::getMemberTitleName)
                .orElse("");

        return new MemberTitleInfoResponse(count, mainMemberTitleName);
    }


}

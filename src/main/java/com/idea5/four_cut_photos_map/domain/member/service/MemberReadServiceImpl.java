package com.idea5.four_cut_photos_map.domain.member.service;


import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberReadServiceImpl implements MemberReadService {
    private final MemberRepository memberRepository;
    private final MemberTitleLogRepository memberTitleLogRepository;

    @Override
    public Member getMemberWithThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Integer titleCount = memberTitleLogRepository.countByMemberId(memberId).intValue();

        return MemberInfoResponse.toResponse(member, titleCount);
    }

    @Override
    public NicknameCheckResponse checkNickname(Long memberId, String nickname) {
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getNickname().equals(nickname)) {
            return new NicknameCheckResponse(true, false);
        }

        if (memberRepository.existsByNickname(nickname)) {
            return new NicknameCheckResponse(false, true);
        }

        return new NicknameCheckResponse(false, false);
    }
}

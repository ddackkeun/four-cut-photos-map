package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;

public interface MemberReadService {
    Member getMemberWithThrow(Long memberId);
    MemberInfoResponse getMemberInfo(Long memberId);
    NicknameCheckResponse checkNickname(Long memberId, String nickname);
}

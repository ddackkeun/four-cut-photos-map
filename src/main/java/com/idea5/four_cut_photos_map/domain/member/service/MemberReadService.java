package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;

public interface MemberReadService {
    public Member getMemberWithThrow(Long memberId);
}

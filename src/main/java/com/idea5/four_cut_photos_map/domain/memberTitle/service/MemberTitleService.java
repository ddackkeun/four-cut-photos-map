package com.idea5.four_cut_photos_map.domain.memberTitle.service;

import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleResponse;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;

import java.util.List;
import java.util.Set;

public interface MemberTitleService {
    List<MemberTitleResponse> getMemberTitles(Long memberId);
    void updateMainMemberTitle(Long memberId, Long memberTitleId);
    void issueMemberTitleLog(Long memberId, Long memberTitleId);
    List<MemberTitle> getAllMemberTitles();
    Set<Long> getMemberTitleIds(Long memberId);
}

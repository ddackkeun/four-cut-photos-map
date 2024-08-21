package com.idea5.four_cut_photos_map.domain.member.mapper;

import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public Member toEntity(Long kakaoId, String nickname, String kakaoRefreshToken) {
        return Member.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .kakaoRefreshToken(kakaoRefreshToken)
                .build();
    }

    public MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .build();
    }

    public MemberResponse toResponse(Member member, String mainMemberTitleName) {
        return MemberResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .mainMemberTitle(mainMemberTitleName)
                .build();
    }
}

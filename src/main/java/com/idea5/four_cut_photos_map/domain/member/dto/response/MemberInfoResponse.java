package com.idea5.four_cut_photos_map.domain.member.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberInfoResponse {
    private Long id;
    private String nickname;
    private String mainTitle;
    private Integer memberTitleCnt;

    public static MemberInfoResponse toResponse(Member member, Integer memberTitleCnt) {
        return MemberInfoResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .mainTitle(member.getMainTitleName())
                .memberTitleCnt(memberTitleCnt)
                .build();
    }
}

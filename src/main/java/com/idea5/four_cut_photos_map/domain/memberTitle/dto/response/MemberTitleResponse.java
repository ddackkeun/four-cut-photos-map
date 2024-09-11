package com.idea5.four_cut_photos_map.domain.memberTitle.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원 칭호 단건 조회 응답 DTO
 */
@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberTitleResponse {
    private Long id;
    private String name;
    private String content;     // 설명
    private String imageUrl;
    private Boolean isHolding;  // 획득여부
    private Boolean isMain;     // 대표칭호 여부

    public static MemberTitleResponse toResponse(MemberTitle memberTitle, boolean isHolding, boolean isMain) {
        return MemberTitleResponse.builder()
                .id(memberTitle.getId())
                .name(memberTitle.getName())
                .content(memberTitle.getContent())
                .imageUrl(isHolding ? memberTitle.getColorImageUrl() : memberTitle.getBwImageUrl())
                .isHolding(isHolding)
                .isMain(isMain)
                .build();
    }
}

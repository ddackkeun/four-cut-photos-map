package com.idea5.four_cut_photos_map.domain.member.dto.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberResponse {
    private Long id;            // 회원 번호
    private String nickname;    // 닉네임
    private String mainTitleName; // 회원 대표 칭호
    private Collection<? extends GrantedAuthority> authorities;

    public static MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .mainTitleName(member.getMainTitleName())
                .authorities(member.getAuthorities())
                .build();
    }
}

package com.idea5.four_cut_photos_map.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private MemberResponse memberResponse;
    private Boolean isNewMember;
}

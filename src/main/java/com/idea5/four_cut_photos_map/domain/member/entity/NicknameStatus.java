package com.idea5.four_cut_photos_map.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NicknameStatus {
    AVAILABLE("사용 가능한 닉네임"),
    DUPLICATED("중복된 닉네임"),
    USED("현재 유저가 사용 중인 닉네임");

    private final String description;
}

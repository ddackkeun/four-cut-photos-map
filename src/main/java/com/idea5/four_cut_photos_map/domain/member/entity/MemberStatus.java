package com.idea5.four_cut_photos_map.domain.member.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MemberStatus {

    REGISTERED("등록"),
    DELETED("삭제");

    private final String description;
}

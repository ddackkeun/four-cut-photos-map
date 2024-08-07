package com.idea5.four_cut_photos_map.domain.reviewphoto.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ReviewPhotoStatus {
    REGISTERED("등록"),
    DELETED("삭제")
    ;
    
    private final String description;
}

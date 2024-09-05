package com.idea5.four_cut_photos_map.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NicknameCheckResponse {
    private boolean isSameAsCurrent;
    private boolean isUsed;
}

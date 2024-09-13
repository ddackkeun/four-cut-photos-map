package com.idea5.four_cut_photos_map.domain.member.entity;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;

public class MemberFactory {
    public static Member fromKakaoUser(KakaoUserInfoParam kakaoUserInfo, KakaoTokenResp kakaoToken, String nickname, MemberStatus status) {
        return Member.builder()
                .kakaoId(kakaoUserInfo.getId())
                .nickname(nickname)
                .kakaoRefreshToken(kakaoToken.getRefreshToken())
                .status(status)
                .build();
    }
}

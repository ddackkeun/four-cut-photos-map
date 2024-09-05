package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;

public interface MemberRequestService {
    LoginResponse login(KakaoUserInfoParam kakaoUserInfo, KakaoTokenResp kakaoToken);

    Member register(KakaoUserInfoParam kakaoUserInfo, KakaoTokenResp kakaoToken);

    String generateUniqueNickname(String nickname);

    String updateNickname(Long id, String nickname);
}

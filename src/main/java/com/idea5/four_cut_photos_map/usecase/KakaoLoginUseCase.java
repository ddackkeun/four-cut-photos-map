package com.idea5.four_cut_photos_map.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.auth.service.KakaoService;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.service.MemberRequestService;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import com.idea5.four_cut_photos_map.security.jwt.JwtService;
import com.idea5.four_cut_photos_map.security.jwt.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class KakaoLoginUseCase {
    private final KakaoService kakaoService;
    private final MemberRequestService memberRequestService;
    private final MemberTitleService memberTitleService;
    private final JwtService jwtService;

    public TokenResponse execute(String code, HttpServletRequest request) throws JsonProcessingException {
        String redirectURI = kakaoService.getRedirectURI(request.getHeader("Origin"));
        KakaoTokenResp kakaoToken = kakaoService.getKakaoTokens(code, redirectURI);
        KakaoUserInfoParam kakaoUserInfo = kakaoService.getKakaoUserInfo(kakaoToken);

        LoginResponse loginResponse = memberRequestService.login(kakaoUserInfo, kakaoToken);

        // TODO 비동기처리
        if(loginResponse.getIsNewMember()) {
            memberTitleService.issueNewbieTitle(loginResponse.getMemberResponse().getId());
        }

        return jwtService.generateTokens(loginResponse.getMemberResponse().getId(), loginResponse.getMemberResponse().getAuthorities());
    }
}

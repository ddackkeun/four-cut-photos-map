package com.idea5.four_cut_photos_map.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.auth.service.KakaoService;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.service.MemberService;
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
    private final MemberService memberService;
    private final JwtService jwtService;
    private final MemberTitleService memberTitleService;


    public TokenResponse execute(String code, HttpServletRequest request) throws JsonProcessingException {
        String redirectURI = kakaoService.getRedirectURI(request.getHeader("Origin"));
        KakaoTokenResp kakaoToken = kakaoService.getKakaoTokens(code, redirectURI);
        KakaoUserInfoParam kakaoUserInfo = kakaoService.getKakaoUserInfo(kakaoToken);

        Member member = memberService.login(kakaoUserInfo, kakaoToken);

        if (member.getMainTitleName() == null) {
            memberTitleService.issueNewbieTitle(member);
        }

        return jwtService.generateTokens(member);
    }
}

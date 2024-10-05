package com.idea5.four_cut_photos_map.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.auth.service.KakaoService;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.service.MemberRequestServiceImpl;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleType;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleServiceImpl;
import com.idea5.four_cut_photos_map.security.jwt.JwtService;
import com.idea5.four_cut_photos_map.security.jwt.dto.response.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KakaoLoginUseCaseTest {
    @Mock
    private KakaoService kakaoService;

    @Mock
    private MemberRequestServiceImpl memberRequestService;

    @Mock
    private MemberTitleServiceImpl memberTitleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private KakaoLoginUseCase kakaoLoginUseCase;

    @Test
    @DisplayName("회원이 존재할 때 회원 타이틀을 발행하지 않고 JWT토큰 생성 및 반환")
    void execute_WhenMemberExist_DoNotIssueMemberTitle() throws JsonProcessingException {
        // given
        String code = "sampleCode";
        String redirectURI = "http://localhost/callback";
        String originHeader = "http://localhost";

        KakaoTokenResp kakaoToken = new KakaoTokenResp();
        KakaoUserInfoParam kakaoUserInfo = new KakaoUserInfoParam(12345L, "nickname");
        MemberResponse memberResponse = new MemberResponse(1L, kakaoUserInfo.getNickname()+"1234", null, Collections.singletonList(new SimpleGrantedAuthority("MEMBER")));
        LoginResponse loginResponse = new LoginResponse(memberResponse, false);
        TokenResponse tokenResponse = new TokenResponse("accessToken", "refreshToken");

        given(request.getHeader("Origin")).willReturn(originHeader);
        given(kakaoService.getRedirectURI(originHeader)).willReturn(redirectURI);
        given(kakaoService.getKakaoTokens(code, redirectURI)).willReturn(kakaoToken);
        given(kakaoService.getKakaoUserInfo(kakaoToken)).willReturn(kakaoUserInfo);
        given(memberRequestService.login(kakaoUserInfo, kakaoToken)).willReturn(loginResponse);
        given(jwtService.generateTokens(loginResponse.getMemberResponse().getId(), loginResponse.getMemberResponse().getAuthorities())).willReturn(tokenResponse);

        // when
        TokenResponse response = kakaoLoginUseCase.execute(code, request);

        // then
        Assertions.assertEquals(tokenResponse, response);
        verify(kakaoService, times(1)).getRedirectURI(originHeader);
        verify(kakaoService, times(1)).getKakaoTokens(code, redirectURI);
        verify(kakaoService, times(1)).getKakaoUserInfo(kakaoToken);
        verify(memberRequestService, times(1)).login(kakaoUserInfo, kakaoToken);
        verify(memberTitleService, never()).issueMemberTitleLog(anyLong(), anyLong());
        verify(jwtService, times(1)).generateTokens(loginResponse.getMemberResponse().getId(), loginResponse.getMemberResponse().getAuthorities());
    }

    @Test
    @DisplayName("회원이 존재하지 않을 때 회원칭호 발행, JWT 토큰 생성 및 반환")
    void execute_WhenMemberNotExist_IssueMemberTitle() throws JsonProcessingException {
        // given
        String originHeader = "http://localhost";
        String redirectURI = "http://localhost/callback";
        String code = "sampleCode";
        KakaoTokenResp kakaoToken = new KakaoTokenResp();
        KakaoUserInfoParam kakaoUserInfo = new KakaoUserInfoParam(12345L, "nickname");
        MemberResponse memberResponse = new MemberResponse(1L, kakaoUserInfo.getNickname()+"1234", null, Collections.singletonList(new SimpleGrantedAuthority("MEMBER")));
        LoginResponse loginResponse = new LoginResponse(memberResponse, true);
        TokenResponse tokenResponse = new TokenResponse("accessToken", "refreshToken");

        given(request.getHeader("Origin")).willReturn(originHeader);
        given(kakaoService.getRedirectURI(originHeader)).willReturn(redirectURI);
        given(kakaoService.getKakaoTokens(code, redirectURI)).willReturn(kakaoToken);
        given(kakaoService.getKakaoUserInfo(kakaoToken)).willReturn(kakaoUserInfo);
        given(memberRequestService.login(kakaoUserInfo, kakaoToken)).willReturn(loginResponse);
        given(jwtService.generateTokens(loginResponse.getMemberResponse().getId(), loginResponse.getMemberResponse().getAuthorities())).willReturn(tokenResponse);

        // when
        TokenResponse response = kakaoLoginUseCase.execute(code, request);

        // then
        Assertions.assertEquals(tokenResponse, response);
        verify(kakaoService, times(1)).getRedirectURI(originHeader);
        verify(kakaoService, times(1)).getKakaoTokens(code, redirectURI);
        verify(kakaoService, times(1)).getKakaoUserInfo(kakaoToken);
        verify(memberRequestService, times(1)).login(kakaoUserInfo, kakaoToken);
        verify(memberTitleService, times(1)).issueMemberTitleLog(loginResponse.getMemberResponse().getId(), MemberTitleType.NEWBIE.getCode());
        verify(jwtService, times(1)).generateTokens(loginResponse.getMemberResponse().getId(), loginResponse.getMemberResponse().getAuthorities());
    }

    @Test
    @DisplayName("잘못된 code, redirectURI 인해 카카오 토큰을 가져오기 실패할 경우 실패처리")
    void execute_() throws JsonProcessingException {
        // given
        String code = "sampleCode";
        String originHeader = "http://localhost";
        JsonProcessingException exception = new JsonProcessingException("Kakao service exception") {};

        given(request.getHeader("Origin")).willReturn(originHeader);
        given(kakaoService.getKakaoTokens(any(), any())).willThrow(exception);

        // when & then
        JsonProcessingException response = assertThrows(JsonProcessingException.class,
                () -> kakaoLoginUseCase.execute(code, request));
        verify(kakaoService, never()).getKakaoUserInfo(any());
        verify(memberRequestService, never()).login(any(), any());
        verify(memberTitleService, never()).issueMemberTitleLog(anyLong(), anyLong());
        verify(jwtService, never()).generateTokens(anyLong(), anyCollection());
    }
}

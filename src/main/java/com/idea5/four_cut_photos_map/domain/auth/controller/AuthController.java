package com.idea5.four_cut_photos_map.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.dto.request.RefreshTokenReq;
import com.idea5.four_cut_photos_map.domain.member.service.MemberService;
import com.idea5.four_cut_photos_map.global.util.Util;
import com.idea5.four_cut_photos_map.security.jwt.JwtService;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import com.idea5.four_cut_photos_map.security.jwt.dto.response.TokenResponse;
import com.idea5.four_cut_photos_map.usecase.KakaoLoginUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 인증 관련 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    private final MemberService memberService;
    private final KakaoLoginUseCase kakaoLoginUseCase;

    /**
     * 카카오 로그인
     * @param code 인가코드
     */
    @PreAuthorize("isAnonymous()")
    @GetMapping("/login/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestParam String code, HttpServletRequest request) throws JsonProcessingException {
        log.info("code = " + code);
        log.info("origin = " + request.getHeader("Origin"));
        log.info("referer = " + request.getHeader("referer"));
        log.info("X-Forwarded-For = " + request.getHeader("X-Forwarded-For"));
        log.info("Remote Addr = " + request.getRemoteAddr());
        log.info("Remote Host = " + request.getRemoteHost());
        log.info("client ip = " + Util.getClientIpAddr(request));

        TokenResponse response = kakaoLoginUseCase.execute(code, request);
        return ResponseEntity.ok(response);
    }

    /**
     * refreshToken 으로 accessToken 재발급
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenReq refreshTokenReq) {
        TokenResponse response = jwtService.reissueAccessToken(refreshTokenReq.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 서비스 로그아웃
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@AuthenticationPrincipal MemberContext memberContext) {
        // 서비스 로그아웃
        log.info("서비스 로그아웃");
        memberService.logout(memberContext.getId());
        return ResponseEntity.ok(null);
    }
}

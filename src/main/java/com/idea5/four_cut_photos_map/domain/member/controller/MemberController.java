package com.idea5.four_cut_photos_map.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.service.KakaoService;
import com.idea5.four_cut_photos_map.domain.member.dto.request.MemberUpdateRequest;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberWithdrawlResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResponse;
import com.idea5.four_cut_photos_map.domain.member.service.MemberReadService;
import com.idea5.four_cut_photos_map.domain.member.service.MemberRequestService;
import com.idea5.four_cut_photos_map.domain.member.service.MemberService;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import com.idea5.four_cut_photos_map.global.common.response.RsData;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final KakaoService kakaoService;
    private final MemberRequestService memberRequestService;
    private final MemberReadService memberReadService;

    private final String BEARER_TOKEN_PREFIX = "Bearer ";

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> getProfile(@AuthenticationPrincipal MemberContext memberContext) {
        MemberInfoResponse response = memberReadService.getMemberInfo(memberContext.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/nickname")
    public ResponseEntity<Object> updateNickname(
            @AuthenticationPrincipal MemberContext memberContext,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        String updateNickname = memberRequestService.updateNickname(memberContext.getId(), request.getNickname());
        return ResponseEntity.ok(updateNickname);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/check-nickname")
    public ResponseEntity<NicknameCheckResponse> checkNickname(
            @AuthenticationPrincipal MemberContext memberContext,
            @RequestParam String nickname
    ) {
        NicknameCheckResponse response = memberReadService.checkNickname(memberContext.getId(), nickname);
        return ResponseEntity.ok(response);
    }

    // 회원 대표 칭호 수정
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/main-title/{member-title-id}")
    public ResponseEntity<RsData> updateMainMemberTitle(
            @AuthenticationPrincipal MemberContext memberContext,
            @PathVariable(value = "member-title-id") Long memberTitleId
    ) {
        // TODO: 2가지 방식 고민중
        // 1. 기존처럼 member 객체를 넘기는 방법
        // 2. memberId 만 넘기고 실질적으로 조회쿼리가 날라가는 memberTitleService.updateMainMemberTitle() 내에서 member 객체를 만들어 사용하는 방식
        memberService.updateMainMemberTitle(memberContext.getMember(), memberTitleId);
        return ResponseEntity.ok(null);
    }

    // 회원탈퇴
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("")
    public ResponseEntity<MemberWithdrawlResp> deleteMember(@AuthenticationPrincipal MemberContext memberContext) throws JsonProcessingException {
        // Kakao Access Token 은 Redis 에서 가져오기
        String kakaoAccessToken = memberService.getKakaoAccessToken(memberContext.getId());
        // 1. 카카오 토큰 만료시 토큰 갱신하기
        if(kakaoAccessToken == null || kakaoService.isExpiredAccessToken(kakaoAccessToken)) {
            // Kakao Refresh Token 은 DB 에서 가져오기
            String kakaoRefreshToken = memberService.getKakaoRefreshToken(memberContext.getId());
            kakaoAccessToken = kakaoService.refresh(kakaoRefreshToken);
        }
        // 2. 연결 끊기
        kakaoService.disconnect(kakaoAccessToken);
        MemberWithdrawlResp memberWithdrawlResp = memberService.deleteMember(memberContext.getId());
        return ResponseEntity.ok(memberWithdrawlResp);
    }
}

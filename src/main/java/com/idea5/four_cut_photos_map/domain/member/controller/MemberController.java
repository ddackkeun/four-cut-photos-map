package com.idea5.four_cut_photos_map.domain.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.idea5.four_cut_photos_map.domain.auth.service.KakaoService;
import com.idea5.four_cut_photos_map.domain.member.dto.request.MemberUpdateRequest;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResponse;
import com.idea5.four_cut_photos_map.domain.member.service.MemberReadService;
import com.idea5.four_cut_photos_map.domain.member.service.MemberRequestService;
import com.idea5.four_cut_photos_map.domain.member.service.MemberService;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleResponse;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewReadService;
import com.idea5.four_cut_photos_map.global.util.CursorRequest;
import com.idea5.four_cut_photos_map.global.util.CursorResponse;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    private final MemberTitleService memberTitleService;
    private final ReviewReadService reviewReadService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> getProfile(@AuthenticationPrincipal MemberContext memberContext) {
        MemberInfoResponse response = memberReadService.getMemberInfo(memberContext.getId());
        return ResponseEntity.ok(response);
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/titles")
    public ResponseEntity<List<MemberTitleResponse>> getMemberTitle(@AuthenticationPrincipal MemberContext memberContext) {
        List<MemberTitleResponse> response = memberTitleService.getMemberTitles(memberContext.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal MemberContext memberContext,
            @RequestBody @Valid MemberUpdateRequest request
    ) {
        memberRequestService.updateNickname(memberContext.getId(), request.getNickname());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/titles/{member-title-id}/main")
    public ResponseEntity<Void> updateMainMemberTitle(
            @AuthenticationPrincipal MemberContext memberContext,
            @PathVariable(value = "member-title-id") Long memberTitleId
    ) {
        memberTitleService.updateMainMemberTitle(memberContext.getId(), memberTitleId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteMember(@AuthenticationPrincipal MemberContext memberContext) throws JsonProcessingException {
        // Kakao Access Token 은 Redis 에서 가져오기
        String kakaoAccessToken = memberService.getKakaoAccessToken(memberContext.getId());
        // 1. 카카오 토큰 만료시 토큰 갱신하기
        if(kakaoAccessToken == null || kakaoService.isExpiredAccessToken(kakaoAccessToken)) {
            // Kakao Refresh Token 은 DB 에서 가져오기
            String kakaoRefreshToken = memberService.getKakaoRefreshToken(memberContext.getId());
            kakaoAccessToken = kakaoService.refresh(kakaoRefreshToken);
        }

        kakaoService.disconnect(kakaoAccessToken);

        memberRequestService.deleteMember(memberContext.getId());

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{member-id}/reviews")
    public ResponseEntity<CursorResponse<MemberReviewResponse>> getMemberReviews(
            @PathVariable("member-id") Long memberId,
            @Valid CursorRequest cursorRequest
    ) {
        CursorResponse<MemberReviewResponse> response = reviewReadService.getMemberReviews(memberId, cursorRequest);
        return ResponseEntity.ok(response);
    }
}

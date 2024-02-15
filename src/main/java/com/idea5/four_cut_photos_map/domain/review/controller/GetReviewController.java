package com.idea5.four_cut_photos_map.domain.review.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponseDetail;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.service.GetReviewService;
import com.idea5.four_cut_photos_map.domain.review.service.GetReviewServiceImpl;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class GetReviewController {
    private final GetReviewService getReviewService;

    /**
     * 리뷰 단건 조회
     */
    @GetMapping("/{review-id}")
    public ResponseEntity<ReviewResponseDetail> getReview(@PathVariable("review-id") Long reviewId) {
        ReviewResponseDetail response = getReviewService.getReviewById(reviewId);

        return ResponseEntity.ok(response);
    }

    /**
     * 회원 전체 리뷰 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/member")
    public ResponseEntity<List<MemberReviewResponse>> getAllReviewsForMember(@AuthenticationPrincipal MemberContext memberContext) {
        List<MemberReviewResponse> response = getReviewService.getAllReviewsForMember(memberContext.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * 지점 전체 리뷰 조회
     */
    @GetMapping("/shop/{shop-id}")
    public ResponseEntity<List<ShopReviewResponse>> getAllReviewsForShop(@PathVariable("shop-id") Long shopId) {
        List<ShopReviewResponse> response = getReviewService.getAllReviewsForShop(shopId);

        return ResponseEntity.ok(response);
    }
}

package com.idea5.four_cut_photos_map.domain.review.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewReadService;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.S3Service;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopRequestService;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewReadService reviewReadService;
    private final ReviewRequestService reviewRequestService;
    private final ShopRequestService shopRequestService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{review-id}")
    public ResponseEntity<ReviewDetailResponse> getReview(@PathVariable("review-id") Long reviewId) {
        ReviewDetailResponse response = reviewReadService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{review-id}")
    public ResponseEntity<Void> modifyReview(
            @PathVariable("review-id") Long reviewId,
            @AuthenticationPrincipal MemberContext memberContext,
            @Valid @RequestBody ReviewRequest request
    ) {
        Long shopId = reviewRequestService.modifyReview(memberContext.getId(), reviewId, request);
        shopRequestService.updateReviewInfo(shopId);        // TODO 추후 배치 등 이용해서 상점 정보 갱신

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{review-id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("review-id") Long reviewId,
            @AuthenticationPrincipal MemberContext memberContext
    ) {
        Long shopId = reviewRequestService.deleteReview(memberContext.getId(), reviewId);
        shopRequestService.updateReviewInfo(shopId);        // TODO 추후 배치 등 이용해서 상점 정보 갱신

        return ResponseEntity.ok().build();
    }

}

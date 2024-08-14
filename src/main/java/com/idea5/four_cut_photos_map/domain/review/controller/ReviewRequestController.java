package com.idea5.four_cut_photos_map.domain.review.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopService;
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
public class ReviewRequestController {
    private final ReviewRequestService reviewRequestService;
    private final ShopService shopService;

    /**
     * 특정 리뷰 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{review-id}")
    public ResponseEntity<String> modifyReview(@PathVariable("review-id") Long reviewId,
                                               @AuthenticationPrincipal MemberContext memberContext,
                                               @Valid @RequestBody ReviewRequest request) {
        Long shopId = reviewRequestService.modifyReview(memberContext.getId(), reviewId, request);
        log.info("shopId of modified reviews: {}", shopId);

        // TODO 추후 배치 등 이용해서 상점 정보 갱신
        shopService.updateReviewInfo(shopId);

        return ResponseEntity.ok("리뷰 수정 완료");
    }

    /**
     * 특정 리뷰 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{review-id}")
    public ResponseEntity<String> deleteReview(@PathVariable("review-id") Long reviewId,
                                               @AuthenticationPrincipal MemberContext memberContext) {
        Long shopId = reviewRequestService.deleteReview(memberContext.getId(), reviewId);

        // TODO 추후 배치 등 이용해서 상점 정보 갱신
        shopService.updateReviewInfo(shopId);

        return ResponseEntity.ok("리뷰 삭제 완료");
    }

}

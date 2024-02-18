package com.idea5.four_cut_photos_map.domain.review.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.service.RequestReviewService;
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
public class RequestReviewController {
    private final RequestReviewService requestReviewService;
    private final ShopService shopService;

    /**
     * 상점 리뷰 작성
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/shop/{shop-id}")
    public ResponseEntity<String> writeReview(@PathVariable("shop-id") Long shopId,
                                              @AuthenticationPrincipal MemberContext memberContext,
                                              @Valid @RequestBody ReviewRequest request) {
        ReviewResponse reviewResponse = requestReviewService.writeReviewForShop(shopId, memberContext.getId(), request);
        log.info("writeReviewForShop reviewResponse: {}", reviewResponse);

        // TODO 추후 배치 등 이용해서 상점 정보 갱신
        shopService.updateReviewInfo(shopId);

        return ResponseEntity.ok("상점 리뷰 작성 성공");
    }

    /**
     * 특정 리뷰 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{review-id}")
    public ResponseEntity<String> modifyReview(@PathVariable("review-id") Long reviewId,
                                               @AuthenticationPrincipal MemberContext memberContext,
                                               @Valid @RequestBody ReviewRequest request) {
        Long shopId = requestReviewService.modifyReview(memberContext.getId(), reviewId, request);
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
        Long shopId = requestReviewService.deleteReview(memberContext.getId(), reviewId);

        // TODO 추후 배치 등 이용해서 상점 정보 갱신
        shopService.updateReviewInfo(shopId);

        return ResponseEntity.ok("리뷰 삭제 완료");
    }

}

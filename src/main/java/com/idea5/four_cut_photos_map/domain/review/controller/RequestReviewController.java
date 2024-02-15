package com.idea5.four_cut_photos_map.domain.review.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponseDetail;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoDto;
import com.idea5.four_cut_photos_map.domain.review.service.GetReviewServiceImpl;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewWriteService;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopService;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class RequestReviewController {
    private final GetReviewServiceImpl getReviewServiceImpl;
    private final ReviewWriteService reviewWriteService;
    private final ShopService shopService;

    /**
     * 특정 리뷰 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{review-id}")
    public ResponseEntity<String> modifyReview(@PathVariable("review-id") Long reviewId,
                                               @AuthenticationPrincipal MemberContext memberContext,
                                               @Valid @RequestBody ReviewRequest reviewDto) {
        ReviewResponseDetail reviewResponseDetail = reviewWriteService.modify(memberContext.getMember(), reviewId, reviewDto);

        // 추후 배치 등 이용해서 상점 정보 갱신
        ShopReviewInfoDto shopReviewInfo = getReviewServiceImpl.getShopReviewInfo(reviewResponseDetail.getShopInfo().getId());
        shopService.updateReviewInfo(shopReviewInfo);

        return ResponseEntity.ok("리뷰 수정 완료");
    }

    /**
     * 특정 리뷰 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{review-id}")
    public ResponseEntity<String> deleteReview(@PathVariable("review-id") Long reviewId,
                                               @AuthenticationPrincipal MemberContext memberContext) {
        Long shopId = reviewWriteService.delete(memberContext.getMember(), reviewId);

        // 추후 배치 등 이용해서 상점 정보 갱신
        ShopReviewInfoDto shopReviewInfo = getReviewServiceImpl.getShopReviewInfo(shopId);
        shopService.updateReviewInfo(shopReviewInfo);

        return ResponseEntity.ok("리뷰 삭제 완료");
    }

    /**
     * 상점 리뷰 작성
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/shop/{shop-id}")
    public ResponseEntity<String> writeReview(@PathVariable("shop-id") Long shopId,
                                              @AuthenticationPrincipal MemberContext memberContext,
                                              @Valid @RequestBody ReviewRequest reviewDto) {
        ReviewResponseDetail reviewResponseDetail = reviewWriteService.write(memberContext.getMember(), shopId, reviewDto);

        // 추후 배치 등 이용해서 상점 정보 갱신
        ShopReviewInfoDto shopReviewInfo = getReviewServiceImpl.getShopReviewInfo(reviewResponseDetail.getShopInfo().getId());
        shopService.updateReviewInfo(shopReviewInfo);

        return ResponseEntity.ok("상점 리뷰 작성 성공");
    }
}

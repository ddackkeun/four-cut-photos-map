package com.idea5.four_cut_photos_map.domain.shop.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopRequestService;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopRequestController {
    private final ShopRequestService shopRequestService;
    private final ReviewRequestService reviewRequestService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{shop-id}/review")
    public ResponseEntity<Void> writeShopReview(
            @PathVariable("shop-id") Long shopId,
            @AuthenticationPrincipal MemberContext memberContext,
            @Valid @RequestBody ReviewRequest request
    ) {
        reviewRequestService.writeReview(shopId, memberContext.getId(), request);
        shopRequestService.updateReviewInfo(shopId);        // todo 비동기로 변경

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

package com.idea5.four_cut_photos_map.domain.shop.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import com.idea5.four_cut_photos_map.usecase.WriteShopReviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopRequestController {
    private final WriteShopReviewUseCase writeShopReviewUseCase;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{shop-id}/review")
    public ResponseEntity<Void> writeShopReview(
            @PathVariable("shop-id") Long shopId,
            @AuthenticationPrincipal MemberContext memberContext,
            @Valid @RequestPart(value = "review") ReviewRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        if (files == null) {
            files = Collections.emptyList();
        }

        writeShopReviewUseCase.execute(shopId, memberContext.getId(), request, files);
        return ResponseEntity.ok().build();
    }
}

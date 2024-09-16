package com.idea5.four_cut_photos_map.domain.shop.controller;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.S3Service;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopService;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopRequestController {
    private final ShopService shopService;
    private final ReviewRequestService reviewRequestService;
    private final S3Service s3Service;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{shop-id}/review")
    public ResponseEntity<Void> writeShopReview(
            @PathVariable("shop-id") Long shopId,
            @AuthenticationPrincipal MemberContext memberContext,
            @Valid @RequestPart(value = "review") ReviewRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        List<ImageUploadResponse> imageUploadResponses = s3Service.uploadImages(shopId, files);
        reviewRequestService.writeReview(shopId, memberContext.getId(), request, imageUploadResponses);
        shopService.updateReviewInfo(shopId);

        return ResponseEntity.ok().build();
    }
}

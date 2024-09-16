package com.idea5.four_cut_photos_map.domain.review.entity;

import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;

public class ReviewPhotoFactory {
    public static ReviewPhoto from(Long shopId, ReviewPhotoStatus status, ImageUploadResponse imageUploadResponse) {
        return ReviewPhoto.builder()
                .shopId(shopId)
                .fileName(imageUploadResponse.getFileName())
                .filePath(imageUploadResponse.getUrl())
                .fileType(imageUploadResponse.getType())
                .fileSize(imageUploadResponse.getSize())
                .status(status)
                .build();
    }
}

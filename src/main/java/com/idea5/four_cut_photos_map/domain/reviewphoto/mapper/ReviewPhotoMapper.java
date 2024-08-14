package com.idea5.four_cut_photos_map.domain.reviewphoto.mapper;

import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import org.springframework.stereotype.Component;

@Component
public class ReviewPhotoMapper {
    public ReviewPhoto toEntity(Long reviewId, Long shopId, ImageUploadResponse image) {
        return ReviewPhoto.builder()
                .reviewId(reviewId)
                .shopId(shopId)
                .fileName(image.getFileName())
                .filePath(image.getUrl())
                .fileType(image.getType())
                .fileSize(image.getSize())
                .build();
    }

    public ReviewPhotoResponse toResponse(ReviewPhoto reviewPhoto) {
        return ReviewPhotoResponse.builder()
                .id(reviewPhoto.getId())
                .createDate(reviewPhoto.getCreateDate().toString())
                .modifyDate(reviewPhoto.getModifyDate().toString())
                .reviewId(reviewPhoto.getReviewId())
                .fileName(reviewPhoto.getFileName())
                .filePath(reviewPhoto.getFilePath())
                .fileSize(reviewPhoto.getFileSize())
                .status(reviewPhoto.getStatus())
                .build();
    }
}

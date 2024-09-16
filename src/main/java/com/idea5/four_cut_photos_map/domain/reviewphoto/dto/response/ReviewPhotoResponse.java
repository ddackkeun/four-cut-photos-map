package com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewPhotoResponse {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;

    public static ReviewPhotoResponse from(ReviewPhoto reviewPhoto) {
        return ReviewPhotoResponse.builder()
                .id(reviewPhoto.getId())
                .fileName(reviewPhoto.getFileName())
                .filePath(reviewPhoto.getFilePath())
                .fileType(reviewPhoto.getFileType())
                .fileSize(reviewPhoto.getFileSize())
                .build();
    }
}

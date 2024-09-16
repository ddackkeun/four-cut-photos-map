package com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이미지 업로드 응답
 */
@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ImageUploadResponse {
    private String fileName;
    private String url;
    private String type;
    private Long size;

    public static ImageUploadResponse from(String fileName, String url, String type, long size) {
        return new ImageUploadResponse(fileName, url, type, size);
    }
}

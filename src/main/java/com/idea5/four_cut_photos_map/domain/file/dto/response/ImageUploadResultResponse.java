package com.idea5.four_cut_photos_map.domain.file.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 이미지 업로드 결과 응답 객체
 */
@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ImageUploadResultResponse {
    private List<ImageUploadResponse> successfulUploads;
    private List<ImageUploadResponse> failedUploads;
}

package com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewPhotoResponse {
    private Long id;
    private String url;

    public static ReviewPhotoResponse from(ReviewPhoto reviewPhoto) {
        return ReviewPhotoResponse.builder()
                .id(reviewPhoto.getId())
                .url(reviewPhoto.getUrl())
                .build();
    }
}

package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewResponse {
    private Long id;
    private String createDate;
    private String modifyDate;
    private int starRating;
    private String content;
    private String purity;
    private String retouch;
    private String item;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .createDate(review.getCreateDate().toString())
                .modifyDate(review.getModifyDate().toString())
                .starRating(review.getStarRating())
                .content(review.getContent())
                .purity(review.getPurity().toString())
                .retouch(review.getRetouch().toString())
                .item(review.getItem().toString())
                .build();
    }
}

package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopReviewSummaryResponse {
    private Long shopId;
    private int reviewCnt;
    private double starRatingAvg;

    public static ShopReviewSummaryResponse from(Long shopId, int reviewCount, double starRatingAvg) {
        return ShopReviewSummaryResponse.builder()
                .shopId(shopId)
                .reviewCnt(reviewCount)
                .starRatingAvg(starRatingAvg)
                .build();
    }
}

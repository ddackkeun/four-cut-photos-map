package com.idea5.four_cut_photos_map.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShopReviewSummary {
    private Long reviewCount;
    private Double starRatingAvg;
}

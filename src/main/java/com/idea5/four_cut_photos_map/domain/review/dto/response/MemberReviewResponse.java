package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberReviewResponse {

    // Review 관련 정보
    private ReviewResponse reviewInfo;

    // Shop 관련 정보
    private ShopResponse shopInfo;
}
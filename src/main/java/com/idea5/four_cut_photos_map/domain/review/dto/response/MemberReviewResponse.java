package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MemberReviewResponse {
    private ReviewResponse review;
    private ShopResponse shop;
    private List<ReviewPhotoResponse> photos;

    public static MemberReviewResponse from(ReviewResponse reviewResponse, ShopResponse shopResponse, List<ReviewPhotoResponse> reviewPhotoResponses) {
        return MemberReviewResponse.builder()
                .review(reviewResponse)
                .shop(shopResponse)
                .photos(reviewPhotoResponses)
                .build();
    }
}

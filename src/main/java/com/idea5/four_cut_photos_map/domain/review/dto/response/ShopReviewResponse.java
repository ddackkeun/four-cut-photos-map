package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopReviewResponse {
    private ReviewResponse review;
    private MemberResponse member;
    private List<ReviewPhotoResponse> photos;

    public static ShopReviewResponse from(ReviewResponse reviewResponse, MemberResponse memberResponse, List<ReviewPhotoResponse> reviewPhotoResponses) {
        return ShopReviewResponse.builder()
                .review(reviewResponse)
                .member(memberResponse)
                .photos(reviewPhotoResponses)
                .build();
    }
}

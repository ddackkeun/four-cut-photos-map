package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

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

    public static ShopReviewResponse from(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.from(review);
        MemberResponse memberResponse = MemberResponse.from(review.getMember());
        List<ReviewPhotoResponse> reviewPhotoResponses = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhotoResponse::from)
                .collect(Collectors.toList());

        return ShopReviewResponse.builder()
                .review(reviewResponse)
                .member(memberResponse)
                .photos(reviewPhotoResponses)
                .build();
    }
}

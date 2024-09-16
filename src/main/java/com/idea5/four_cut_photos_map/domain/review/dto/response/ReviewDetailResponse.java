package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewDetailResponse {
    private ReviewResponse review;
    private MemberResponse member;
    private ShopResponse shop;
    private List<ReviewPhotoResponse> photos;

    public static ReviewDetailResponse from(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.from(review);
        MemberResponse memberResponse = MemberResponse.from(review.getMember());
        ShopResponse shopResponse = ShopResponse.from(review.getShop());
        List<ReviewPhotoResponse> reviewPhotoResponses = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhotoResponse::from)
                .collect(Collectors.toList());

        return ReviewDetailResponse.builder()
                .review(reviewResponse)
                .member(memberResponse)
                .shop(shopResponse)
                .photos(reviewPhotoResponses)
                .build();
    }
}

package com.idea5.four_cut_photos_map.domain.review.mapper;

import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.*;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;

public class ReviewMapper {
    private static ReviewRequest setDefaultScore(ReviewRequest request) {
        if(request.getPurity() == null) request.setPurity("UNSELECTED");
        if(request.getRetouch() == null) request.setRetouch("UNSELECTED");
        if(request.getItem() == null) request.setItem("UNSELECTED");

        return request;
    }
    public static Review toEntity(Member writer, Shop shop, ReviewRequest dto) {
        dto = setDefaultScore(dto);

        return Review.builder()
                .writer(writer)
                .shop(shop)
                .starRating(dto.getStarRating())
                .content(dto.getContent())
                .purity(PurityScore.valueOf(dto.getPurity()))
                .retouch(RetouchScore.valueOf(dto.getRetouch()))
                .item(ItemScore.valueOf(dto.getItem()))
                .build();
    }

    public static Review update(Review review, ReviewRequest dto) {
        dto = setDefaultScore(dto);

        return review.update(dto);
    }

    /**
     * Review -> ReviewDto
     */
    private static ReviewResponse toReviewDto(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .createDate(review.getCreateDate())
                .modifyDate(review.getModifyDate())
                .starRating(review.getStarRating())
                .content(review.getContent())
                .purity(review.getPurity())
                .retouch(review.getRetouch())
                .item(review.getItem())
                .build();
    }

    /**
     * Review.writer -> MemberDto
     */
    private static MemberResponse toMemberDto(Member writer) {
        return MemberResponse.builder()
                .id(writer.getId())
                .nickname(writer.getNickname())
                .build();
    }

    private static MemberResponse toMemberResp(Member writer, String mainMemberTitle) {
        return MemberResponse.builder()
                .id(writer.getId())
                .nickname(writer.getNickname())
                .mainMemberTitle(mainMemberTitle)
                .build();
    }

    /**
     * Review.shop -> ShopDto
     */
    private static ShopResponse toShopDto(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .brand(shop.getBrand().getBrandName())
                .placeName(shop.getPlaceName())
                .build();
    }


    /**
     * 작성자, 지점 정보가 담긴 리뷰 DTO 반환
     */
    public static ReviewResponseDetail toResponseReviewDto(Review review) {
        return ReviewResponseDetail.builder()
                .reviewInfo(toReviewDto(review))
                .memberInfo(toMemberDto(review.getWriter()))
                .shopInfo(toShopDto(review.getShop()))
                .build();
    }


    /**
     * 지점 정보가 담긴 리뷰 DTO 반환
     */
    public static ResponseShopReviewDto toResponseShopReviewDto(Review review) {
        return ResponseShopReviewDto.builder()
                .reviewInfo(toReviewDto(review))
                .memberInfo(toMemberDto(review.getWriter()))
                .build();
    }

    // 리뷰, 회원 정보가 담긴 지점 리뷰 정보 반환
    public static ShopReviewResponse toShopReviewResp(Review review, String mainMemberTitle) {
        return ShopReviewResponse.builder()
                .reviewInfo(toReviewDto(review))
                .memberInfo(toMemberResp(review.getWriter(), mainMemberTitle))
                .build();
    }

    /**
     * 상점 정보가 담긴 리뷰 DTO 반환
     */
    public static MemberReviewResponse toResponseMemberReviewDto(Review review) {
        return MemberReviewResponse.builder()
                .reviewInfo(toReviewDto(review))
                .shopInfo(toShopDto(review.getShop()))
                .build();
    }
}

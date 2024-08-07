package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoDto;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewReadService {
    Optional<Review> getReview(Long reviewId);
    List<MemberReviewResponse> getAllReviewsForMember(Long memberId);
    List<ShopReviewResponse> getAllReviewsForShop(Long shopId);
    List<ShopReviewResponse> getRecentReviewsForShop(Long shopId);
    ShopReviewInfoDto getShopReviewInfo(Long shopId);
}

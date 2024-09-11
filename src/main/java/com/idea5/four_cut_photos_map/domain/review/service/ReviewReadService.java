package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;

import java.util.List;

public interface ReviewReadService {
    Review getRegisteredReviewWithThrow(Long reviewId);
    List<MemberReviewResponse> getAllReviewsForMember(Long memberId);
    List<ShopReviewResponse> getAllReviewsForShop(Long shopId);
    List<ShopReviewResponse> getRecentReviewsForShop(Long shopId);
    ShopReviewInfoResponse getShopReviewInfo(Long shopId);
}

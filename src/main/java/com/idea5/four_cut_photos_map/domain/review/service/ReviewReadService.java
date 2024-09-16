package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummaryResponse;

import java.util.List;

public interface ReviewReadService {
    ReviewDetailResponse getReview(Long reviewId);
    List<MemberReviewResponse> getMemberReviews(Long memberId, Long lastReviewId, int size);
    List<ShopReviewResponse> getShopReviews(Long shopId, Long lastReviewId, int size);
    ShopReviewSummaryResponse getShopReviewSummary(Long shopId);
}

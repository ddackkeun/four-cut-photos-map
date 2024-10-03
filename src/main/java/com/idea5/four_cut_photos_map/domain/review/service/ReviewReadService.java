package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.global.util.CursorRequest;
import com.idea5.four_cut_photos_map.global.util.CursorResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;

import java.util.List;

public interface ReviewReadService {
    ReviewDetailResponse getReview(Long reviewId);
    CursorResponse<MemberReviewResponse> getMemberReviews(Long memberId, CursorRequest request);
    CursorResponse<ShopReviewResponse> getShopReviews(Long shopId, CursorRequest request);
}

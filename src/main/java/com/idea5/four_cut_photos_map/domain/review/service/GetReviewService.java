package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponseDetail;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoDto;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;

import java.util.List;

public interface GetReviewService {
    ReviewResponseDetail getReviewById(Long reviewId);
    List<MemberReviewResponse> getAllReviewsForMember(Long memberId);
    List<ShopReviewResponse> getAllReviewsForShop(Long shopId);
    List<ShopReviewResponse> getRecentReviewsForShop(Long shopId);
    ShopReviewInfoDto getShopReviewInfo(Long shopId);
}

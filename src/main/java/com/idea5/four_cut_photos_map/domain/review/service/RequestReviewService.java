package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;

public interface RequestReviewService {
    ReviewResponse writeReviewForShop(Long shopId, Long writerId, ReviewRequest request);
}

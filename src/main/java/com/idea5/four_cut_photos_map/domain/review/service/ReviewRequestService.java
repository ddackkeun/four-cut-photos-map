package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;

public interface ReviewRequestService {
    void writeReview(Long shopId, Long memberId, ReviewRequest request);
    Long modifyReview(Long memberId, Long reviewId, ReviewRequest request);
    Long deleteReview(Long memberId, Long reviewId);
}

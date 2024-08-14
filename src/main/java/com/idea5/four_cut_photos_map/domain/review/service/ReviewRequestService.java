package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;

public interface ReviewRequestService {
    Review writeReview(Review review);
    Long modifyReview(Long memberId, Long reviewId, ReviewRequest request);
    Long deleteReview(Long memberId, Long reviewId);
    void deleteAllReviewsFromMember(Long memberId);
}

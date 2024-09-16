package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;

import java.util.List;

public interface ReviewRequestService {
    void writeReview(Long shopId, Long memberId, ReviewRequest request, List<ImageUploadResponse> imageUploadResponses);
    Long modifyReview(Long memberId, Long reviewId, ReviewRequest request);
    Long deleteReview(Long memberId, Long reviewId);
    void deleteMemberReviews(Long memberId);
}

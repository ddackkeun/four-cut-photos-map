package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;

import java.util.List;

public interface ReviewPhotoReadService {
    List<ReviewPhoto> getRegisteredReviewPhotos(Long reviewId);
}

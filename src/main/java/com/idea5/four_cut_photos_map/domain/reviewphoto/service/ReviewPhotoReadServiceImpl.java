package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.repository.ReviewPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewPhotoReadServiceImpl implements ReviewPhotoReadService {
    private final ReviewPhotoRepository reviewPhotoRepository;

    @Override
    public List<ReviewPhoto> getRegisteredReviewPhotos(Long reviewId) {
        return reviewPhotoRepository.findAllByReviewIdAndStatus(reviewId, ReviewPhotoStatus.REGISTERED);
    }
}

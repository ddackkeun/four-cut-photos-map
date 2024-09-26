package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.repository.ReviewPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewPhotoRequestServiceImpl implements ReviewPhotoRequestService {
    private final ReviewPhotoRepository reviewPhotoRepository;

    /*@Override
    public ReviewPhoto createReviewPhoto(ReviewPhoto reviewPhoto) {
        reviewPhoto.modifyStatus(ReviewPhotoStatus.REGISTERED);
        return reviewPhotoRepository.save(reviewPhoto);
    }*/
}

package com.idea5.four_cut_photos_map.domain.reviewphoto.repository;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {
    List<ReviewPhoto> findAllByReviewIdAndStatus(Long reviewId, ReviewPhotoStatus status);
}

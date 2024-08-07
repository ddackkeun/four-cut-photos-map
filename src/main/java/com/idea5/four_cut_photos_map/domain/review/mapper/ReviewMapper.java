package com.idea5.four_cut_photos_map.domain.review.mapper;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewMapper {
    public Review toEntity(Review review, ReviewRequest request) {
        Review newReview = toEntity(review.getWriter(), review.getShop(), request);
        newReview.setId(review.getId());
        newReview.setCreateDate(review.getCreateDate());

        return newReview;
    }
    public Review toEntity(Member writer, Shop shop, ReviewRequest request) {
        PurityScore purity = request.getPurity() == null ? PurityScore.UNSELECTED : PurityScore.valueOf(request.getPurity());
        RetouchScore retouch = request.getRetouch() == null ? RetouchScore.UNSELECTED : RetouchScore.valueOf(request.getRetouch());
        ItemScore item = request.getItem() == null ? ItemScore.UNSELECTED : ItemScore.valueOf(request.getItem());

        return Review.builder()
                .writer(writer)
                .shop(shop)
                .starRating(request.getStarRating())
                .content(request.getContent())
                .purity(purity)
                .retouch(retouch)
                .item(item)
                .build();
    }

    public ReviewResponse toResponse(Review review) {
        return toResponse(review, new ArrayList<>());
    }

    public ReviewResponse toResponse(Review review, List<ReviewPhoto> reviewPhotos) {
        List<ReviewPhotoResponse> reviewPhotoResponses = reviewPhotos.stream()
                .map(reviewPhoto -> {
                    return ReviewPhotoResponse.builder()
                            .id(reviewPhoto.getId())
                            .createDate(reviewPhoto.getCreateDate().toString())
                            .modifyDate(reviewPhoto.getModifyDate().toString())
                            .reviewId(reviewPhoto.getReviewId())
                            .filename(reviewPhoto.getFilename())
                            .filePath(reviewPhoto.getFilePath())
                            .fileType(reviewPhoto.getFileType())
                            .fileSize(reviewPhoto.getFileSize())
                            .status(reviewPhoto.getStatus())
                            .build();
                })
                .toList();

        return ReviewResponse.builder()
                .id(review.getId())
                .createDate(review.getCreateDate().toString())
                .modifyDate(review.getModifyDate().toString())
                .starRating(review.getStarRating())
                .content(review.getContent())
                .purity(review.getPurity())
                .retouch(review.getRetouch())
                .item(review.getItem())
                .reviewPhotoResponses(reviewPhotoResponses)
                .build();
    }
}

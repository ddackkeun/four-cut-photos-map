/*
package com.idea5.four_cut_photos_map.usecase;

import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewReadService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.ReviewPhotoReadService;
import com.idea5.four_cut_photos_map.domain.shop.mapper.ShopMapper;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetReviewUseCase {
    private final ReviewReadService reviewReadService;
    private final ReviewPhotoReadService reviewPhotoReadService;

    @Transactional(readOnly = true)
    public ReviewDetailResponse execute(Long reviewId) {
        return Optional.ofNullable(reviewId)
                .map(reviewReadService::getRegisteredReviewWithThrow)
                .map(review -> {
                    List<ReviewPhoto> reviewPhotos = reviewPhotoReadService.getRegisteredReviewPhotos(review.getId());

                    return ReviewDetailResponse.builder()
                            .reviewInfo(reviewMapper.toResponse(review, reviewPhotos))
                            .memberInfo(memberMapper.toResponse(review.getMember()))
                            .shopInfo(shopMapper.toResponse(review.getShop(), review.getShop().getBrand()))
                            .build();
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_REQUEST_DATA));
    }
}
*/

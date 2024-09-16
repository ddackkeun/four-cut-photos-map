/*
package com.idea5.four_cut_photos_map.usecase;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.service.MemberReadService;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.mapper.ReviewPhotoMapper;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.ReviewPhotoRequestService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.S3Service;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopReadService;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopRequestService;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WriteShopReviewUseCase {
    private final ShopReadService shopReadService;
    private final MemberReadService memberReadService;
    private final ReviewRequestService reviewRequestService;
    private final ReviewPhotoRequestService reviewPhotoRequestService;
    private final S3Service s3Service;
    private final ShopRequestService shopRequestService;

    private final ReviewMapper reviewMapper;
    private final ReviewPhotoMapper reviewPhotoMapper;


    */
/**
     * 상점에 리뷰를 작성하는 로직
     * 1. shopId 통해 상점 검색, memberId를 통해 사용자 검색
     * - 상점 혹은 사용자가 없으면 예외 발생
     * 2. 상점, 멤버, request 통해 review 생성하고 저장
     * 3. 요청의 이미지를 S3 버킷에 저장
     * 4. S3 저장에 성공한 이미지, shopId, reviewId 통해 ReviewPhoto 생성
     * 5. 상점 리뷰 개수 및 평점 등 집계 갱신
     *//*

    @Transactional(readOnly = false)
    public void execute(long shopId, long memberId, ReviewRequest request, List<MultipartFile> files) {
        Shop shop = shopReadService.getShopWithThrow(shopId);
        Member member = memberReadService.getMemberWithThrow(memberId);

        Review review = Optional.ofNullable(request)
                .map(it -> reviewMapper.toEntity(shop, member, it))
                .map(reviewRequestService::writeReview)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_REQUEST_DATA));

        s3Service.uploadImages(shop.getBrand().getBrandName(), files).stream()
                .filter(ImageUploadResponse::getSuccess)
                .map(imageUploadResponse -> reviewPhotoMapper.toEntity(review.getId(), shop.getId(), imageUploadResponse))
                .forEach(reviewPhotoRequestService::createReviewPhoto);

        // TODO 상점 리뷰 집계 테이블 분리
        // TODO 배치, 스케쥴러 사용하여 상점 정보 갱신 비동기 처리
        shopRequestService.updateReviewInfo(shop);
    }
}
*/

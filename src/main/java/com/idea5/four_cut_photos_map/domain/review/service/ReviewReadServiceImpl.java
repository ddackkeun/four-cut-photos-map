package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.global.util.CursorRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.*;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.CursorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewReadServiceImpl implements ReviewReadService {
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;

    @Override
    public ReviewDetailResponse getReview(Long reviewId) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        return toReviewDetailResponse(review);
    }

    @Override
    public List<MemberReviewResponse> getMemberReviews(Long memberId, Long lastReviewId, int size) {
        memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return reviewRepository.findAllByMemberIdAndStatusAndIdLessThan(memberId, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size)).stream()
                .map(this::toMemberReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CursorResponse<ShopReviewResponse> getShopReviews(Long shopId, CursorRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        long lastReviewId = request.getKeyOrDefault(Long.MAX_VALUE);
        int size = request.getSizeOrDefault(10);
        List<Review> reviews = reviewRepository.findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size));

        long nextKey = getNextKey(reviews);
        List<ShopReviewResponse> responses = reviews.stream()
                .map(this::toShopReviewResponse)
                .toList();

        return new CursorResponse<>(CursorRequest.of(nextKey, size), responses);
    }

    private ReviewDetailResponse toReviewDetailResponse(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.from(review);
        MemberResponse memberResponse = MemberResponse.from(review.getMember());
        ShopResponse shopResponse = ShopResponse.from(review.getShop());
        List<ReviewPhotoResponse> reviewPhotoResponses = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhotoResponse::from)
                .toList();

        return ReviewDetailResponse.from(reviewResponse, memberResponse, shopResponse, reviewPhotoResponses);
    }

    private MemberReviewResponse toMemberReviewResponse(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.from(review);
        ShopResponse shopResponse = ShopResponse.from(review.getShop());
        List<ReviewPhotoResponse> reviewPhotoResponses = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhotoResponse::from)
                .toList();

        return MemberReviewResponse.from(reviewResponse, shopResponse, reviewPhotoResponses);
    }

    private ShopReviewResponse toShopReviewResponse(Review review) {
        ReviewResponse reviewResponse = ReviewResponse.from(review);
        MemberResponse memberResponse = MemberResponse.from(review.getMember());
        List<ReviewPhotoResponse> reviewPhotoResponses = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhotoResponse::from)
                .toList();

        return ShopReviewResponse.from(reviewResponse, memberResponse, reviewPhotoResponses);
    }

    private long getNextKey(List<Review> reviews) {
        return reviews.stream()
                .mapToLong(BaseEntity::getId)
                .min()
                .orElse(CursorRequest.NONE_KEY);
    }
}

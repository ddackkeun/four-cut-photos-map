package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummaryResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED).orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewDetailResponse.from(review);
    }

    @Override
    public List<MemberReviewResponse> getMemberReviews(Long memberId, Long lastReviewId, int size) {
        memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (lastReviewId == null) {
            lastReviewId = Long.MAX_VALUE;
        }

        Pageable pageable = PageRequest.of(0, size);
        return reviewRepository.findReviewsWithShopByMemberAndCursor(memberId, lastReviewId, ReviewStatus.REGISTERED, pageable).stream()
                .map(MemberReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopReviewResponse> getShopReviews(Long shopId, Long lastReviewId, int size) {
        shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        if(lastReviewId == null) {
            lastReviewId = Long.MAX_VALUE;
        }

        Pageable pageable = PageRequest.of(0, size);
        return reviewRepository.findReviewsWithMemberByShopAndCursor(shopId, lastReviewId, ReviewStatus.REGISTERED, pageable).stream()
                .map(ShopReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public ShopReviewSummaryResponse getShopReviewSummary(Long shopId) {
        shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByShopIdAndStatus(shopId, ReviewStatus.REGISTERED);
        int reviewCount = reviews.size();
        double starRatingAvg = reviews.stream()
                .mapToDouble(Review::getStarRating)
                .average()
                .orElse(0.0);
        starRatingAvg = Math.round(starRatingAvg * 10) / 10.0;

        return ShopReviewSummaryResponse.from(shopId, reviewCount, starRatingAvg);
    }
}

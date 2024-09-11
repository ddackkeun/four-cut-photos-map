package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.mapper.ShopMapper;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final ReviewMapper reviewMapper;
    private final MemberMapper memberMapper;
    private final ShopMapper shopMapper;

    @Override
    public Review getRegisteredReviewWithThrow(Long reviewId) {
        return reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Override
    public List<MemberReviewResponse> getAllReviewsForMember(Long memberId) {
        List<Review> reviews = reviewRepository.findAllByMemberIdOrderByCreateDateDesc(memberId);

        return reviews.stream()
                .map(review -> {
                    return MemberReviewResponse.builder()
                            .reviewInfo(reviewMapper.toResponse(review))
                            .shopInfo(shopMapper.toResponse(review.getShop(), review.getShop().getBrand()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopReviewResponse> getAllReviewsForShop(Long shopId) {
        shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId);   // 최신 작성순
        return createShopReviewResponse(reviews);
    }

    // 작성일 기준 최신 리뷰 3개 조회
    @Override
    public List<ShopReviewResponse> getRecentReviewsForShop(Long shopId) {
        List<Review> reviews = reviewRepository.findTop3ByShopIdOrderByCreateDateDesc(shopId);

        return createShopReviewResponse(reviews);
    }

    @Override
    public ShopReviewInfoResponse getShopReviewInfo(Long shopId) {
        shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByShopId(shopId);
        int reviewCount = reviews.size();

        double starRatingAvg = reviews.stream()
                .mapToDouble(Review::getStarRating)
                .average()
                .orElse(0.0);
        starRatingAvg = Math.round(starRatingAvg * 10) / 10.0;

        return new ShopReviewInfoResponse(shopId, reviewCount, starRatingAvg);
    }

    private List<ShopReviewResponse> createShopReviewResponse(List<Review> reviews) {
        return reviews.stream()
                .map(review -> {
                    Member member = review.getMember();
                    String mainMemberTitleName = member.getMainTitleName() == null ? "" : member.getMainTitleName();
                    return ShopReviewResponse.builder()
                            .reviewInfo(reviewMapper.toResponse(review))
                            .memberInfo(memberMapper.toResponse(member, mainMemberTitleName))
                            .build();
                })
                .collect(Collectors.toList());
    }
}

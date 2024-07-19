package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestReviewServiceImpl implements RequestReviewService {
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;

    private final ReviewMapper reviewMapper;

    public void matchMemberIdAndWriterId(long memberId, long writerId) {
        if(memberId != writerId) {
            throw new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);
        }
    }

    @Override
    public ReviewResponse writeReviewForShop(Long shopId, Long writerId, ReviewRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        Member member = memberRepository.findById(writerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return Optional.ofNullable(request)
                .map(it -> reviewMapper.toEntity(member, shop, it))
                .map(reviewRepository::save)
                .map(reviewMapper::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_PARAMETER));
    }

    @Override
    public Long modifyReview(Long memberId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getWriter().getId());

        Review newReview = reviewMapper.toEntity(review, request);
        reviewRepository.save(newReview);

        return newReview.getShop().getId();
    }

    @Override
    public Long deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getWriter().getId());
        Long shopId = review.getShop().getId();

        reviewRepository.delete(review);
        return shopId;
    }

    public void deleteByWriterId(Long memberId) {
        reviewRepository.deleteByWriterId(memberId);
    }

}

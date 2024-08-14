package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewRequestServiceImpl implements ReviewRequestService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public void matchMemberIdAndWriterId(long memberId, long writerId) {
        if(memberId != writerId) {
            throw new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);
        }
    }

    @Override
    public Review writeReview(Review review) {
        review.changeStatus(ReviewStatus.REGISTERED);
        return reviewRepository.save(review);
    }

    @Override
    public Long modifyReview(Long memberId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getMember().getId());

        Review newReview = reviewMapper.toEntity(review, request);
        reviewRepository.save(newReview);

        return newReview.getShop().getId();
    }

    @Override
    public Long deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getMember().getId());
        Long shopId = review.getShop().getId();

        reviewRepository.delete(review);
        return shopId;
    }

    @Override
    public void deleteAllReviewsFromMember(Long memberId) {
        //TODO 배치 작업 or 비동기 처리를 통해서 회원 리뷰 삭제하도록 변경
        List<Review> reviews = reviewRepository.findAllByMemberId(memberId).stream()
                .map(review -> {
                    review.changeStatus(ReviewStatus.DELETED);
                    return reviewRepository.save(review);
                })
                .toList();
    }

}

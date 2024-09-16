package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.ReviewFactory;
import com.idea5.four_cut_photos_map.domain.review.entity.ReviewPhotoFactory;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewRequestServiceImpl implements ReviewRequestService {
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;

    public void matchMemberIdAndWriterId(long memberId, long writerId) {
        if(memberId != writerId) {
            throw new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);
        }
    }

    @Override
    public void writeReview(Long shopId, Long memberId, ReviewRequest request, List<ImageUploadResponse> imageUploadResponses) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = Optional.ofNullable(request)
                .map(it -> ReviewFactory.from(member, shop, ReviewStatus.REGISTERED, it))
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_REQUEST_DATA));

        imageUploadResponses.stream()
                .map(imageUploadResponse -> ReviewPhotoFactory.from(shopId, ReviewPhotoStatus.REGISTERED, imageUploadResponse))
                .forEach(review::addPhoto);

        reviewRepository.save(review);
    }

    @Override
    public Long modifyReview(Long memberId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED).orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getMember().getId());

        review.update(request.getStarRating(), request.getContent(), request.getPurity(), request.getRetouch(), request.getItem());
        return review.getShop().getId();
    }

    @Override
    public Long deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        matchMemberIdAndWriterId(memberId, review.getMember().getId());

        review.updateStatus(ReviewStatus.DELETED);
        review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .peek(reviewPhoto -> reviewPhoto.updateStatus(ReviewPhotoStatus.DELETED));

        return review.getShop().getId();
    }

    @Override
    public void deleteMemberReviews(Long memberId) {
        //TODO 배치 작업 or 비동기 처리를 통해서 회원 리뷰 삭제하도록 변경
        reviewRepository.findAllByMemberIdAndStatus(memberId, ReviewStatus.REGISTERED).stream()
                .peek(review -> {
                    review.updateStatus(ReviewStatus.DELETED);
                    review.getPhotos().stream()
                            .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                            .peek(reviewPhoto -> reviewPhoto.updateStatus(ReviewPhotoStatus.DELETED));
                });
    }

}

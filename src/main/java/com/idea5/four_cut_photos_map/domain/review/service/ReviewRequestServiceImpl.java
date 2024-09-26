package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.ReviewFactory;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewRequestServiceImpl implements ReviewRequestService {
    private final ReviewRepository reviewRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;

    @Override
    public void writeReview(Long shopId, Long memberId, ReviewRequest request) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));
        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Review review = ReviewFactory.from(member, shop, request, ReviewStatus.REGISTERED);
        addPhotosToReview(review, shopId, request.getImageUrls());
        reviewRepository.save(review);
    }

    @Override
    public Long modifyReview(Long memberId, Long reviewId, ReviewRequest request) {
        Review review = getRegisteredReviewWithThrows(reviewId);
        validateWriter(memberId, review.getMember().getId());

        review.update(request.getStarRating(), request.getContent(), request.getPurity(), request.getRetouch(), request.getItem());
        updateReviewPhotos(review, request.getImageUrls());

        return review.getShop().getId();
    }

    @Override
    public Long deleteReview(Long memberId, Long reviewId) {
        Review review = getRegisteredReviewWithThrows(reviewId);
        validateWriter(memberId, review.getMember().getId());

        review.delete();

        return review.getShop().getId();
    }

    private Review getRegisteredReviewWithThrows(Long id) {
        return reviewRepository.findByIdAndStatus(id, ReviewStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateWriter(Long memberId, Long writerId) {
        if (!memberId.equals(writerId)) {
            throw new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);
        }
    }

    private void addPhotosToReview(Review review, Long shopId, List<String> imageUrls) {
        Optional.ofNullable(imageUrls)
                .orElse(Collections.emptyList())
                .stream()
                .map(imageUrl -> ReviewPhoto.create(review, shopId, imageUrl, ReviewPhotoStatus.REGISTERED))
                .forEach(review::addPhoto);
    }

    private void updateReviewPhotos(Review review, List<String> imageUrls) {
        Set<String> requestImageUrls = Optional.ofNullable(imageUrls)
                .map(Set::copyOf)
                .orElseGet(Collections::emptySet);

        deleteObsoleteReviewPhotos(review, requestImageUrls);
        addNewReviewPhotos(review, requestImageUrls);
    }

    private void deleteObsoleteReviewPhotos(Review review, Set<String> requestImageUrls) {
        review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED) && !requestImageUrls.contains(reviewPhoto.getUrl()))
                .forEach(ReviewPhoto::delete);
    }

    private void addNewReviewPhotos(Review review, Set<String> requestImageUrls) {
        Set<String> existingImageUrls = review.getPhotos().stream()
                .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                .map(ReviewPhoto::getUrl)
                .collect(Collectors.toSet());

        requestImageUrls.stream()
                .filter(imageUrl -> !existingImageUrls.contains(imageUrl))
                .map(imageUrl -> ReviewPhoto.create(review, review.getShop().getId(), imageUrl, ReviewPhotoStatus.REGISTERED))
                .forEach(review::addPhoto);
    }
}

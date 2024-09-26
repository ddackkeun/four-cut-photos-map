package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummary;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
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
public class ShopRequestServiceImpl implements ShopRequestService {
    private final ShopRepository shopRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void updateReviewInfo(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        ShopReviewSummary shopReviewSummary = getShopReviewSummary(shopId);
        shop.updateReviewSummary(shopReviewSummary.getReviewCount().intValue(), shopReviewSummary.getStarRatingAvg());
    }

    private ShopReviewSummary getShopReviewSummary(Long shopId) {
        ShopReviewSummary reviewSummary = reviewRepository.findReviewSummaryByShopIdAndStatus(shopId, ReviewStatus.REGISTERED);
        Double roundedStarRatingAvg = Optional.ofNullable(reviewSummary.getStarRatingAvg())
                .map(starRatingAvg -> Math.round(starRatingAvg * 10) / 10.0)
                .orElse(0.0);

        return new ShopReviewSummary(reviewSummary.getReviewCount(), roundedStarRatingAvg);
    }
}

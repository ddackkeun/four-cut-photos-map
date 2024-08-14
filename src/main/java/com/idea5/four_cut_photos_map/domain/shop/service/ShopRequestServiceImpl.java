package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopRequestServiceImpl implements ShopRequestService {
    private final ShopRepository shopRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void updateReviewInfo(Shop shop) {
        Integer reviewCount = reviewRepository.countByShopId(shop.getId());
        Double roundAvgStarRating = Optional.ofNullable(reviewRepository.findAverageStarRatingByShopId(shop.getId()))
                .map(avgStarRating -> Math.round(avgStarRating * 10) / 10.0)
                .orElse(0.0);

        shop.setReviewCnt(reviewCount);
        shop.setStarRatingAvg(roundAvgStarRating);
        shopRepository.save(shop);
    }
}

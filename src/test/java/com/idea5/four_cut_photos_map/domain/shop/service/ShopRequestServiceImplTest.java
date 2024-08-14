package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopRequestServiceImplTest {
    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ShopRequestServiceImpl shopRequestService;

    @Nested
    @DisplayName("상점 리뷰 정보 갱신")
    class UpdateReviewInfo {
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("Shop 리뷰 정보가 정상적으로 업데이트되는지 확인")
            void updateReviewInfo_UpdatesShopReviewInfo() {
                // given
                Long shopId = 1L;
                Integer expectedReviewCount  = 5;
                Double expectedAvgStarRating = 4.3;

                Shop shop = Shop.builder().id(shopId).placeName("test place").address("test address").favoriteCnt(0).reviewCnt(9).starRatingAvg(1.0).build();
                Shop savedShop = Shop.builder().id(shopId).placeName("test place").address("test address").favoriteCnt(0).reviewCnt(expectedReviewCount).starRatingAvg(expectedAvgStarRating).build();

                when(reviewRepository.countByShopId(shopId)).thenReturn(expectedReviewCount);
                when(reviewRepository.findAverageStarRatingByShopId(shopId)).thenReturn(expectedAvgStarRating);
                when(shopRepository.save(shop)).thenReturn(savedShop);

                // when
                shopRequestService.updateReviewInfo(shop);

                // then
                assertEquals(expectedReviewCount, savedShop.getReviewCnt());
                assertEquals(expectedAvgStarRating, savedShop.getStarRatingAvg());

                verify(reviewRepository).countByShopId(shopId);
                verify(reviewRepository).findAverageStarRatingByShopId(shopId);
                verify(shopRepository).save(shop);
            }

            @Test
            @DisplayName("리뷰가 없는 경우 Shop 별점 평균이 0.0으로 설정되는지 확인")
            void updateReviewInfo_NoReviews_SetsDefaultValues() {
                // given
                Long shopId = 1L;
                Integer expectedReviewCount  = 0;
                Double expectedAvgStarRating = 0.0;

                Shop shop = Shop.builder().id(shopId).placeName("test place").address("test address").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
                Shop savedShop = Shop.builder().id(shopId).placeName("test place").address("test address").favoriteCnt(0).reviewCnt(expectedReviewCount).starRatingAvg(expectedAvgStarRating).build();

                when(reviewRepository.countByShopId(shopId)).thenReturn(expectedReviewCount);
                when(reviewRepository.findAverageStarRatingByShopId(shopId)).thenReturn(null);
                when(shopRepository.save(shop)).thenReturn(savedShop);

                // when
                shopRequestService.updateReviewInfo(shop);

                // then
                assertEquals(expectedReviewCount, savedShop.getReviewCnt());
                assertEquals(expectedAvgStarRating, savedShop.getStarRatingAvg());

                verify(reviewRepository).countByShopId(shopId);
                verify(reviewRepository).findAverageStarRatingByShopId(shopId);
                verify(shopRepository).save(shop);
            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

        }
    }
}
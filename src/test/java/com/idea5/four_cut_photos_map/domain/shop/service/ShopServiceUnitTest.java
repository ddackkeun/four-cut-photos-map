package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.review.entity.Review;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShopServiceUnitTest {
    @InjectMocks
    private ShopService shopService;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Nested
    @DisplayName("상점 리뷰 정보 갱신")
    class UpdateReviewInfo {
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("상점의 리뷰 개수, 별점 갱신")
            void updateReviewInfoSuccessCase1() {
                // given
                Long shopId = 1L;
                List<Review> reviews = Arrays.asList(
                        Review.builder().starRating(5).build(),
                        Review.builder().starRating(3).build()
                );

                double expectedAvgRating = reviews.stream()
                        .mapToDouble(Review::getStarRating)
                        .average()
                        .orElse(0.0);
                expectedAvgRating = Math.round(expectedAvgRating * 10) / 10.0;

                Shop shop = mock(Shop.class);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(reviewRepository.findAllByShopId(shopId)).thenReturn(reviews);
                when(shopRepository.save(shop)).thenReturn(shop);

                shopService.updateReviewInfo(shopId);

                // then
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopId(shopId);
                verify(shop).setReviewCnt(reviews.size());
                verify(shop).setStarRatingAvg(expectedAvgRating);
                verify(shopRepository).save(shop);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCase {

        }
    }
}

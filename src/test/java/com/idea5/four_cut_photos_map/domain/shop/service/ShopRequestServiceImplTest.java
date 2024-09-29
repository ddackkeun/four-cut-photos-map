package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummary;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopRequestServiceImplTest {
    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ShopRequestServiceImpl shopRequestService;

    @Nested
    @DisplayName("지점 리뷰 정보 갱신")
    class UpdateReviewInfo {
        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("지점이 있을 때 지점 리뷰 집계 정보를 가져와 수정")
            void updateReviewInfo_Success1() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).reviewCnt(3).starRatingAvg(3.5).build();
                ShopReviewSummary reviewSummary = new ShopReviewSummary(5L, 3.77);
                Double roundStarRatingAvg = Math.round(reviewSummary.getStarRatingAvg() * 10) / 10.0;

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findReviewSummaryByShopIdAndStatus(shopId, ReviewStatus.REGISTERED)).willReturn(reviewSummary);

                // when
                shopRequestService.updateReviewInfo(shopId);

                // then
                assertEquals(reviewSummary.getReviewCount().intValue(), shop.getReviewCnt());
                assertEquals(roundStarRatingAvg, shop.getStarRatingAvg());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findReviewSummaryByShopIdAndStatus(shopId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("지점이 있고 지점 리뷰 집계 정보에 별점 평균이 Null일 때 0.0으로 수정")
            void updateReviewInfo_Success2() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).reviewCnt(3).starRatingAvg(3.5).build();
                ShopReviewSummary reviewSummary = new ShopReviewSummary(0L, null);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findReviewSummaryByShopIdAndStatus(shopId, ReviewStatus.REGISTERED)).willReturn(reviewSummary);

                // when
                shopRequestService.updateReviewInfo(shopId);

                // then
                assertEquals(0, shop.getReviewCnt());
                assertEquals(0.0, shop.getStarRatingAvg());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findReviewSummaryByShopIdAndStatus(shopId, ReviewStatus.REGISTERED);
            }

        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("shopId의 지점이 없을 때 예외 발생")
            void updateReviewInfo_Fail1() {
                // given
                Long shopId = 99L;

                given(shopRepository.findById(shopId)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> shopRequestService.updateReviewInfo(shopId));
                assertEquals(ErrorCode.SHOP_NOT_FOUND, result.getErrorCode());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository, never()).findReviewSummaryByShopIdAndStatus(anyLong(), any());
            }

        }
    }
}
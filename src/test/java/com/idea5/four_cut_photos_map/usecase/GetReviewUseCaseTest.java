package com.idea5.four_cut_photos_map.usecase;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewReadService;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.ReviewPhotoReadService;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.mapper.ShopMapper;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetReviewUseCaseTest {
    @Mock
    private ReviewReadService reviewReadService;

    @Mock
    private ReviewPhotoReadService reviewPhotoReadService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private ShopMapper shopMapper;

    @InjectMocks
    private GetReviewUseCase getReviewUseCase;

    @Test
    @DisplayName("reviewId에 해당하는 리뷰가 존재하며, 리뷰 id를 가진 사진이 존재하는 경우")
    @Transactional(readOnly = true)
    public void execute_WhenReviewFoundWithReviewPhotos_ReturnReviewDetailResponse() {
        // given
        Long reviewId = 1L;
        Long memberId = 1L;
        Long brandId = 1L;
        Long shopId = 1L;

        Member member = Member.builder().id(memberId).build();
        Brand brand = Brand.builder().id(brandId).build();
        Shop shop = Shop.builder().id(shopId).brand(brand).build();
        Review review = Review.builder().id(reviewId).member(member).shop(shop).build();
        ReviewPhoto reviewPhoto = ReviewPhoto.builder().id(1L).reviewId(reviewId).fileName("valid1").fileType("image/jpeg").fileSize(100).status(ReviewPhotoStatus.REGISTERED).build();
        List<ReviewPhoto> reviewPhotos = List.of(reviewPhoto);

        ReviewPhotoResponse reviewPhotoResponse = ReviewPhotoResponse.builder().id(reviewPhoto.getId()).reviewId(reviewPhoto.getReviewId()).fileName(reviewPhoto.getFileName()).fileType(reviewPhoto.getFileName()).fileSize(reviewPhoto.getFileSize()).status(reviewPhoto.getStatus()).build();
        List<ReviewPhotoResponse> reviewPhotoResponses = List.of(reviewPhotoResponse);

        ReviewResponse reviewResponse = ReviewResponse.builder().id(review.getId()).reviewPhotoResponses(reviewPhotoResponses).build();
        MemberResponse memberResponse = MemberResponse.builder().id(memberId).build();
        ShopResponse shopResponse = ShopResponse.builder().id(shopId).build();

        when(reviewReadService.getRegisteredReviewWithThrow(reviewId)).thenReturn(review);
        when(reviewPhotoReadService.getRegisteredReviewPhotos(reviewId)).thenReturn(reviewPhotos);
        when(reviewMapper.toResponse(review, reviewPhotos)).thenReturn(reviewResponse);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);
        when(shopMapper.toResponse(shop, brand)).thenReturn(shopResponse);

        // when
        ReviewDetailResponse response = getReviewUseCase.execute(reviewId);

        // then
        assertNotNull(response);
        assertEquals(reviewResponse.getId(), response.getReviewInfo().getId());
        assertEquals(memberResponse.getId(), response.getMemberInfo().getId());
        assertEquals(shopResponse.getId(), response.getShopInfo().getId());
        assertEquals(reviewPhotoResponses.size(), response.getReviewInfo().getReviewPhotoResponses().size());

        verify(reviewReadService, times(1)).getRegisteredReviewWithThrow(reviewId);
        verify(reviewPhotoReadService, times(1)).getRegisteredReviewPhotos(reviewId);
        verify(reviewMapper, times(1)).toResponse(review, reviewPhotos);
        verify(memberMapper, times(1)).toResponse(member);
        verify(shopMapper, times(1)).toResponse(shop, brand);
    }


    @Test
    @DisplayName("reviewId에 해당하는 리뷰가 존재하며, 리뷰 id를 가진 사진이 존재하지 않는 경우")
    @Transactional(readOnly = true)
    public void execute_WhenReviewFoundWithoutReviewPhotos_ReturnReviewResponseWithEmptyList() {
        // given
        Long reviewId = 1L;
        Long memberId = 1L;
        Long shopId = 1L;
        Long brandId = 1L;

        Member member = Member.builder().id(memberId).build();
        Brand brand = Brand.builder().id(brandId).brandName("브랜드이름").build();
        Shop shop = Shop.builder().id(shopId).brand(brand).build();
        Review review = Review.builder().id(reviewId).member(member).shop(shop).build();

        ReviewResponse reviewResponse = ReviewResponse.builder().id(review.getId()).build();
        MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).build();
        ShopResponse shopResponse = ShopResponse.builder().id(shopId).build();

        when(reviewReadService.getRegisteredReviewWithThrow(reviewId)).thenReturn(review);
        when(reviewPhotoReadService.getRegisteredReviewPhotos(reviewId)).thenReturn(List.of());
        when(reviewMapper.toResponse(review, List.of())).thenReturn(reviewResponse);
        when(memberMapper.toResponse(review.getMember())).thenReturn(memberResponse);
        when(shopMapper.toResponse(review.getShop(), review.getShop().getBrand())).thenReturn(shopResponse);

        // when
        ReviewDetailResponse response = getReviewUseCase.execute(reviewId);

        // then
        assertNotNull(response);
        assertEquals(reviewResponse, response.getReviewInfo());
        assertEquals(memberResponse, response.getMemberInfo());
        assertEquals(shopResponse, response.getShopInfo());

        verify(reviewReadService, times(1)).getRegisteredReviewWithThrow(reviewId);
        verify(reviewPhotoReadService, times(1)).getRegisteredReviewPhotos(reviewId);
        verify(reviewMapper, times(1)).toResponse(review, List.of());
        verify(memberMapper, times(1)).toResponse(review.getMember());
        verify(shopMapper, times(1)).toResponse(review.getShop(), review.getShop().getBrand());
    }

    @Test
    @DisplayName("reviewId가 null 값인 경우 예외 발생")
    public void execute_WhenReviewIdIsNull_ThrowsException() {
        // given

        // when / then
        BusinessException exception = assertThrows(BusinessException.class, () -> getReviewUseCase.execute(null));

        assertEquals(ErrorCode.NO_REQUEST_DATA, exception.getErrorCode());
        verify(reviewReadService, never()).getRegisteredReviewWithThrow(null);
        verify(reviewPhotoReadService, never()).getRegisteredReviewPhotos(null);
        verify(reviewMapper, never()).toResponse(any(Review.class), anyList());
        verify(memberMapper, never()).toResponse(any(Member.class));
        verify(shopMapper, never()).toResponse(any(Shop.class), any(Brand.class));
    }

    @Test
    @DisplayName("reviewId에 해당하는 리뷰가 존재하지 않는 경우 예외 발생")
    @Transactional(readOnly = true)
    public void execute_WhenReviewNotFound_ThrowsException() {
        // given
        Long reviewId = 1L;

        when(reviewReadService.getRegisteredReviewWithThrow(reviewId)).thenThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // when
        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            getReviewUseCase.execute(reviewId);
        });

        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());

        verify(reviewReadService, times(1)).getRegisteredReviewWithThrow(reviewId);
        verify(reviewPhotoReadService, never()).getRegisteredReviewPhotos(anyLong());
        verify(reviewMapper, never()).toResponse(any(), any());
        verify(memberMapper, never()).toResponse(any());
        verify(shopMapper, never()).toResponse(any(), any());
    }

}

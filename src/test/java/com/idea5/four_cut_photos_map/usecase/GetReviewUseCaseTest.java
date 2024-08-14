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
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("특정 id를 가진 리뷰가 존재하며, 사진이 존재하는 경우")
    @Transactional(readOnly = true)
    public void execute_found() {
        // given
        Long reviewId = 1L;
        Member member = Member.builder().build();
        Brand brand = Brand.builder().build();
        Shop shop = Shop.builder().brand(brand).build();
        Review review = Review.builder().id(reviewId).member(member).shop(shop).build();
        List<ReviewPhoto> reviewPhotos = Arrays.asList(new ReviewPhoto(), new ReviewPhoto());

        ReviewResponse reviewResponse = new ReviewResponse();
        MemberResponse memberResponse = new MemberResponse();
        ShopResponse shopResponse = new ShopResponse();

        when(reviewReadService.getReview(reviewId)).thenReturn(Optional.of(review));
        when(reviewPhotoReadService.getReviewPhotos(reviewId)).thenReturn(reviewPhotos);
        when(reviewMapper.toResponse(review, reviewPhotos)).thenReturn(reviewResponse);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);
        when(shopMapper.toResponse(shop, brand)).thenReturn(shopResponse);

        // when
        ReviewDetailResponse response = getReviewUseCase.execute(reviewId);

        // then
        assertNotNull(response);
        assertEquals(reviewResponse, response.getReviewInfo());
        assertEquals(memberResponse, response.getMemberInfo());
        assertEquals(shopResponse, response.getShopInfo());

        verify(reviewReadService, times(1)).getReview(reviewId);
        verify(reviewPhotoReadService, times(1)).getReviewPhotos(reviewId);
        verify(reviewMapper, times(1)).toResponse(review, reviewPhotos);
        verify(memberMapper, times(1)).toResponse(member);
        verify(shopMapper, times(1)).toResponse(shop, brand);
    }


    @Test
    @DisplayName("특정 id를 가진 리뷰가 존재하며, 사진이 존재하지 않는 경우")
    @Transactional(readOnly = true)
    public void execute_reviewPhotosEmpty() {
        // given
        Long reviewId = 1L;
        Member member = Member.builder().build();
        Brand brand = Brand.builder().build();
        Shop shop = Shop.builder().brand(brand).build();
        Review review = Review.builder().id(reviewId).member(member).shop(shop).build();
        List<ReviewPhoto> reviewPhotos = List.of();

        ReviewResponse reviewResponse = new ReviewResponse();
        MemberResponse memberResponse = new MemberResponse();
        ShopResponse shopResponse = new ShopResponse();

        when(reviewReadService.getReview(reviewId)).thenReturn(Optional.of(review));
        when(reviewPhotoReadService.getReviewPhotos(reviewId)).thenReturn(reviewPhotos);
        when(reviewMapper.toResponse(review, reviewPhotos)).thenReturn(reviewResponse);
        when(memberMapper.toResponse(member)).thenReturn(memberResponse);
        when(shopMapper.toResponse(shop, brand)).thenReturn(shopResponse);

        // when
        ReviewDetailResponse response = getReviewUseCase.execute(reviewId);

        // then
        assertNotNull(response);
        assertEquals(reviewResponse, response.getReviewInfo());
        assertEquals(memberResponse, response.getMemberInfo());
        assertEquals(shopResponse, response.getShopInfo());

        verify(reviewReadService, times(1)).getReview(reviewId);
        verify(reviewPhotoReadService, times(1)).getReviewPhotos(reviewId);
        verify(reviewMapper, times(1)).toResponse(review, reviewPhotos);
        verify(memberMapper, times(1)).toResponse(member);
        verify(shopMapper, times(1)).toResponse(shop, brand);
    }

    @Test
    @DisplayName("특정 id를 가진 리뷰가 존재하지 않는 경우")
    @Transactional(readOnly = true)
    public void execute_reviewNotFound() {
        // given
        Long reviewId = 1L;

        when(reviewReadService.getReview(reviewId)).thenReturn(Optional.empty());

        // when
        // then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            getReviewUseCase.execute(reviewId);
        });

        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());

        verify(reviewReadService, times(1)).getReview(reviewId);
        verify(reviewPhotoReadService, never()).getReviewPhotos(anyLong());
        verify(reviewMapper, never()).toResponse(any(), any());
        verify(memberMapper, never()).toResponse(any());
        verify(shopMapper, never()).toResponse(any(), any());
    }

}

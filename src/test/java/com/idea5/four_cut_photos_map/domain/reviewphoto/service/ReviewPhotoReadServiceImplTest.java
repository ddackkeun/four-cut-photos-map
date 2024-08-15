package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.repository.ReviewPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewPhotoReadServiceImplTest {
    @Mock
    private ReviewPhotoRepository reviewPhotoRepository;

    @InjectMocks
    private ReviewPhotoReadServiceImpl reviewPhotoReadService;


    @Nested
    @DisplayName("특정 리뷰의 등록 상태인 사진들을 가져오는 로직")
    class GetRegisteredReviewPhotos {
        private List<ReviewPhoto> reviewPhotos;

        @BeforeEach
        public void setUp() {
            reviewPhotos = List.of(new ReviewPhoto(), new ReviewPhoto());
        }

        @Test
        @DisplayName("특정 review-id를 가진 리뷰 사진이 존재하는 경우")
        public void getRegisteredReviewPhotos_WhenFoundReviewPhoto_ReturnReviewPhotos() {
            // given
            Long reviewId = 1L;
            ReviewPhotoStatus status = ReviewPhotoStatus.REGISTERED;

            when(reviewPhotoRepository.findAllByReviewIdAndStatus(reviewId, status)).thenReturn(reviewPhotos);

            // when
            List<ReviewPhoto> response = reviewPhotoReadService.getRegisteredReviewPhotos(reviewId);

            // then
            assertNotNull(response);
            assertEquals(reviewPhotos.size(), response.size());
            verify(reviewPhotoRepository, times(1)).findAllByReviewIdAndStatus(reviewId, status);
        }

        @Test
        @DisplayName("특정 review-id를 가진 리뷰 사진이 존재하지 않는 경우")
        public void getRegisteredReviewPhotos_WhenExistReviewPhoto_ReturnEmptyList() {
            // given
            Long reviewId = 1L;
            ReviewPhotoStatus status = ReviewPhotoStatus.REGISTERED;

            when(reviewPhotoRepository.findAllByReviewIdAndStatus(reviewId, status)).thenReturn(List.of());

            // when
            List<ReviewPhoto> response = reviewPhotoReadService.getRegisteredReviewPhotos(reviewId);

            // then
            assertNotNull(response);
            assertTrue(response.isEmpty());
            verify(reviewPhotoRepository, times(1)).findAllByReviewIdAndStatus(reviewId, status);
        }
    }
}

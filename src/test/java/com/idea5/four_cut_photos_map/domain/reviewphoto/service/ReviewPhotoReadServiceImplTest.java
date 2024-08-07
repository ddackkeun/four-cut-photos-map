package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
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
    @DisplayName("특정 리뷰의 사진을 가져오는 로직")
    class GetReviewPhotos {
        private List<ReviewPhoto> reviewPhotos;

        @BeforeEach
        public void setUp() {
            reviewPhotos = List.of(new ReviewPhoto(), new ReviewPhoto());
        }

        @Test
        @DisplayName("특정 review-id를 가진 리뷰 사진이 존재하는 경우")
        public void getReviewPhotos_found() {
            // given
            Long reviewId = 1L;

            // when
            when(reviewPhotoRepository.findAllByReviewId(reviewId)).thenReturn(reviewPhotos);

            List<ReviewPhoto> response = reviewPhotoReadService.getReviewPhotos(reviewId);

            // then
            assertNotNull(response);
            assertEquals(reviewPhotos.size(), response.size());
            verify(reviewPhotoRepository, times(1)).findAllByReviewId(reviewId);
        }

        @Test
        @DisplayName("특정 review-id를 가진 리뷰 사진이 존재하지 않는 경우")
        public void getReviewPhotos_Empty() {
            // given
            Long reviewId = 1L;

            // when
            when(reviewPhotoRepository.findAllByReviewId(reviewId)).thenReturn(List.of());

            List<ReviewPhoto> response = reviewPhotoReadService.getReviewPhotos(reviewId);

            // then
            assertNotNull(response);
            assertTrue(response.isEmpty());
            verify(reviewPhotoRepository, times(1)).findAllByReviewId(reviewId);
        }
    }
}

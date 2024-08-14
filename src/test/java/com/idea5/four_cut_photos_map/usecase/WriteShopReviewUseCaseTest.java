package com.idea5.four_cut_photos_map.usecase;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.service.MemberReadServiceImpl;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestServiceImpl;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.mapper.ReviewPhotoMapper;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.ReviewPhotoRequestServiceImpl;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.S3Service;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopReadServiceImpl;
import com.idea5.four_cut_photos_map.domain.shop.service.ShopRequestServiceImpl;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WriteShopReviewUseCaseTest {
    @Mock
    private ShopReadServiceImpl shopReadService;

    @Mock
    private MemberReadServiceImpl memberReadService;

    @Mock
    private ReviewRequestServiceImpl reviewRequestService;

    @Mock
    private ReviewPhotoRequestServiceImpl reviewPhotoRequestService;

    @Mock
    private ShopRequestServiceImpl shopRequestService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewPhotoMapper reviewPhotoMapper;

    @InjectMocks
    private WriteShopReviewUseCase writeShopReviewUseCase;

    @Test
    @DisplayName("리뷰 요청이 존재하지 않을 때 예외가 발생")
    void execute_noReviewRequest_throwsException() {
        // given
        long shopId = 1L;
        long memberId = 1L;
        ReviewRequest reviewRequest = null;
        List<MultipartFile> files = List.of();

        // when / then
        assertThrows(BusinessException.class, () -> writeShopReviewUseCase.execute(shopId, memberId, reviewRequest, files));
    }

    @Test
    @DisplayName("request, files 사용하여 Review, ReviewPhoto 생성 후 상점 리뷰 정보 갱신")
    void execute_withValidReviewRequest_success() {
        // given
        long shopId = 1L;
        long memberId = 1L;
        long reviewId = 1L;
        long reviewPhotoId = 1L;

        Shop shop = Shop.builder().id(shopId).brand(Brand.builder().brandName("TestBrand").build()).build();
        Member member = Member.builder().id(memberId).build();
        ReviewRequest reviewRequest = ReviewRequest.builder().starRating(5).content("Nice place!").purity("GOOD").build();
        Review review = Review.builder().member(member).shop(shop).starRating(reviewRequest.getStarRating()).content(reviewRequest.getContent()).purity(PurityScore.GOOD).retouch(RetouchScore.UNSELECTED).item(ItemScore.UNSELECTED).build();
        Review savedReview = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(review.getStarRating()).content(review.getContent()).status(ReviewStatus.REGISTERED).purity(review.getPurity()).retouch(review.getRetouch()).item(review.getItem()).build();

        MockMultipartFile validFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", "SomeImageContent".getBytes());
        List<MultipartFile> files = List.of(validFile);
        ImageUploadResponse imageUploadResponse = new ImageUploadResponse("image.jpg", "https://test-url.com/image.jpg", "image/jpeg", validFile.getSize(), true, null);

        ReviewPhoto reviewPhoto = ReviewPhoto.builder().reviewId(reviewId).shopId(shopId).fileName(imageUploadResponse.getFileName()).filePath(imageUploadResponse.getUrl()).fileType(imageUploadResponse.getType()).fileSize(imageUploadResponse.getSize()).build();
        ReviewPhoto savedReviewPhoto = ReviewPhoto.builder().id(reviewPhotoId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).reviewId(reviewPhoto.getReviewId()).shopId(reviewPhoto.getShopId()).fileName(reviewPhoto.getFileName()).filePath(reviewPhoto.getFilePath()).fileType(reviewPhoto.getFileType()).fileSize(reviewPhoto.getFileSize()).status(ReviewPhotoStatus.REGISTERED).build();

        // when
        when(shopReadService.getShopWithThrow(shopId)).thenReturn(shop);
        when(memberReadService.getMemberWithThrow(memberId)).thenReturn(member);
        when(reviewMapper.toEntity(shop, member, reviewRequest)).thenReturn(review);
        when(reviewRequestService.writeReview(review)).thenReturn(savedReview);

        when(s3Service.uploadImages(eq("TestBrand"), anyList())).thenReturn(List.of(imageUploadResponse));
        when(reviewPhotoMapper.toEntity(eq(reviewId), eq(shopId), any(ImageUploadResponse.class))).thenReturn(reviewPhoto);
        when(reviewPhotoRequestService.createReviewPhoto(reviewPhoto)).thenReturn(savedReviewPhoto);

        // then
        writeShopReviewUseCase.execute(shopId, memberId, reviewRequest, files);

        // Verify interactions
        verify(shopReadService).getShopWithThrow(shopId);
        verify(memberReadService).getMemberWithThrow(memberId);
        verify(reviewMapper).toEntity(shop, member, reviewRequest);
        verify(reviewRequestService).writeReview(review);
        verify(s3Service).uploadImages(eq("TestBrand"), anyList());
        verify(reviewPhotoMapper).toEntity(eq(reviewId), eq(shopId), any(ImageUploadResponse.class));
        verify(reviewPhotoRequestService).createReviewPhoto(reviewPhoto);
        verify(shopRequestService).updateReviewInfo(shop);
    }
}

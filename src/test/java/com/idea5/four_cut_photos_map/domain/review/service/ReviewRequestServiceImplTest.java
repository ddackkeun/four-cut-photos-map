package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReviewRequestServiceImplTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReviewRequestServiceImpl requestReviewServiceImpl;


    @Nested
    @DisplayName("상점 리뷰 작성")
    class WriteReview {
        private Shop shop;
        private Member member;

        @BeforeEach
        void setUp() {
            shop = Shop.builder().id(1L).build();
            member = Member.builder().id(1L).status(MemberStatus.REGISTERED).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("요청 데이터에 리뷰, 리뷰 사진 데이터 있을 때 shopId의 지점에 리뷰, 리뷰 사진 생성")
            void writeReview_Success1() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                List<String> imageUrls = List.of("image1.jpg", "image2.jpg", "image3.jpg");
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").imageUrls(imageUrls).build();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));

                // when
                requestReviewServiceImpl.writeReview(shopId, memberId, request);

                // then
                ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
                verify(shopRepository).findById(shopId);
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).save(reviewCaptor.capture());

                Review captureReview = reviewCaptor.getValue();
                assertEquals(request.getStarRating(), captureReview.getStarRating());
                assertEquals(request.getContent(), captureReview.getContent());
                assertEquals(shop, captureReview.getShop());
                assertEquals(member, captureReview.getMember());
                assertEquals(imageUrls.size(), captureReview.getPhotos().size());
                assertEquals(imageUrls.get(0), captureReview.getPhotos().get(0).getUrl());
                assertEquals(imageUrls.get(1), captureReview.getPhotos().get(1).getUrl());
                assertEquals(imageUrls.get(2), captureReview.getPhotos().get(2).getUrl());
            }

            @Test
            @DisplayName("요청 데이터에 리뷰 데이터만 있을 때 shopId의 지점에 리뷰 생성")
            void writeReview_Success2() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));

                // when
                requestReviewServiceImpl.writeReview(shopId, memberId, request);

                // then
                ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
                verify(shopRepository).findById(shopId);
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).save(reviewCaptor.capture());

                Review captureReview = reviewCaptor.getValue();
                assertEquals(request.getStarRating(), captureReview.getStarRating());
                assertEquals(request.getContent(), captureReview.getContent());
                assertEquals(shop, captureReview.getShop());
                assertEquals(member, captureReview.getMember());
                assertTrue(captureReview.getPhotos().isEmpty());
            }

            @Test
            @DisplayName("요청 데이터의 purity, retouch, item 값이 null일 때 shopId의 지점에 리뷰 생성")
            void writeReview_success3() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity(null).retouch(null).item(null).build();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));

                // when
                requestReviewServiceImpl.writeReview(shopId, memberId, request);

                // then
                ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
                verify(shopRepository).findById(shopId);
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).save(reviewCaptor.capture());

                Review captureReview = reviewCaptor.getValue();
                assertNull(captureReview.getPurity());
                assertNull(captureReview.getRetouch());
                assertNull(captureReview.getItem());
            }
        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("shopId의 지점이 존재하지 않을 때 예외 발생")
            void writeReview_fail1() {
                // given
                Long shopId = 99L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();

                given(shopRepository.findById(shopId)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.writeReview(shopId, memberId, request));

                assertEquals(ErrorCode.SHOP_NOT_FOUND, result.getErrorCode());
                verify(shopRepository).findById(shopId);
                verify(memberRepository, never()).findByIdAndStatus(anyLong(), any());
                verify(reviewRepository, never()).save(any(Review.class));
            }

            @Test
            @DisplayName("memberId의 회원 존재하지 않는 경우 예외 발생")
            void writeReview_fail2() {
                // given
                Long shopId = 1L;
                Long memberId = 99L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.writeReview(shopId, memberId, request));
                assertEquals(ErrorCode.MEMBER_NOT_FOUND, result.getErrorCode());

                verify(shopRepository).findById(shopId);
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository, never()).save(any(Review.class));
            }

            @Test
            @DisplayName("요청 데이터(ReviewRequest) 존재하지 않을 때 예외 발생")
            public void writeReview_fail3() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));

                // when & then
                assertThrows(NullPointerException.class, () -> requestReviewServiceImpl.writeReview(shopId, memberId, null));
            }
        }
    }


    @Nested
    @DisplayName("특정 리뷰 수정")
    class ModifyReview {
        private Shop shop;
        private Member member;
        private ReviewPhoto reviewPhoto1, reviewPhoto2, reviewPhoto3;

        @BeforeEach
        void setUp() {
            shop = Shop.builder().id(1L).build();
            member = Member.builder().id(1L).status(MemberStatus.REGISTERED).build();
            reviewPhoto1 = ReviewPhoto.builder().id(1L).url("image1.jpg").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto2 = ReviewPhoto.builder().id(2L).url("image2.jpg").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto3 = ReviewPhoto.builder().id(3L).url("image3.jpg").status(ReviewPhotoStatus.REGISTERED).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("요청 데이터에 리뷰 내용에만 변동이 있을 때 reviewId를 가진 리뷰 내용 수정")
            void modifyReview_Success1() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(5).content("새로운 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();
                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                assertEquals(review.getShop().getId(), result);
                assertEquals(request.getStarRating(), review.getStarRating());
                assertEquals(request.getContent(), review.getContent());
                assertEquals(request.getPurity(), review.getPurity().toString());
                assertEquals(request.getRetouch(), review.getRetouch().toString());
                assertEquals(request.getItem(), review.getItem().toString());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰 사진 내용만 변동(추가, 삭제)이 있을 때 reviewId를 가진 리뷰의 사진 내용 수정")
            void modifyReview_Success2() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                List<String> requestImageUrls = List.of("image1.jpg", "image4.jpg");
                ReviewRequest request = ReviewRequest.builder().starRating(1).content("기존 리뷰 내용").imageUrls(requestImageUrls).build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(requestImageUrls.size(), reviewPhotos.size());
                assertEquals(requestImageUrls.get(0), reviewPhotos.get(0).getUrl());
                assertEquals(requestImageUrls.get(1), reviewPhotos.get(1).getUrl());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰 사진 내용만 변동(추가)이 있을 때 reviewId를 가진 리뷰의 사진 내용 수정")
            void modifyReview_Success3() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                List<String> requestImageUrls = List.of("image1.jpg", "image3.jpg", "image4.jpg");
                ReviewRequest request = ReviewRequest.builder().starRating(1).content("기존 리뷰 내용").imageUrls(requestImageUrls).build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(requestImageUrls.size(), reviewPhotos.size());
                assertEquals(requestImageUrls.get(0), reviewPhotos.get(0).getUrl());
                assertEquals(requestImageUrls.get(1), reviewPhotos.get(1).getUrl());
                assertEquals(requestImageUrls.get(2), reviewPhotos.get(2).getUrl());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰 사진 내용만 변동(삭제)이 있을 때 reviewId를 가진 리뷰의 사진 내용 수정")
            void modifyReview_Success4() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(1).content("기존 리뷰 내용").imageUrls(null).build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(0, reviewPhotos.size());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰, 리뷰 사진 변동(추가, 삭제)이 있을 때 리뷰, 리뷰 사진 수정")
            void modifyReview_Success5() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                List<String> requestImageUrls = List.of("image1.jpg", "image4.jpg");
                ReviewRequest request = ReviewRequest.builder().starRating(5).content("새로운 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").imageUrls(requestImageUrls).build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(request.getStarRating(), review.getStarRating());
                assertEquals(request.getContent(), review.getContent());
                assertEquals(request.getPurity(), review.getPurity().toString());
                assertEquals(request.getRetouch(), review.getRetouch().toString());
                assertEquals(request.getItem(), review.getItem().toString());
                assertEquals(requestImageUrls.size(), reviewPhotos.size());
                assertEquals(requestImageUrls.get(0), reviewPhotos.get(0).getUrl());
                assertEquals(requestImageUrls.get(1), reviewPhotos.get(1).getUrl());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰, 리뷰 사진 변동(추가)이 있을 때 리뷰, 리뷰 사진 수정")
            void modifyReview_Success6() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                List<String> requestImageUrls = List.of("image1.jpg", "image3.jpg", "image4.jpg");
                ReviewRequest request = ReviewRequest.builder().starRating(5).content("새로운 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").imageUrls(requestImageUrls).build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(request.getStarRating(), review.getStarRating());
                assertEquals(request.getContent(), review.getContent());
                assertEquals(request.getPurity(), review.getPurity().toString());
                assertEquals(request.getRetouch(), review.getRetouch().toString());
                assertEquals(request.getItem(), review.getItem().toString());
                assertEquals(requestImageUrls.size(), reviewPhotos.size());
                assertEquals(requestImageUrls.get(0), reviewPhotos.get(0).getUrl());
                assertEquals(requestImageUrls.get(1), reviewPhotos.get(1).getUrl());
                assertEquals(requestImageUrls.get(2), reviewPhotos.get(2).getUrl());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청 데이터에 리뷰, 리뷰 사진 변동(삭제)이 있을 때 리뷰, 리뷰 사진 수정")
            void modifyReview_Success7() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(5).content("새로운 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();

                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                List<ReviewPhoto> reviewPhotos = review.getPhotos().stream()
                        .filter(reviewPhoto -> reviewPhoto.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                        .toList();

                assertEquals(review.getShop().getId(), result);
                assertEquals(request.getStarRating(), review.getStarRating());
                assertEquals(request.getContent(), review.getContent());
                assertEquals(request.getPurity(), review.getPurity().toString());
                assertEquals(request.getRetouch(), review.getRetouch().toString());
                assertEquals(request.getItem(), review.getItem().toString());
                assertEquals(0, reviewPhotos.size());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

        }
        
        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("reviewId 가진 리뷰가 존재하지 않을 때 예외 발생")
            void modifyReview_Fail1() {
                // given
                Long memberId = 1L;
                Long reviewId = 99L;

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException result = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.modifyReview(memberId, reviewId, new ReviewRequest()));

                Assertions.assertEquals(ErrorCode.REVIEW_NOT_FOUND, result.getErrorCode());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("사용자와 리뷰 작성자 일치하지 않을 때 예외 발생")
            void modifyReview_Fail2() {
                // given
                Long memberId = 99L;
                Long reviewId = 1L;
                Review review = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when & then
                BusinessException result = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.modifyReview(memberId, reviewId, new ReviewRequest()));

                Assertions.assertEquals(ErrorCode.WRITER_DOES_NOT_MATCH, result.getErrorCode());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("요청데이터(ReviewRequest)가 null 일때 예외 발생")
            void modifyReview_Fail3() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                Review review = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when & then
                assertThrows(NullPointerException.class,
                        () -> requestReviewServiceImpl.modifyReview(memberId, reviewId, null));

                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }
        }
    }

    @Nested
    @DisplayName("특정 리뷰 삭제")
    class DeleteReview {
        private Shop shop;
        private Member member;
        private ReviewPhoto reviewPhoto1, reviewPhoto2, reviewPhoto3;

        @BeforeEach
        void setUp() {
            shop = Shop.builder().id(1L).build();
            member = Member.builder().id(1L).status(MemberStatus.REGISTERED).build();
            reviewPhoto1 = ReviewPhoto.builder().id(1L).url("image1.jpg").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto2 = ReviewPhoto.builder().id(2L).url("image2.jpg").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto3 = ReviewPhoto.builder().id(3L).url("image3.jpg").status(ReviewPhotoStatus.REGISTERED).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("리뷰 사진을 가지고 있는 리뷰가 있을 때 리뷰, 리뷰 사진 삭제")
            void deleteReview_Success1() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                Review review = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.deleteReview(memberId, reviewId);

                // then
                assertEquals(review.getShop().getId(), result);
                assertEquals(ReviewStatus.DELETED, review.getStatus());
                review.getPhotos().forEach(reviewPhoto ->
                        assertEquals(ReviewPhotoStatus.DELETED, reviewPhoto.getStatus()));
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("리뷰 사진이 없는 리뷰가 있을 때 리뷰 삭제")
            void deleteReview_Success2() {
                // given
                Long memberId = 1L;
                Long reviewId = 1L;
                Review review = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                Long result = requestReviewServiceImpl.deleteReview(memberId, reviewId);

                // then
                assertEquals(review.getShop().getId(), result);
                assertEquals(ReviewStatus.DELETED, review.getStatus());
                assertTrue(review.getPhotos().isEmpty());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("reviewId 가진 리뷰 존재하지 않을 때 예외 발생")
            void modifyReview_Fail1() {
                // given
                Long memberId = 1L;
                Long reviewId = 99L;

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.deleteReview(memberId, reviewId));

                assertEquals(ErrorCode.REVIEW_NOT_FOUND, result.getErrorCode());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("리뷰 작성자와 사용자가 일치하지 않을 때 예외 발생")
            void modifyReview_Fail2() {
                // given
                Long memberId = 99L;
                Long reviewId = 1L;
                Review review = Review.builder().id(reviewId).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(1).content("기존 리뷰 내용").status(ReviewStatus.REGISTERED).build();

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when & then
                BusinessException result = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.deleteReview(memberId, reviewId));

                Assertions.assertEquals(ErrorCode.WRITER_DOES_NOT_MATCH, result.getErrorCode());
                verify(reviewRepository).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

        }
    }
}

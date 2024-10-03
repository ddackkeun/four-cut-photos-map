package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewDetailResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ReviewPhotoResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.CursorRequest;
import com.idea5.four_cut_photos_map.global.util.CursorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewReadServiceImplTest {
    @InjectMocks
    private ReviewReadServiceImpl reviewReadServiceImpl;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("특정 리뷰의 상세 정보 가져옴")
    class GetReview {
        private Member member;
        private Shop shop;
        private ReviewPhoto reviewPhoto1, reviewPhoto2, reviewPhoto3, reviewPhoto4, reviewPhoto5;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).nickname("nickname").status(MemberStatus.REGISTERED).build();
            shop = Shop.builder().id(1L).placeName("지점명").address("도로명주소").build();
            reviewPhoto1 = ReviewPhoto.builder().id(1L).url("link1").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto2 = ReviewPhoto.builder().id(2L).url("link2").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto3 = ReviewPhoto.builder().id(3L).url("link3").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto4 = ReviewPhoto.builder().id(4L).url("link4").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto5 = ReviewPhoto.builder().id(5L).url("link5").status(ReviewPhotoStatus.DELETED).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("해당 id를 가진 리뷰 존재하며, 리뷰의 회원, 상점, 리뷰 사진이 존재할 경우")
            void getReview_Success1() {
                //given
                Long reviewId = 1L;
                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(5).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto1);
                review.addPhoto(reviewPhoto2);
                review.addPhoto(reviewPhoto3);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                ReviewDetailResponse result = reviewReadServiceImpl.getReview(reviewId);

                //then
                assertNotNull(result);
                assertEquals(reviewId, result.getReview().getId());
                assertEquals(member.getId(), result.getMember().getId());
                assertEquals(shop.getId(), result.getShop().getId());
                assertEquals(2, result.getPhotos().size());
                assertEquals(reviewPhoto1.getId(), result.getPhotos().get(0).getId());
                assertEquals(reviewPhoto3.getId(), result.getPhotos().get(1).getId());

                verify(reviewRepository, times(1)).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }

            @Test
            @DisplayName("해당 id를 가진 리뷰 존재하며, 리뷰의 상점, 회원이 존재하고 등록 상태인 리뷰 사진이 존재하지 않을 경우")
            void getReview_Success2() {
                // given
                Long reviewId = 1L;
                Review review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(5).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                review.addPhoto(reviewPhoto4);
                review.addPhoto(reviewPhoto5);

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.of(review));

                // when
                ReviewDetailResponse result = reviewReadServiceImpl.getReview(reviewId);

                // then
                assertNotNull(result);
                assertEquals(reviewId, result.getReview().getId());
                assertEquals(member.getId(), result.getMember().getId());
                assertEquals(shop.getId(), result.getShop().getId());
                assertTrue(result.getPhotos().isEmpty());

                verify(reviewRepository, times(1)).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }
        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("해당 shopId의 리뷰가 존재하지 않을 때 예외 발생")
            void getReview_WhenNotFoundReview_ThrowsException() {
                // given
                Long reviewId = 1L;

                given(reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> reviewReadServiceImpl.getReview(reviewId));

                assertEquals(ErrorCode.REVIEW_NOT_FOUND, result.getErrorCode());
                verify(reviewRepository, times(1)).findByIdAndStatus(reviewId, ReviewStatus.REGISTERED);
            }
        }
    }

    @Nested
    @DisplayName("커서 기반 페이징 방식으로 회원의 리뷰 조회")
    class GetMemberReviews {
        private Shop shop1, shop2;
        private Member member;
        private ReviewPhoto reviewPhoto1, reviewPhoto2, reviewPhoto3, reviewPhoto4, reviewPhoto5;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).nickname("nickname").build();

            shop1 = Shop.builder().id(1L).placeName("1번 지점명").address("1번 지점 도로명주소").build();
            shop2 = Shop.builder().id(2L).placeName("2번 지점명").address("2번 지점 도로명주소").build();

            reviewPhoto1 = ReviewPhoto.builder().id(1L).url("link1").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto2 = ReviewPhoto.builder().id(2L).url("link2").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto3 = ReviewPhoto.builder().id(3L).url("link3").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto4 = ReviewPhoto.builder().id(4L).url("link4").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto5 = ReviewPhoto.builder().id(5L).url("link5").status(ReviewPhotoStatus.DELETED).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("조건에 맞는 회원의 리뷰가 있고 리뷰의 사진이 있을 때 MemberReviewResponse 반환")
            void getMemberReviews_Success1() {
                // given
                Long memberId = 1L;
                Long lastReviewId = 3L;
                int size = 5;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop1).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop2).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> memberReviews = List.of(review2, review1);
                review1.addPhoto(reviewPhoto1);
                review1.addPhoto(reviewPhoto2);
                review2.addPhoto(reviewPhoto3);
                review2.addPhoto(reviewPhoto4);
                review2.addPhoto(reviewPhoto5);

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(reviewRepository.findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size))).willReturn(memberReviews);

                // when
                CursorResponse<MemberReviewResponse> result = reviewReadServiceImpl.getMemberReviews(memberId, cursorRequest);

                // then
                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());

                assertEquals(memberReviews.size(), result.getBody().size());
                assertTrue(size >= result.getBody().size());
                IntStream.range(0, result.getBody().size())
                        .forEach(i -> {
                            MemberReviewResponse response = result.getBody().get(i);
                            Review review = memberReviews.get(i);

                            List<Long> responsePhotoIds = response.getPhotos().stream()
                                    .map(ReviewPhotoResponse::getId)
                                    .toList();
                            List<Long> reviewPhotoIds = review.getPhotos().stream()
                                    .filter(photos -> photos.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                                    .map(BaseEntity::getId)
                                    .toList();

                            assertTrue(lastReviewId > response.getReview().getId());
                            assertEquals(review.getId(), response.getReview().getId());
                            assertEquals(review.getShop().getId(), response.getShop().getId());
                            assertEquals(reviewPhotoIds.size(), responsePhotoIds.size());
                            assertEquals(reviewPhotoIds, responsePhotoIds);
                        });

                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size));
            }

            @Test
            @DisplayName("조건에 맞는 회원의 리뷰가 있고 리뷰 사진이 없을 때 MemberReviewResponse 반환")
            void getMemberReviews_Success2() {
                // given
                Long memberId = 1L;
                Long lastReviewId = 3L;
                int size = 5;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop1).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop2).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> memberReviews = List.of(review2, review1);

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(reviewRepository.findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size))).willReturn(memberReviews);

                // when
                CursorResponse<MemberReviewResponse> result = reviewReadServiceImpl.getMemberReviews(memberId, cursorRequest);

                // then
                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());

                assertTrue(size >= result.getBody().size());
                assertEquals(memberReviews.size(), result.getBody().size());
                IntStream.range(0, result.getBody().size())
                        .forEach(i -> {
                            MemberReviewResponse response = result.getBody().get(i);
                            Review review = memberReviews.get(i);

                            assertTrue(lastReviewId > response.getReview().getId());
                            assertEquals(review.getId(), response.getReview().getId());
                            assertEquals(review.getShop().getId(), response.getShop().getId());
                            assertTrue(response.getPhotos().isEmpty());
                        });

                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size));
            }

            @Test
            @DisplayName("조건에 맞는 회원의 리뷰가 존재하지 않는 경우 빈 리스트 반환")
            void getMemberReviews_Success3() {
                // given
                Long memberId = 1L;
                long lastReviewId = 5L;
                int size = 5;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(reviewRepository.findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size))).willReturn(Collections.emptyList());

                // when
                CursorResponse<MemberReviewResponse> result = reviewReadServiceImpl.getMemberReviews(memberId, cursorRequest);

                // then
                assertEquals(CursorRequest.NONE_KEY, result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());
                assertTrue(result.getBody().isEmpty());
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(member, ReviewStatus.REGISTERED, lastReviewId, PageRequest.of(0, size));
            }

            @Test
            @DisplayName("CursorRequest 정보가 null 일 때 기본 값을 이용한 회원 리뷰 응답 리스트 반환")
            void getMemberReviews_Success5() {
                // given
                Long memberId = 1L;
                CursorRequest cursorRequest = CursorRequest.of(null, null);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop1).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop2).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> memberReviews = List.of(review2, review1);

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(reviewRepository.findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(eq(member), eq(ReviewStatus.REGISTERED), anyLong(), any(PageRequest.class))).willReturn(memberReviews);

                // when
                CursorResponse<MemberReviewResponse> result = reviewReadServiceImpl.getMemberReviews(memberId, cursorRequest);

                // then
                ArgumentCaptor<Long> keyCaptor = ArgumentCaptor.forClass(Long.class);
                ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository).findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(eq(member), eq(ReviewStatus.REGISTERED), keyCaptor.capture(), pageRequestCaptor.capture());

                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(10, result.getNextCursorRequest().getSize());
                assertEquals(Long.MAX_VALUE, keyCaptor.getValue());
                assertEquals(0, pageRequestCaptor.getValue().getPageNumber());
                assertEquals(10, pageRequestCaptor.getValue().getPageSize());

                assertTrue(10 >= result.getBody().size());
                assertEquals(memberReviews.size(), result.getBody().size());
                IntStream.range(0, result.getBody().size())
                        .forEach(i -> {
                            MemberReviewResponse response = result.getBody().get(i);
                            Review review = memberReviews.get(i);

                            assertTrue(Long.MAX_VALUE > response.getReview().getId());
                            assertEquals(review.getId(), response.getReview().getId());
                            assertEquals(review.getShop().getId(), response.getShop().getId());
                            assertTrue(response.getPhotos().isEmpty());
                        });
            }
        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("memberId 해당하는 회원이 없을 때 예외 발생")
            void getMemberReviews_Fail1() {
                // given
                Long memberId = 99L;
                CursorRequest cursorRequest = CursorRequest.of(5L, 5);

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> reviewReadServiceImpl.getMemberReviews(memberId, cursorRequest));

                assertEquals(ErrorCode.MEMBER_NOT_FOUND, result.getErrorCode());
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(reviewRepository, never()).findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(any(), any(), anyLong(), any());
            }
        }

    }

    @Nested
    @DisplayName("커서 기반 페이징 방식으로 지점의 리뷰 조회")
    class GetShopReviews {
        private Shop shop;
        private Member member1, member2;
        private ReviewPhoto reviewPhoto1, reviewPhoto2, reviewPhoto3, reviewPhoto4, reviewPhoto5;

        @BeforeEach
        void setUp() {
            shop = Shop.builder().id(1L).placeName("지점명").address("도로명주소").build();

            member1 = Member.builder().id(1L).nickname("회원1 닉네임").build();
            member2 = Member.builder().id(2L).nickname("회원2 닉네임").build();

            reviewPhoto1 = ReviewPhoto.builder().id(1L).url("link1").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto2 = ReviewPhoto.builder().id(2L).url("link2").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto3 = ReviewPhoto.builder().id(3L).url("link3").status(ReviewPhotoStatus.REGISTERED).build();
            reviewPhoto4 = ReviewPhoto.builder().id(4L).url("link4").status(ReviewPhotoStatus.DELETED).build();
            reviewPhoto5 = ReviewPhoto.builder().id(5L).url("link5").status(ReviewPhotoStatus.DELETED).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("조건에 맞는 지점의 리뷰가 있고 리뷰 사진이 있을 때 MemberReviewResponse 반환")
            void getShopReviews_Success1() {
                // given
                Long shopId = 1L;
                Long lastReviewId = 5L;
                int size = 10;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);
                PageRequest pageRequest = PageRequest.of(0, size);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member1).shop(shop).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member2).shop(shop).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> shopReviews = List.of(review2, review1);
                review1.addPhoto(reviewPhoto1);
                review1.addPhoto(reviewPhoto2);
                review2.addPhoto(reviewPhoto3);
                review2.addPhoto(reviewPhoto4);
                review2.addPhoto(reviewPhoto5);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest)).willReturn(shopReviews);

                // when
                CursorResponse<ShopReviewResponse> result = reviewReadServiceImpl.getShopReviews(shopId, cursorRequest);

                // then
                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());

                assertTrue(result.getBody().size() <= size);
                assertEquals(shopReviews.size(), result.getBody().size());
                IntStream.range(0, result.getBody().size())
                        .forEach(i -> {
                            ShopReviewResponse response = result.getBody().get(i);
                            Review review = shopReviews.get(i);
                            List<Long> reviewPhotoIds = review.getPhotos().stream()
                                    .filter(photo -> photo.getStatus().equals(ReviewPhotoStatus.REGISTERED))
                                    .map(BaseEntity::getId)
                                    .toList();
                            List<Long> responsePhotoIds = response.getPhotos().stream()
                                    .map(ReviewPhotoResponse::getId)
                                    .toList();

                            assertTrue(response.getReview().getId() < lastReviewId);
                            assertEquals(review.getId(), response.getReview().getId());
                            assertEquals(review.getMember().getId(), response.getMember().getId());
                            assertEquals(reviewPhotoIds.size(), responsePhotoIds.size());
                            assertEquals(reviewPhotoIds, responsePhotoIds);
                        });

                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest);
            }

            @Test
            @DisplayName("조건에 맞는 지점의 리뷰가 있고 리뷰 사진이 없을 때 MemberReviewResponse 반환")
            void getShopReviews_Success2() {
                // given
                Long shopId = 1L;
                Long lastReviewId = 5L;
                int size = 5;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);
                PageRequest pageRequest = PageRequest.of(0, size);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member1).shop(shop).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member2).shop(shop).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> shopReviews = List.of(review2, review1);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest)).willReturn(shopReviews);

                // when
                CursorResponse<ShopReviewResponse> result = reviewReadServiceImpl.getShopReviews(shopId, cursorRequest);

                // then
                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());

                assertTrue(result.getBody().size() <= size);
                assertEquals(shopReviews.size(), result.getBody().size());
                IntStream.range(0, result.getBody().size())
                        .forEach(i -> {
                            ShopReviewResponse response = result.getBody().get(i);
                            Review review = shopReviews.get(i);

                            assertTrue(response.getReview().getId() < lastReviewId);
                            assertEquals(review.getId(), response.getReview().getId());
                            assertEquals(review.getMember().getId(), response.getMember().getId());
                            assertTrue(response.getPhotos().isEmpty());
                        });

                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest);
            }

            @Test
            @DisplayName("조건에 맞는 지점의 리뷰가 없을 때 빈 리스트 반환")
            void getShopReviews_Success3() {
                // given
                Long shopId = 1L;
                long lastReviewId = 5L;
                int size = 5;
                CursorRequest cursorRequest = CursorRequest.of(lastReviewId, size);
                PageRequest pageRequest = PageRequest.of(0, size);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest)).willReturn(Collections.emptyList());

                // when
                CursorResponse<ShopReviewResponse> result = reviewReadServiceImpl.getShopReviews(shopId, cursorRequest);

                // then
                assertEquals(CursorRequest.NONE_KEY, result.getNextCursorRequest().getKey());
                assertEquals(size, result.getNextCursorRequest().getSize());
                assertTrue(result.getBody().isEmpty());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopAndStatusAndIdLessThanOrderByIdDesc(shop, ReviewStatus.REGISTERED, lastReviewId, pageRequest);
            }

            @Test
            @DisplayName("CursorRequest null 일 때 기본 값 응답")
            void getShopReviews_Success4() {
                // given
                Long shopId = 1L;
                CursorRequest cursorRequest = CursorRequest.of(null, null);

                Review review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member1).shop(shop).starRating(1).content("리뷰 내용1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).status(ReviewStatus.REGISTERED).build();
                Review review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member2).shop(shop).starRating(2).content("리뷰 내용2").purity(PurityScore.GOOD).status(ReviewStatus.REGISTERED).build();
                List<Review> shopReviews = List.of(review2, review1);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopAndStatusAndIdLessThanOrderByIdDesc(eq(shop), eq(ReviewStatus.REGISTERED), anyLong(), any(PageRequest.class))).willReturn(shopReviews);

                // when
                CursorResponse<ShopReviewResponse> result = reviewReadServiceImpl.getShopReviews(shopId, cursorRequest);

                // then
                assertEquals(review1.getId(), result.getNextCursorRequest().getKey());
                assertEquals(10, result.getNextCursorRequest().getSize());

                ArgumentCaptor<Long> keyCaptor = ArgumentCaptor.forClass(Long.class);
                ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopAndStatusAndIdLessThanOrderByIdDesc(eq(shop), eq(ReviewStatus.REGISTERED), keyCaptor.capture(), pageRequestCaptor.capture());

                assertEquals(Long.MAX_VALUE, keyCaptor.getValue());
                assertEquals(0, pageRequestCaptor.getValue().getPageNumber());
                assertEquals(10, pageRequestCaptor.getValue().getPageSize());
            }

        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("shopId 해당하는 지점이 없을 때 예외 발생")
            void getShopReviews_Fail1() {
                // given
                Long shopId = 99L;
                Long lastReviewId = 5L;
                int size = 10;
                CursorRequest cursorRequest = new CursorRequest(lastReviewId, size);

                given(shopRepository.findById(shopId)).willReturn(Optional.empty());

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> reviewReadServiceImpl.getShopReviews(shopId, cursorRequest));
                assertEquals(ErrorCode.SHOP_NOT_FOUND, result.getErrorCode());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository, never()).findAllByShopAndStatusAndIdLessThanOrderByIdDesc(any(Shop.class), any(), anyLong(), any());
            }
        }
    }
}
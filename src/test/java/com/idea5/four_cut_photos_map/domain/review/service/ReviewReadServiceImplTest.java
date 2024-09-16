/*
package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.mapper.ShopMapper;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

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
    private ReviewMapper reviewMapper;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private ShopMapper shopMapper;

    @Nested
    @DisplayName("단일 리뷰 검색")
    class GetRegisteredReviewWithThrow {
        private Member member;
        private Brand brand;
        private Shop shop;
        private Review review;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            brand = Brand.builder().id(1L).brandName("인생네컷").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("장소이름").address("상세주소").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(5).content("리뷰 내용").status(ReviewStatus.REGISTERED).purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("해당 id를 가진 리뷰 존재")
            void getRegisteredReviewWithThrow_WhenFoundRegisteredMember_ReturnReview() {
                //given
                Long reviewId = 1L;
                ReviewStatus status = ReviewStatus.REGISTERED;

                when(reviewRepository.findByIdAndStatus(reviewId, status)).thenReturn(Optional.of(review));

                // when
                Review response = reviewReadServiceImpl.getRegisteredReviewWithThrow(reviewId);

                //then
                assertNotNull(response);
                assertEquals(reviewId, response.getId());
                assertEquals(shop, response.getShop());
                assertEquals(member, response.getMember());
                assertEquals(status, response.getStatus());

                verify(reviewRepository, times(1)).findByIdAndStatus(reviewId, status);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {

            @Test
            @DisplayName("해당 id의 리뷰가 존재하지 않음")
            void getRegisteredReviewWithThrow_WhenNotFoundReview_ThrowsException() {
                // given
                Long reviewId = 1L;
                ReviewStatus status = ReviewStatus.REGISTERED;

                when(reviewRepository.findByIdAndStatus(reviewId, status)).thenReturn(Optional.empty());

                // when / then
                BusinessException exception = assertThrows(BusinessException.class, () -> reviewReadServiceImpl.getRegisteredReviewWithThrow(reviewId));

                assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
                verify(reviewRepository, times(1)).findByIdAndStatus(reviewId, status);
            }
        }
    }

    @Nested
    @DisplayName("회원 전체 리뷰 조회")
    class GetAllReviewsForMember {
        private Member member1;
        private Member member2;
        private Brand brand;

        private Shop shop;
        private Review review1;
        private Review review2;
        private Review review3;

        @BeforeEach
        void setUp() {
            member1 = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            member2 = Member.builder().id(2L).kakaoId(2000L).nickname("user2").build();
            brand = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member1).shop(shop).starRating(5).content("user1 작성한 리뷰 내용-1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).build();
            review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member1).shop(shop).starRating(4).content("user1 작성한 리뷰 내용-2").purity(PurityScore.UNSELECTED).retouch(RetouchScore.UNSELECTED).item(ItemScore.UNSELECTED).build();
            review3 = Review.builder().id(3L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member2).shop(shop).starRating(3).content("user2 작성한 리뷰 내용-1").purity(PurityScore.BAD).retouch(RetouchScore.BAD).item(ItemScore.BAD).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("user1 리뷰 조회")
            void getAllReviewsForMemberSuccess1() {
                // given
                Long memberId = 1L;
                List<Review> reviews = new ArrayList<>();
                reviews.add(review1);
                reviews.add(review2);
                ReviewResponse reviewResponse1 = ReviewResponse.builder().id(review1.getId()).createDate(review1.getCreateDate().toString()).modifyDate(review1.getModifyDate().toString()).starRating(review1.getStarRating()).content(review1.getContent()).purity(review1.getPurity()).retouch(review1.getRetouch()).item(review1.getItem()).build();
                ReviewResponse reviewResponse2 = ReviewResponse.builder().id(review2.getId()).createDate(review2.getCreateDate().toString()).modifyDate(review2.getModifyDate().toString()).starRating(review2.getStarRating()).content(review2.getContent()).purity(review2.getPurity()).retouch(review2.getRetouch()).item(review2.getItem()).build();
                ShopResponse shopResponse = ShopResponse.builder().id(shop.getId()).brand(brand.getBrandName()).placeName(shop.getPlaceName()).build();

                // when
                when(reviewRepository.findAllByMemberIdOrderByCreateDateDesc(memberId)).thenReturn(reviews);
                when(reviewMapper.toResponse(review1)).thenReturn(reviewResponse1);
                when(reviewMapper.toResponse(review2)).thenReturn(reviewResponse2);
                when(shopMapper.toResponse(shop, brand)).thenReturn(shopResponse);

                List<MemberReviewResponse> memberReviews = reviewReadServiceImpl.getAllReviewsForMember(memberId);

                // then
                assertEquals(memberReviews.size(), 2);

                assertEquals(memberReviews.get(0).getReviewInfo().getId(), review1.getId());
                assertEquals(memberReviews.get(0).getReviewInfo().getContent(), review1.getContent());
                assertEquals(memberReviews.get(0).getShopInfo().getId(), shop.getId());
                assertEquals(memberReviews.get(0).getShopInfo().getPlaceName(), shop.getPlaceName());

                assertEquals(memberReviews.get(1).getReviewInfo().getId(), review2.getId());
                assertEquals(memberReviews.get(1).getReviewInfo().getContent(), review2.getContent());
                assertEquals(memberReviews.get(1).getShopInfo().getId(), shop.getId());
                assertEquals(memberReviews.get(1).getShopInfo().getPlaceName(), shop.getPlaceName());

            }

            @Test
            @DisplayName("user2 리뷰 조회")
            void getAllReviewsForMemberSuccess2() {
                Long memberId = 2L;
                List<Review> reviews = new ArrayList<>();
                reviews.add(review3);

                ReviewResponse reviewResponse = ReviewResponse.builder().id(review3.getId()).createDate(review3.getCreateDate().toString()).modifyDate(review3.getModifyDate().toString()).starRating(review3.getStarRating()).content(review3.getContent()).purity(review3.getPurity()).retouch(review3.getRetouch()).item(review3.getItem()).build();
                ShopResponse shopResponse = ShopResponse.builder().id(shop.getId()).brand(brand.getBrandName()).placeName(shop.getPlaceName()).build();

                // when
                when(reviewRepository.findAllByMemberIdOrderByCreateDateDesc(memberId)).thenReturn(reviews);
                when(reviewMapper.toResponse(review3)).thenReturn(reviewResponse);
                when(shopMapper.toResponse(shop, brand)).thenReturn(shopResponse);

                List<MemberReviewResponse> memberReviews = reviewReadServiceImpl.getAllReviewsForMember(memberId);

                // then
                assertEquals(memberReviews.size(), 1);

                assertEquals(memberReviews.get(0).getReviewInfo().getId(), review3.getId());
                assertEquals(memberReviews.get(0).getReviewInfo().getContent(), review3.getContent());

                assertEquals(memberReviews.get(0).getShopInfo().getId(), shop.getId());
                assertEquals(memberReviews.get(0).getShopInfo().getPlaceName(), shop.getPlaceName());
            }

            @Test
            @DisplayName("회원의 리뷰가 존재하지 않는 경우")
            void getAllReviewsForMemberSuccess3() {
                // given
                Long memberId = 1L;
                List<Review> reviews = new ArrayList<>();

                // when
                when(reviewRepository.findAllByMemberIdOrderByCreateDateDesc(memberId)).thenReturn(reviews);

                List<MemberReviewResponse> memberReviews = reviewReadServiceImpl.getAllReviewsForMember(memberId);

                // then
                assertEquals(memberReviews.size(), 0);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {

        }
    }

    @Nested
    @DisplayName("지점 전체 리뷰 조회")
    class GetAllReviewsForShop {
        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("shopId를 가진 지점이 있고 리뷰가 있을 때 지점 전체 리뷰 반환")
            void getAllReviewsForShop_Success1() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).placeName("placeName").address("address").build();
                Member member1 = Member.builder().id(1L).kakaoId(1000L).nickname("member1").mainTitleName("뉴비").build();
                Member member2 = Member.builder().id(2L).kakaoId(2000L).nickname("member2").mainTitleName(null).build();
                Review review1 = Review.builder().id(1L).member(member1).shop(shop).starRating(5).content("내용1").build();
                Review review2 = Review.builder().id(2L).member(member2).shop(shop).starRating(4).content("내용2").build();
                List<Review> reviews = Arrays.asList(review1, review2);
                ReviewResponse reviewResponse1 = ReviewResponse.builder().id(review1.getId()).starRating(review1.getStarRating()).content(review1.getContent()).build();
                ReviewResponse reviewResponse2 = ReviewResponse.builder().id(review2.getId()).starRating(review2.getStarRating()).content(review2.getContent()).build();
                MemberResponse memberResponse1 = MemberResponse.builder().id(member1.getId()).nickname(member1.getNickname()).mainTitleName("뉴비").build();
                MemberResponse memberResponse2 = MemberResponse.builder().id(member2.getId()).nickname(member2.getNickname()).mainTitleName("").build();

                // when
                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).willReturn(reviews);
                given(reviewMapper.toResponse(review1)).willReturn(reviewResponse1);
                given(reviewMapper.toResponse(review2)).willReturn(reviewResponse2);
                given(memberMapper.toResponse(member1, "뉴비")).willReturn(memberResponse1);
                given(memberMapper.toResponse(member2, "")).willReturn(memberResponse2);

                // when
                List<ShopReviewResponse> response = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(reviews.size(), response.size());
                assertEquals(reviewResponse1, response.get(0).getReviewInfo());
                assertEquals(memberResponse1, response.get(0).getMemberInfo());
                assertEquals(reviewResponse2, response.get(1).getReviewInfo());
                assertEquals(memberResponse2, response.get(1).getMemberInfo());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopIdOrderByCreateDateDesc(shopId);
                verify(reviewMapper, times(reviews.size())).toResponse(any());
                verify(memberMapper, times(reviews.size())).toResponse(any(), anyString());
            }

            @Test
            @DisplayName("shopId를 가진 지점이 있고 리뷰가 없을 때 빈 리뷰 리스트 반환")
            void getAllReviewsForShop_Success2() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).placeName("placeName").address("address").build();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).willReturn(Collections.emptyList());

                // when
                List<ShopReviewResponse> response = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(0, response.size());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopIdOrderByCreateDateDesc(shopId);
                verify(reviewMapper, never()).toResponse(any());
                verify(memberMapper, never()).toResponse(any(), anyString());

            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("ShopId에 해당하는 Shop 존재하지 않는 경우")
            void getAllReviewsForShopFail1() {
                // given
                Long shopId = 4L;
                BusinessException exception = new BusinessException(ErrorCode.SHOP_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenThrow(exception);

                // then
                BusinessException resultException = assertThrows(exception.getClass(), () -> reviewReadServiceImpl.getAllReviewsForShop(shopId));
                assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                assertEquals(resultException.getMessage(), exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("최신 리뷰 3건 가져오기")
    class GetRecentReviewsForShop {
        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("상점이 존재하고 상점 리뷰가 존재할 때 작성일 기준 최신 리뷰 3건 반환")
            void getRecentReviewsForShop_Success1() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).build();
                Member member1 = Member.builder().id(1L).mainTitleName("뉴비").build();
                Member member2 = Member.builder().id(2L).mainTitleName(null).build();
                Review review1 = Review.builder().id(1L).shop(shop).member(member1).build();
                Review review2 = Review.builder().id(1L).shop(shop).member(member2).build();
                List<Review> reviews = List.of(review1, review2);
                ReviewResponse reviewResponse1 = ReviewResponse.builder().id(review1.getId()).build();
                ReviewResponse reviewResponse2 = ReviewResponse.builder().id(review2.getId()).build();
                MemberResponse memberResponse1 = MemberResponse.builder().id(member1.getId()).build();
                MemberResponse memberResponse2 = MemberResponse.builder().id(member2.getId()).build();


                given(reviewRepository.findTop3ByShopIdOrderByCreateDateDesc(shopId)).willReturn(reviews);
                given(reviewMapper.toResponse(review1)).willReturn(reviewResponse1);
                given(reviewMapper.toResponse(review2)).willReturn(reviewResponse2);
                given(memberMapper.toResponse(member1, "뉴비")).willReturn(memberResponse1);
                given(memberMapper.toResponse(member2, "")).willReturn(memberResponse2);

                // when
                List<ShopReviewResponse> response = reviewReadServiceImpl.getRecentReviewsForShop(shopId);

                // then
                assertEquals(reviews.size(), response.size());
                assertEquals(reviewResponse1, response.get(0).getReviewInfo());
                assertEquals(memberResponse1, response.get(0).getMemberInfo());
                assertEquals(reviewResponse2, response.get(1).getReviewInfo());
                assertEquals(memberResponse2, response.get(1).getMemberInfo());
                verify(reviewRepository).findTop3ByShopIdOrderByCreateDateDesc(shopId);
                verify(reviewMapper, times(2)).toResponse(any());
                verify(memberMapper, times(2)).toResponse(any(), anyString());
            }

            @Test
            @DisplayName("상점이 존재하고 상점 리뷰가 존재하지 않을 때 빈 리뷰 리스트를 반환")
            void getRecentReviewsForShop_Success2() {
                Long shopId = 1L;

                given(reviewRepository.findTop3ByShopIdOrderByCreateDateDesc(shopId)).willReturn(Collections.emptyList());

                // when
                List<ShopReviewResponse> response = reviewReadServiceImpl.getRecentReviewsForShop(shopId);

                // then
                assertEquals(0, response.size());
                verify(reviewRepository).findTop3ByShopIdOrderByCreateDateDesc(shopId);
                verify(reviewMapper, never()).toResponse(any());
                verify(memberMapper, never()).toResponse(any(), anyString());
            }
        }
    }

    @Nested
    @DisplayName("상점의 리뷰 정보 가져오기")
    class GetShopReviewInfo {
        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("상점에 리뷰가 있을 때 상점의 리뷰 정보 반환")
            void getShopReviewInfo_Success1() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).build();
                Review review1 = Review.builder().id(1L).shop(shop).starRating(3).build();
                Review review2 = Review.builder().id(2L).shop(shop).starRating(3).build();
                Review review3 = Review.builder().id(3L).shop(shop).starRating(4).build();
                List<Review> reviews = List.of(review1, review2, review3);

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopId(shopId)).willReturn(reviews);

                // when
                ShopReviewInfoResponse response = reviewReadServiceImpl.getShopReviewInfo(shopId);

                // then
                assertEquals(shopId, response.getShopId());
                assertEquals(reviews.size(), response.getReviewCnt());
                assertEquals(3.3, response.getStarRatingAvg());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopId(shopId);
            }

            @Test
            @DisplayName("상점의 리뷰가 없을 때 별점 평균 처리")
            void getShopReviewInfo_Success2() {
                // given
                Long shopId = 1L;
                Shop shop = Shop.builder().id(shopId).build();
                List<Review> reviews = List.of();

                given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
                given(reviewRepository.findAllByShopId(shopId)).willReturn(reviews);

                // when
                ShopReviewInfoResponse response = reviewReadServiceImpl.getShopReviewInfo(shopId);

                // then
                assertEquals(shopId, response.getShopId());
                assertEquals(0, response.getReviewCnt());
                assertEquals(0.0, response.getStarRatingAvg());
                verify(shopRepository).findById(shopId);
                verify(reviewRepository).findAllByShopId(shopId);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("shopId 해당하는 상점이 존재하지 않는 경우 예외 발생")
            void getShopReviewInfo_Fail1() {
                // given
                Long shopId = 1L;

                given(shopRepository.findById(shopId)).willReturn(Optional.empty());

                // when & then
                BusinessException response = assertThrows(BusinessException.class, () -> reviewReadServiceImpl.getShopReviewInfo(shopId));
                assertEquals(ErrorCode.SHOP_NOT_FOUND, response.getErrorCode());
                verify(shopRepository).findById(anyLong());
                verify(reviewRepository, never()).findAllByShopId(anyLong());
            }
        }
    }

}
*/

package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewRequestServiceImplTest {
    @InjectMocks
    private ReviewRequestServiceImpl requestReviewServiceImpl;
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReviewMapper reviewMapper;

    /*@Nested
    @DisplayName("상점 리뷰 작성")
    class WriteReview {

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("shopId 가진 지점에 review 추가")
            void writeReview_success() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();
                Shop shop = new Shop();
                Member member = new Member();
                Review review = new Review();
                ReviewResponse response = new ReviewResponse(1L, LocalDateTime.now().toString(), LocalDateTime.now().toString(), request.getStarRating(), request.getContent(), PurityScore.valueOf(request.getPurity()), RetouchScore.valueOf(request.getRetouch()), ItemScore.valueOf(request.getItem()), new ArrayList<>());

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
                when(reviewMapper.toEntity(any(Member.class), any(Shop.class), any(ReviewRequest.class))).thenReturn(review);
                when(reviewRepository.save(any(Review.class))).thenReturn(review);
                when(reviewMapper.toResponse(any(Review.class))).thenReturn(response);

                ReviewResponse result = requestReviewServiceImpl.writeReview(shopId, memberId, request);

                // then
                Assertions.assertEquals(result.getId(), response.getId());
                Assertions.assertEquals(result.getCreateDate(), response.getCreateDate());
                Assertions.assertEquals(result.getModifyDate(), response.getModifyDate());
                Assertions.assertEquals(result.getStarRating(), response.getStarRating());
                Assertions.assertEquals(result.getContent(), response.getContent());
                Assertions.assertEquals(result.getPurity(), response.getPurity());
                Assertions.assertEquals(result.getRetouch(), response.getRetouch());
                Assertions.assertEquals(result.getItem(), response.getItem());
            }

            @Test
            @DisplayName("요청 데이터의 purity, retouch, item 값이 null일 때")
            void writeReview_success2() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity(null).retouch(null).item(null).build();
                Shop shop = new Shop();
                Member member = new Member();
                Review review = new Review();
                ReviewResponse response = new ReviewResponse(1L, LocalDateTime.now().toString(), LocalDateTime.now().toString(), request.getStarRating(), request.getContent(), PurityScore.UNSELECTED, RetouchScore.UNSELECTED, ItemScore.UNSELECTED, new ArrayList<>());

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
                when(reviewMapper.toEntity(any(Member.class), any(Shop.class), any(ReviewRequest.class))).thenReturn(review);
                when(reviewRepository.save(any(Review.class))).thenReturn(review);
                when(reviewMapper.toResponse(any(Review.class))).thenReturn(response);

                ReviewResponse result = requestReviewServiceImpl.writeReview(shopId, memberId, request);

                // then
                Assertions.assertEquals(result.getId(), response.getId());
                Assertions.assertEquals(result.getCreateDate(), response.getCreateDate());
                Assertions.assertEquals(result.getModifyDate(), response.getModifyDate());
                Assertions.assertEquals(result.getStarRating(), response.getStarRating());
                Assertions.assertEquals(result.getContent(), response.getContent());
                Assertions.assertEquals(result.getPurity(), response.getPurity());
                Assertions.assertEquals(result.getRetouch(), response.getRetouch());
                Assertions.assertEquals(result.getItem(), response.getItem());
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("ShopId의 지점(shop) 존재하지 않는 경우")
            void writeReview_fail1() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = new ReviewRequest(3, "내용", "GOOD", "GOOD", "GOOD");
                BusinessException exception = new BusinessException(ErrorCode.SHOP_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

                // then
                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReview(shopId, memberId, request));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }

            @Test
            @DisplayName("memberId의 회원(member) 존재하지 않는 경우")
            void writeReview_fail2() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = new ReviewRequest(3, "내용", "GOOD", "GOOD", "GOOD");
                BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(new Shop()));
                when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReview(shopId, memberId, request));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }

            @Test
            @DisplayName("ReviewRequest 존재하지 않는 경우")
            public void writeReview_fail3() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                BusinessException exception = new BusinessException(ErrorCode.MISSING_PARAMETER);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(new Shop()));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(new Member()));

                // then
                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReview(shopId, memberId, null));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }
        }
    }
*/
    @Nested
    @DisplayName("특정 리뷰 수정")
    class ModifyReview {
        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("해당 id 가진 리뷰 수정")
            void modifyReviewSuccess1() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;
                ReviewRequest request = new ReviewRequest(5, "수정 내용", "GOOD", "GOOD", "GOOD");

                Member member = Member.builder().id(memberId).build();
                Shop shop = Shop.builder().id(2L).build();

                Review originalReview = Review.builder().id(reviewId).member(member).shop(shop).createDate(LocalDateTime.now()).build();
                Review newReview = Review.builder()
                        .id(reviewId)
                        .createDate(originalReview.getCreateDate())
                        .modifyDate(LocalDateTime.now())
                        .member(originalReview.getMember())
                        .shop(originalReview.getShop())
                        .starRating(request.getStarRating())
                        .content(request.getContent())
                        .purity(PurityScore.valueOf(request.getPurity()))
                        .retouch(RetouchScore.valueOf(request.getRetouch()))
                        .item(ItemScore.valueOf(request.getItem()))
                        .build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(originalReview));
                when(reviewMapper.toEntity(originalReview, request)).thenReturn(newReview);
                when(reviewRepository.save(newReview)).thenReturn(newReview);

                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                verify(reviewRepository).findById(reviewId);
                verify(reviewMapper).toEntity(originalReview, request);
                verify(reviewRepository).save(newReview);

                Assertions.assertEquals(shop.getId(), result);
            }

            @Test
            @DisplayName("요청의 purity, retouch, item 값이 null인 경우")
            void modifyReviewSuccess2() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;
                ReviewRequest request = ReviewRequest.builder().starRating(5).content("수정 내용").build();

                Member member = Member.builder().id(memberId).build();
                Shop shop = Shop.builder().id(2L).build();

                Review originalReview = Review.builder().id(reviewId).createDate(LocalDateTime.now()).member(member).shop(shop).build();
                Review newReview = Review.builder()
                        .id(originalReview.getId())
                        .createDate(originalReview.getCreateDate())
                        .modifyDate(LocalDateTime.now())
                        .member(originalReview.getMember())
                        .shop(originalReview.getShop())
                        .starRating(request.getStarRating())
                        .content(request.getContent())
                        .purity(PurityScore.UNSELECTED)
                        .retouch(RetouchScore.UNSELECTED)
                        .item(ItemScore.UNSELECTED)
                        .build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(originalReview));
                when(reviewMapper.toEntity(originalReview, request)).thenReturn(newReview);
                when(reviewRepository.save(newReview)).thenReturn(newReview);

                Long result = requestReviewServiceImpl.modifyReview(memberId, reviewId, request);

                // then
                verify(reviewRepository).findById(reviewId);
                verify(reviewMapper).toEntity(originalReview, request);
                verify(reviewRepository).save(newReview);

                Assertions.assertEquals(shop.getId(), result);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("해당 id 가진 리뷰가 존재하지 않음")
            void modifyReviewFail1() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

                // then
                BusinessException resultException = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.modifyReview(memberId, reviewId, new ReviewRequest()));

                Assertions.assertEquals(resultException.getErrorCode(), ErrorCode.REVIEW_NOT_FOUND);
                Assertions.assertEquals(resultException.getMessage(), ErrorCode.REVIEW_NOT_FOUND.getMessage());

                verify(reviewRepository, never()).save(any());
            }

            @Test
            @DisplayName("사용자와 리뷰 작성자 불일치")
            void modifyReviewFail2() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;

                Member member = Member.builder().id(100L).build();
                Review originalReview = Review.builder().id(reviewId).member(member).build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(originalReview));

                // then
                BusinessException resultException = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.modifyReview(memberId, reviewId, new ReviewRequest()));

                Assertions.assertEquals(resultException.getErrorCode(), ErrorCode.WRITER_DOES_NOT_MATCH);
                Assertions.assertEquals(resultException.getMessage(), ErrorCode.WRITER_DOES_NOT_MATCH.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("특정 리뷰 삭제")
    class DeleteReview {
        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("특정 id의 리뷰 존재")
            void deleteReviewSuccessCase1() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;
                Long shopId = 1L;
                Member member = Member.builder().id(memberId).build(); // 현재 사용자
                Shop shop = Shop.builder().id(shopId).build();
                Review review = Review.builder().id(reviewId).member(member).shop(shop).build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

                Long result = requestReviewServiceImpl.deleteReview(memberId, reviewId);

                // then
                Assertions.assertEquals(shopId, result);

                verify(reviewRepository, times(1)).delete(review);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("해당 id 가진 리뷰 존재하지 않음")
            void modifyReviewFail1() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;
                Member user = Member.builder().id(memberId).build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

                // then
                BusinessException resultException = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.deleteReview(memberId, reviewId));

                Assertions.assertEquals(ErrorCode.REVIEW_NOT_FOUND, resultException.getErrorCode());
                Assertions.assertEquals(ErrorCode.REVIEW_NOT_FOUND.getMessage(), resultException.getMessage());

                verify(reviewRepository, never()).delete(any());
            }

            @Test
            @DisplayName("리뷰 작성자와 사용자 불일치")
            void modifyReviewFail2() {
                // given
                Long memberId = 1L;
                Long reviewId = 2L;

                Member member = Member.builder().id(100L).build();
                Review review = Review.builder().id(reviewId).member(member).build();

                // when
                when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

                // then
                BusinessException resultException = Assertions.assertThrows(BusinessException.class,
                        () -> requestReviewServiceImpl.deleteReview(memberId, reviewId));

                Assertions.assertEquals(ErrorCode.WRITER_DOES_NOT_MATCH, resultException.getErrorCode());
                Assertions.assertEquals(ErrorCode.WRITER_DOES_NOT_MATCH.getMessage(), resultException.getMessage());

                verify(reviewRepository, never()).delete(any());
            }
        }
    }
}

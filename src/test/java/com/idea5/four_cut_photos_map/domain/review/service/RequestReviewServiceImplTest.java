package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponseDetail;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.mapper.ReviewMapper2;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestReviewServiceImplTest {
    @InjectMocks
    private RequestReviewServiceImpl requestReviewServiceImpl;
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReviewMapper2 reviewMapper;

    @Nested
    @DisplayName("상점 리뷰 작성")
    class WriteReviewForShop {

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("shopId 가진 지점에 review 추가")
            void writeReviewForShopSuccess1() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();
                Shop shop = new Shop();
                Member writer = new Member();
                Review review = new Review();
                ReviewResponse response = new ReviewResponse(1L, LocalDateTime.now(), LocalDateTime.now(), request.getStarRating(), request.getContent(), PurityScore.valueOf(request.getPurity()), RetouchScore.valueOf(request.getRetouch()), ItemScore.valueOf(request.getItem()));

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(writer));
                when(reviewMapper.toEntity(any(Member.class), any(Shop.class), any(ReviewRequest.class))).thenReturn(review);
                when(reviewRepository.save(any(Review.class))).thenReturn(review);
                when(reviewMapper.toResponse(any(Review.class))).thenReturn(response);

                ReviewResponse result = requestReviewServiceImpl.writeReviewForShop(shopId, memberId, request);

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
            void writeReviewForShopSuccess2() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = ReviewRequest.builder().starRating(3).content("리뷰 내용").purity(null).retouch(null).item(null).build();
                Shop shop = new Shop();
                Member writer = new Member();
                Review review = new Review();
                ReviewResponse response = new ReviewResponse(1L, LocalDateTime.now(), LocalDateTime.now(), request.getStarRating(), request.getContent(), PurityScore.UNSELECTED, RetouchScore.UNSELECTED, ItemScore.UNSELECTED);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(writer));
                when(reviewMapper.toEntity(any(Member.class), any(Shop.class), any(ReviewRequest.class))).thenReturn(review);
                when(reviewRepository.save(any(Review.class))).thenReturn(review);
                when(reviewMapper.toResponse(any(Review.class))).thenReturn(response);

                ReviewResponse result = requestReviewServiceImpl.writeReviewForShop(shopId, memberId, request);

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
            void writeReviewForShopFail1() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = new ReviewRequest(3, "내용", "GOOD", "GOOD", "GOOD");
                BusinessException exception = new BusinessException(ErrorCode.SHOP_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

                // then
                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReviewForShop(shopId, memberId, request));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }

            @Test
            @DisplayName("memberId의 회원(member) 존재하지 않는 경우")
            void writeReviewForShopFail2() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                ReviewRequest request = new ReviewRequest(3, "내용", "GOOD", "GOOD", "GOOD");
                BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(new Shop()));
                when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReviewForShop(shopId, memberId, request));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }

            @Test
            @DisplayName("ReviewRequest 존재하지 않는 경우")
            public void writeReviewForShopFail3() {
                // given
                Long shopId = 1L;
                Long memberId = 1L;
                BusinessException exception = new BusinessException(ErrorCode.MISSING_PARAMETER);

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(new Shop()));
                when(memberRepository.findById(memberId)).thenReturn(Optional.of(new Member()));

                // then
                BusinessException result = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.writeReviewForShop(shopId, memberId, null));
                Assertions.assertEquals(exception.getErrorCode(), result.getErrorCode());
                Assertions.assertEquals(exception.getMessage(), result.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("특정 리뷰 수정")
    class ModifyReview {
        private Member writer;
        private Brand brand;
        private Shop shop;
        private Review review;

        @BeforeEach
        void setUp() {
            writer = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            brand = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).writer(writer).shop(shop).starRating(5).content("리뷰 내용").purity(PurityScore.BAD).retouch(RetouchScore.BAD).item(ItemScore.BAD).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("해당 id 가진 리뷰 수정")
            void modifyReviewSuccess1() {
                // given
                Long modifyReviewId = 1L;
                Member user = Member.builder().id(1L).build();
                ReviewRequest modifyReviewDto = ReviewRequest.builder().starRating(3).content("수정 후 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();

                // when
                when(reviewRepository.findById(modifyReviewId)).thenReturn(Optional.of(review));

                ReviewResponseDetail reviewResponseDetail = requestReviewServiceImpl.modify(user, modifyReviewId, modifyReviewDto);

                // then
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getStarRating(), modifyReviewDto.getStarRating());
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getContent(), modifyReviewDto.getContent());
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getPurity(), PurityScore.valueOf(modifyReviewDto.getPurity()));
                Assertions.assertEquals(String.valueOf(reviewResponseDetail.getReviewInfo().getRetouch()), modifyReviewDto.getRetouch());
                Assertions.assertEquals(String.valueOf(reviewResponseDetail.getReviewInfo().getItem()), modifyReviewDto.getItem());
            }

            @Test
            @DisplayName("purity, retouch, item를 null로 전송")
            void modifyReviewSuccess2() {
                // given
                Long modifyReviewId = 1L;
                Member user = Member.builder().id(1L).build();
                ReviewRequest modifyReviewDto = ReviewRequest.builder().starRating(3).content("수정 후 리뷰 내용").build();

                // when
                when(reviewRepository.findById(modifyReviewId)).thenReturn(Optional.of(review));

                ReviewResponseDetail reviewResponseDetail = requestReviewServiceImpl.modify(user, modifyReviewId, modifyReviewDto);

                // then
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getId(), modifyReviewId);
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getStarRating(), modifyReviewDto.getStarRating());
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getContent(), modifyReviewDto.getContent());
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getPurity(), PurityScore.UNSELECTED);
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getRetouch(), RetouchScore.UNSELECTED);
                Assertions.assertEquals(reviewResponseDetail.getReviewInfo().getItem(), ItemScore.UNSELECTED);

                Assertions.assertEquals(reviewResponseDetail.getMemberInfo().getId(), writer.getId());
                Assertions.assertEquals(reviewResponseDetail.getMemberInfo().getNickname(), writer.getNickname());

                Assertions.assertEquals(reviewResponseDetail.getShopInfo().getId(), shop.getId());
                Assertions.assertEquals(reviewResponseDetail.getShopInfo().getPlaceName(), shop.getPlaceName());
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("해당 id 가진 리뷰 존재하지 않음")
            void modifyReviewFail1() {
                // given
                Long modifyReviewId = 2L;
                Member user = Member.builder().id(1L).build();
                ReviewRequest modifyReviewDto = ReviewRequest.builder().starRating(3).content("수정 후 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();
                BusinessException exception = new BusinessException(ErrorCode.REVIEW_NOT_FOUND);

                // when
                when(reviewRepository.findById(modifyReviewId)).thenThrow(exception);

                // then
                BusinessException resultException = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.modify(user, modifyReviewId, modifyReviewDto));
                Assertions.assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                Assertions.assertEquals(resultException.getMessage(), exception.getMessage());
            }

            @Test
            @DisplayName("사용자와 리뷰 작성자 불일치")
            void modifyReviewFail2() {
                // given
                Long modifyReviewId = 1L;
                Member user = Member.builder().id(2L).build();
                ReviewRequest modifyReviewDto = ReviewRequest.builder().starRating(3).content("수정 후 리뷰 내용").purity("GOOD").retouch("GOOD").item("GOOD").build();
                BusinessException exception = new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);

                // when
                when(reviewRepository.findById(modifyReviewId)).thenReturn(Optional.of(review));

                // then
                BusinessException resultException = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.modify(user, modifyReviewId, modifyReviewDto));
                Assertions.assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                Assertions.assertEquals(resultException.getMessage(), exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("특정 리뷰 삭제")
    class DeleteReview {
        private Member writer;
        private Brand brand;
        private Shop shop;
        private Review review;

        @BeforeEach
        void setUp() {
            writer = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            brand = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            review = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).writer(writer).shop(shop).starRating(5).content("리뷰 내용").purity(PurityScore.UNSELECTED).retouch(RetouchScore.UNSELECTED).item(ItemScore.UNSELECTED).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {

        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("해당 id 가진 리뷰 존재하지 않음")
            void modifyReviewFail1() {
                // given
                Long deleteReviewId = 2L;
                Member user = Member.builder().id(1L).build();
                BusinessException exception = new BusinessException(ErrorCode.REVIEW_NOT_FOUND);

                // when
                when(reviewRepository.findById(deleteReviewId)).thenThrow(exception);

                // then
                BusinessException resultException = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.delete(user, deleteReviewId));
                Assertions.assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                Assertions.assertEquals(resultException.getMessage(), exception.getMessage());
            }

            @Test
            @DisplayName("사용자와 리뷰 작성자 불일치")
            void modifyReviewFail2() {
                // given
                Long modifyReviewId = 1L;
                Member user = Member.builder().id(2L).build();
                BusinessException exception = new BusinessException(ErrorCode.WRITER_DOES_NOT_MATCH);

                // when
                when(reviewRepository.findById(modifyReviewId)).thenReturn(Optional.of(review));

                // then
                BusinessException resultException = Assertions.assertThrows(exception.getClass(), () -> requestReviewServiceImpl.delete(user, modifyReviewId));
                Assertions.assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                Assertions.assertEquals(resultException.getMessage(), exception.getMessage());
            }
        }
    }

}

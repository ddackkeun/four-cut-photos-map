package com.idea5.four_cut_photos_map.domain.review.service;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.review.dto.response.MemberReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ReviewResponse;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewInfoDto;
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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
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
    private MemberTitleLogRepository memberTitleLogRepository;

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

        @Nested
        @DisplayName("실패")
        class FailCase {

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
        private Member member;
        private Brand brand1;
        private Brand brand2;
        private Brand brand3;
        private Shop shop1;
        private Shop shop2;
        private Shop shop3;
        private Review review1;
        private Review review2;
        private Review review3;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            brand1 = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            brand2 = Brand.builder().id(2L).brandName("하루필름").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_2.jpg").build();
            brand3 = Brand.builder().id(3L).brandName("포토이즘").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_3.jpg").build();
            shop1 = Shop.builder().id(1L).brand(brand1).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            shop2 = Shop.builder().id(2L).brand(brand2).placeName("하루필름 연트럴파크점").address("서울 마포구 양화로23길 30, 1층 (동교동)").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            shop3 = Shop.builder().id(3L).brand(brand3).placeName("포토이즘박스 광운대점").address("서울 노원구 석계로 95 성북빌딩").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
            review1 = Review.builder().id(1L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop1).starRating(5).content("shop1 작성한 리뷰 내용-1").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).build();
            review2 = Review.builder().id(2L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop1).starRating(4).content("shop1 작성한 리뷰 내용-2").purity(PurityScore.UNSELECTED).retouch(RetouchScore.UNSELECTED).item(ItemScore.UNSELECTED).build();
            review3 = Review.builder().id(3L).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop2).starRating(3).content("shop2 작성한 리뷰 내용-1").purity(PurityScore.BAD).retouch(RetouchScore.BAD).item(ItemScore.BAD).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("shopId로 지점 전체 리뷰 조회 - 회원의 mainMemberTitle 없는 경우")
            void getAllReviewsForShopSuccess1() {
                // given
                Long shopId = 1L;
                List<Review> reviews = Arrays.asList(review1, review2);
                String mainMemberTitleName = "";
                ReviewResponse reviewResponse1 = ReviewResponse.builder().id(review1.getId()).createDate(review1.getCreateDate().toString()).modifyDate(review1.getModifyDate().toString()).starRating(review1.getStarRating()).content(review1.getContent()).purity(review1.getPurity()).retouch(review1.getRetouch()).item(review1.getItem()).build();
                ReviewResponse reviewResponse2 = ReviewResponse.builder().id(review2.getId()).createDate(review2.getCreateDate().toString()).modifyDate(review2.getModifyDate().toString()).starRating(review2.getStarRating()).content(review2.getContent()).purity(review2.getPurity()).retouch(review2.getRetouch()).item(review2.getItem()).build();
                MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).nickname(member.getNickname()).mainTitleName(mainMemberTitleName).build();

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop1));
                when(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);
                when(memberTitleLogRepository.findByMemberAndIsMainTrue(member)).thenReturn(Optional.empty());
                when(reviewMapper.toResponse(review1)).thenReturn(reviewResponse1);
                when(reviewMapper.toResponse(review2)).thenReturn(reviewResponse2);
                when(memberMapper.toResponse(member, mainMemberTitleName)).thenReturn(memberResponse);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(result.size(), reviews.size());

                assertEquals(result.get(0).getReviewInfo().getId(), review1.getId());
                assertEquals(result.get(0).getReviewInfo().getContent(), review1.getContent());
                assertEquals(result.get(0).getMemberInfo().getId(), member.getId());
                assertEquals(result.get(0).getMemberInfo().getNickname(), member.getNickname());
                assertEquals(result.get(0).getMemberInfo().getMainTitleName(), mainMemberTitleName);

                assertEquals(result.get(1).getReviewInfo().getId(), review2.getId());
                assertEquals(result.get(1).getReviewInfo().getContent(), review2.getContent());
                assertEquals(result.get(1).getMemberInfo().getId(), member.getId());
                assertEquals(result.get(1).getMemberInfo().getNickname(), member.getNickname());
                assertEquals(result.get(1).getMemberInfo().getMainTitleName(), mainMemberTitleName);
            }

            @Test
            @DisplayName("shopId로 지점 전체 리뷰 조회 - 회원의 mainMemberTitle 존재하는 경우")
            void getAllReviewsForShopSuccess2() {
                // given
                Long shopId = 1L;
                List<Review> reviews = Arrays.asList(review1, review2);
                MemberTitle memberTitle = MemberTitle.builder().name("칭호명").standard("획득방법").content("설명").colorImageUrl("컬러 이미지").bwImageUrl("흑백 이미지").build();
                MemberTitleLog memberTitleLog = MemberTitleLog.builder().member(member).memberTitle(memberTitle).isMain(true).build();
                ReviewResponse reviewResponse1 = ReviewResponse.builder().id(review1.getId()).createDate(review1.getCreateDate().toString()).modifyDate(review1.getModifyDate().toString()).starRating(review1.getStarRating()).content(review1.getContent()).purity(review1.getPurity()).retouch(review1.getRetouch()).item(review1.getItem()).build();
                ReviewResponse reviewResponse2 = ReviewResponse.builder().id(review2.getId()).createDate(review2.getCreateDate().toString()).modifyDate(review2.getModifyDate().toString()).starRating(review2.getStarRating()).content(review2.getContent()).purity(review2.getPurity()).retouch(review2.getRetouch()).item(review2.getItem()).build();
                MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).nickname(member.getNickname()).mainTitleName(memberTitle.getName()).build();

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop1));
                when(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);
                when(memberTitleLogRepository.findByMemberAndIsMainTrue(member)).thenReturn(Optional.of(memberTitleLog));
                when(reviewMapper.toResponse(review1)).thenReturn(reviewResponse1);
                when(reviewMapper.toResponse(review2)).thenReturn(reviewResponse2);
                when(memberMapper.toResponse(member, memberTitle.getName())).thenReturn(memberResponse);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(result.size(), reviews.size());

                assertEquals(result.get(0).getReviewInfo().getId(), review1.getId());
                assertEquals(result.get(0).getReviewInfo().getContent(), review1.getContent());
                assertEquals(result.get(0).getMemberInfo().getId(), member.getId());
                assertEquals(result.get(0).getMemberInfo().getNickname(), member.getNickname());
                assertEquals(result.get(0).getMemberInfo().getMainTitleName(), memberTitle.getName());

                assertEquals(result.get(1).getReviewInfo().getId(), review2.getId());
                assertEquals(result.get(1).getReviewInfo().getContent(), review2.getContent());
                assertEquals(result.get(1).getMemberInfo().getId(), member.getId());
                assertEquals(result.get(1).getMemberInfo().getNickname(), member.getNickname());
                assertEquals(result.get(1).getMemberInfo().getMainTitleName(), memberTitle.getName());
            }

            @Test
            @DisplayName("shop2 리뷰 조회")
            void getAllReviewsForShopSuccess3() {
                // given
                Long shopId = 2L;
                List<Review> reviews = Arrays.asList(review3);
                String mainMemberTitleName = "";
                ReviewResponse reviewResponse = ReviewResponse.builder().id(review3.getId()).createDate(review3.getCreateDate().toString()).modifyDate(review3.getModifyDate().toString()).starRating(review3.getStarRating()).content(review3.getContent()).purity(review3.getPurity()).retouch(review3.getRetouch()).item(review3.getItem()).build();
                MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).nickname(member.getNickname()).mainTitleName(mainMemberTitleName).build();

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop2));
                when(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);
                when(memberTitleLogRepository.findByMemberAndIsMainTrue(member)).thenReturn(Optional.empty());
                when(reviewMapper.toResponse(review3)).thenReturn(reviewResponse);
                when(memberMapper.toResponse(member, mainMemberTitleName)).thenReturn(memberResponse);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(result.size(), reviews.size());

                assertEquals(result.get(0).getReviewInfo().getId(), review3.getId());
                assertEquals(result.get(0).getReviewInfo().getContent(), review3.getContent());
                assertEquals(result.get(0).getMemberInfo().getId(), member.getId());
                assertEquals(result.get(0).getMemberInfo().getNickname(), member.getNickname());
                assertEquals(result.get(0).getMemberInfo().getMainTitleName(), mainMemberTitleName);
            }

            @Test
            @DisplayName("shopId 해당하는 리뷰 없음")
            void getAllReviewsForShopSuccess4() {
                // given
                Long shopId = 3L;
                List<Review> reviews = new ArrayList<>();

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop3));
                when(reviewRepository.findAllByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getAllReviewsForShop(shopId);

                // then
                assertEquals(0, result.size());
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
        private Member member;
        private Brand brand;
        private Shop shop;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            brand = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
        }

        List<Review> createReviews(int n) {
            ArrayList<Review> reviews = new ArrayList<>();
            for(int i=1; i<=n; i++) {
                Review review = Review.builder().id((long)i).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(i).content("리뷰 " + i + "번 내용").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).build();
                reviews.add(review);
            }

            return reviews;
        }

        List<ReviewResponse> createReviewResponses(List<Review> reviews) {
            return reviews.stream()
                    .map(review -> {
                        return ReviewResponse.builder()
                                .id(review.getId())
                                .createDate(review.getCreateDate().toString())
                                .modifyDate(review.getModifyDate().toString())
                                .starRating(review.getStarRating())
                                .content(review.getContent())
                                .purity(review.getPurity())
                                .retouch(review.getRetouch())
                                .item(review.getItem())
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("상점의 작성일 기준 최신 리뷰 3건 조회")
            void getRecentReviewsForShopSuccess1() {
                // given
                Long shopId = 1L;
                int reviewCnt = 3;
                String mainMemberTitleName = "";
                List<Review> reviews = createReviews(reviewCnt);
                List<ReviewResponse> reviewResponses = createReviewResponses(reviews);
                MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).nickname(member.getNickname()).mainTitleName(mainMemberTitleName).build();

                // when
                when(reviewRepository.findTop3ByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);
                when(memberTitleLogRepository.findByMemberAndIsMainTrue(member)).thenReturn(Optional.empty());
                when(reviewMapper.toResponse(reviews.get(0))).thenReturn(reviewResponses.get(0));
                when(reviewMapper.toResponse(reviews.get(1))).thenReturn(reviewResponses.get(1));
                when(reviewMapper.toResponse(reviews.get(2))).thenReturn(reviewResponses.get(2));
                when(memberMapper.toResponse(member, mainMemberTitleName)).thenReturn(memberResponse);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getRecentReviewsForShop(shopId);

                // then
                assertEquals(result.size(), reviewCnt);
                assertEquals(result.get(0).getReviewInfo().getId(), reviews.get(0).getId());
                assertEquals(result.get(1).getReviewInfo().getId(), reviews.get(1).getId());
                assertEquals(result.get(2).getReviewInfo().getId(), reviews.get(2).getId());
            }

            @Test
            @DisplayName("상점 리뷰 3건 이하일 때 리뷰 조회")
            void getRecentReviewsForShopSuccess2() {
                Long shopId = 1L;
                int reviewCnt = 2;
                String mainMemberTitleName = "";
                List<Review> reviews = createReviews(reviewCnt);
                List<ReviewResponse> reviewResponses = createReviewResponses(reviews);
                MemberResponse memberResponse = MemberResponse.builder().id(member.getId()).nickname(member.getNickname()).mainTitleName(mainMemberTitleName).build();

                // when
                when(reviewRepository.findTop3ByShopIdOrderByCreateDateDesc(shopId)).thenReturn(reviews);
                when(memberTitleLogRepository.findByMemberAndIsMainTrue(member)).thenReturn(Optional.empty());
                when(reviewMapper.toResponse(reviews.get(0))).thenReturn(reviewResponses.get(0));
                when(reviewMapper.toResponse(reviews.get(1))).thenReturn(reviewResponses.get(1));
                when(memberMapper.toResponse(member, mainMemberTitleName)).thenReturn(memberResponse);

                List<ShopReviewResponse> result = reviewReadServiceImpl.getRecentReviewsForShop(shopId);

                // then
                assertEquals(result.size(), reviewCnt);
                assertEquals(result.get(0).getReviewInfo().getId(), reviews.get(0).getId());
                assertEquals(result.get(1).getReviewInfo().getId(), reviews.get(1).getId());
            }
        }
    }

    @Nested
    @DisplayName("상점의 리뷰 정보 가져오기")
    class GetShopReviewInfo {
        private Member member;
        private Shop shop;

        List<Review> createReviews(int n) {
            ArrayList<Review> reviews = new ArrayList<>();
            Random random = new Random();

            for(int i=1; i<=n; i++) {
                int startRating = random.nextInt(5) + 1;
                Review review = Review.builder().id((long)i).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).member(member).shop(shop).starRating(startRating).content("리뷰 " + i + "번 내용").purity(PurityScore.GOOD).retouch(RetouchScore.GOOD).item(ItemScore.GOOD).build();
                reviews.add(review);
            }
            return reviews;
        }

        @BeforeEach
        void setUp() {
            Brand brand = Brand.builder().id(1L).brandName("인생네컷").filePath("https://d18tllc1sxg8cp.cloudfront.net/brand_image/brand_1.jpg").build();
            member = Member.builder().id(1L).kakaoId(1000L).nickname("user1").build();
            shop = Shop.builder().id(1L).brand(brand).placeName("인생네컷망리단길점").address("서울 마포구 포은로 109-1").favoriteCnt(0).reviewCnt(0).starRatingAvg(0.0).build();
        }

        @Nested
        @DisplayName("성공")
        class SuccessCase {
            @Test
            @DisplayName("shopId 해당하는 상점 정보 가져오기")
            void getShopReviewInfoSuccess1() {
                // given
                Long shopId = 1L;
                int reviewCount = 10;
                List<Review> reviews = createReviews(reviewCount);
                double starRatingAvg = reviews.stream()
                        .mapToDouble(Review::getStarRating)
                        .average()
                        .orElse(0.0);
                starRatingAvg = Math.round(starRatingAvg * 10) / 10.0;

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(reviewRepository.findAllByShopId(shopId)).thenReturn(reviews);

                ShopReviewInfoDto result = reviewReadServiceImpl.getShopReviewInfo(shopId);

                // then
                assertEquals(result.getShopId(), shopId);
                assertEquals(result.getReviewCnt(), reviewCount);
                assertEquals(result.getStarRatingAvg(), starRatingAvg);
            }

            @Test
            @DisplayName("상점의 리뷰가 없을 때 별점 평균 처리")
            void getShopReviewInfoSuccess2() {
                // given
                Long shopId = 1L;
                int reviewCount = 0;
                double starRatingAvg = 0.0;
                List<Review> reviews = new ArrayList<>();

                // when
                when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
                when(reviewRepository.findAllByShopId(shopId)).thenReturn(reviews);

                ShopReviewInfoDto result = reviewReadServiceImpl.getShopReviewInfo(shopId);

                // then
                assertEquals(result.getShopId(), shopId);
                assertEquals(result.getReviewCnt(), reviewCount);
                assertEquals(result.getStarRatingAvg(), starRatingAvg);
            }
        }

        @Nested
        @DisplayName("실패")
        class FailCase {
            @Test
            @DisplayName("shopId 해당하는 상점이 존재하지 않는 경우")
            void getShopReviewInfoFail1() {
                // given
                Long shopId = 1L;
                BusinessException exception = new BusinessException(ErrorCode.SHOP_NOT_FOUND);

                // when
                when(shopRepository.findById(shopId)).thenThrow(exception);

                // then
                BusinessException resultException = assertThrows(exception.getClass(), () -> reviewReadServiceImpl.getShopReviewInfo(shopId));

                assertEquals(resultException.getErrorCode(), exception.getErrorCode());
                assertEquals(resultException.getMessage(), exception.getMessage());
            }
        }
    }

}

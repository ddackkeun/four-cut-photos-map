package com.idea5.four_cut_photos_map.domain.review.repository;

import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    List<Review> findAllByMemberId(Long memberId);

    List<Review> findAllByMemberIdAndStatus(Long memberId, ReviewStatus status);

    List<Review> findAllByShopIdAndStatus(Long shopId, ReviewStatus status);

    List<Review> findAllByShopIdOrderByCreateDateDesc(Long shopId);

    Optional<Review> findByMemberIdAndShopId(Long memberId, Long shopId);

    List<Review> findAllByMemberIdAndStatusOrderByIdDesc(Long memberId, ReviewStatus status);

    List<Review> findTop3ByShopIdOrderByCreateDateDesc(Long shopId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.shop.id = :shopId AND r.status = :status")
    Integer countByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") ReviewStatus status);

    @Query("SELECT AVG(r.starRating) FROM Review r WHERE r.shop.id = :shopId AND r.status = :status")
    Double findAverageStarRatingByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") ReviewStatus status);

    Long countByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    List<Review> findByModifyDateAfter(LocalDateTime minusMonths);

    @Query("SELECT r FROM Review r WHERE r.createDate BETWEEN :startDate AND :endDate AND (r.purity = 'GOOD' OR r.purity = 'BAD')")
    List<Review> findLastMonthReviewsWithGoodOrBadPurity(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.shop.id = :shopId AND r.id < :lastReviewId AND r.status = :status ORDER BY r.id DESC")
    List<Review> findReviewsWithMemberByShopAndCursor(@Param("shopId") Long shopId, @Param("lastReviewId") Long lastReviewId, @Param("status") ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.shop WHERE r.member.id = :memberId AND r.id < :lastReviewId AND r.status = :status ORDER BY r.id DESC")
    List<Review> findReviewsWithShopByMemberAndCursor(@Param("memberId") Long memberId, @Param("lastReviewId") Long lastReviewId, @Param("status") ReviewStatus status, Pageable pageable);

}
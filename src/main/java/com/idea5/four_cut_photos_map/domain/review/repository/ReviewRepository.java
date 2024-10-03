package com.idea5.four_cut_photos_map.domain.review.repository;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummary;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    List<Review> findAllByMemberId(Long memberId);

    List<Review> findAllByMemberIdAndStatus(Long memberId, ReviewStatus status);

    Long countByMemberId(Long memberId);

    List<Review> findByModifyDateAfter(LocalDateTime minusMonths);

    @Query("SELECT new com.idea5.four_cut_photos_map.domain.review.dto.response.ShopReviewSummary(COUNT(r), AVG(r.starRating)) FROM Review r WHERE r.shop.id = :shopId AND r.status = :status")
    ShopReviewSummary findReviewSummaryByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") ReviewStatus status);

    @Query("SELECT r FROM Review r WHERE r.createDate BETWEEN :startDate AND :endDate AND (r.purity = 'GOOD' OR r.purity = 'BAD')")
    List<Review> findLastMonthReviewsWithGoodOrBadPurity(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Review> findAllByShopAndStatusAndIdLessThanOrderByIdDesc(Shop shop, ReviewStatus status, long lastId, Pageable pageable);

    @Query("SELECT r From Review r WHERE r.member.id = :memberId AND r.status = :status AND r.id < :lastReviewId ORDER BY r.id DESC")
    List<Review> findAllByMemberIdAndStatusAndIdLessThan(@Param("memberId") Long memberId, @Param("status") ReviewStatus status, @Param("lastReviewId") Long lastReviewId, Pageable pageable);

    List<Review> findAllByMemberAndStatusAndIdLessThanOrderByIdDesc(Member member, ReviewStatus status, long lastReviewId, Pageable pageable);
}
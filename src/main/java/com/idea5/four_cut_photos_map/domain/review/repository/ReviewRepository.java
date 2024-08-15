package com.idea5.four_cut_photos_map.domain.review.repository;

import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    List<Review> findAllByMemberId(Long memberId);

    List<Review> findAllByShopId(Long shopId);

    List<Review> findAllByShopIdOrderByCreateDateDesc(Long shopId);

    Optional<Review> findByMemberIdAndShopId(Long memberId, Long shopId);

    List<Review> findAllByMemberIdOrderByCreateDateDesc(Long memberId);

    List<Review> findTop3ByShopIdOrderByCreateDateDesc(Long shopId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.shop.id = :shopId")
    Integer countByShopId(Long shopId);

    @Query("SELECT AVG(r.starRating) FROM Review r WHERE r.shop.id = :shopId")
    Double findAverageStarRatingByShopId(Long shopId);

    Long countByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);

    List<Review> findByModifyDateAfter(LocalDateTime minusMonths);

    @Query("SELECT r FROM Review r WHERE r.createDate BETWEEN :startDate AND :endDate AND (r.purity = 'GOOD' OR r.purity = 'BAD')")
    List<Review> findLastMonthReviewsWithGoodOrBadPurity(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
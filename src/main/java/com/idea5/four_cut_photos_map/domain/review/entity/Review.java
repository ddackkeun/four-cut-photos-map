package com.idea5.four_cut_photos_map.domain.review.entity;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @ToString.Exclude
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @ToString.Exclude
    private Shop shop;

    @Column(nullable = false)
    private int starRating;

    @Column(length = 500, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ReviewStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PurityScore purity;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RetouchScore retouch;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ItemScore item;

    public void changeStatus(ReviewStatus status) {
        this.status = status;
    }

    public Review update(ReviewRequest dto) {
        this.starRating = dto.getStarRating();
        this.content = dto.getContent();
        this.purity = PurityScore.valueOf(dto.getPurity());
        this.retouch = RetouchScore.valueOf(dto.getRetouch());
        this.item = ItemScore.valueOf(dto.getItem());

        return this;
    }

}

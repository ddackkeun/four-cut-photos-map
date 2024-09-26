package com.idea5.four_cut_photos_map.domain.review.entity;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.reviewphoto.entity.ReviewPhoto;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @Column(length = 50)
    private PurityScore purity;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RetouchScore retouch;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ItemScore item;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ReviewStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ReviewPhoto> photos = new ArrayList<>();

    public void addPhoto(ReviewPhoto photo) {
        photos.add(photo);
        photo.setReview(this);
    }

    public void removePhoto(ReviewPhoto photo) {
        photos.remove(photo);
        photo.setReview(null);
    }

    public void updateStatus(ReviewStatus status) {
        this.status = status;
    }

    public void update(int starRating, String content, String purity, String retouch, String item) {
        PurityScore purityScore = (purity != null) ? PurityScore.valueOf(purity) : null;
        RetouchScore retouchScore = (retouch != null) ? RetouchScore.valueOf(retouch) : null;
        ItemScore itemScore = (item != null) ? ItemScore.valueOf(item) : null;

        this.starRating = starRating;
        this.content = content;
        this.purity = purityScore;
        this.retouch = retouchScore;
        this.item = itemScore;
    }

    public void delete() {
        this.status = ReviewStatus.DELETED;
        this.photos.forEach(ReviewPhoto::delete);
    }
}

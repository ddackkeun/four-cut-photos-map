package com.idea5.four_cut_photos_map.domain.reviewphoto.entity;

import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "review_photo")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ReviewPhoto extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false)
    private Long shopId;

    @Column(length = 150, nullable = false)
    private String fileName;

    @Column(length = 200, nullable = false)
    private String filePath;

    @Column(length = 50, nullable = false)
    private String fileType;

    @Column(nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ReviewPhotoStatus status;

    public void setReview(Review review) {
        this.review = review;
    }

    public void updateStatus(ReviewPhotoStatus status) {
        this.status = status;
    }

}

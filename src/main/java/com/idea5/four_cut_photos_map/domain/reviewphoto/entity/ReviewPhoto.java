package com.idea5.four_cut_photos_map.domain.reviewphoto.entity;

import com.idea5.four_cut_photos_map.domain.reviewphoto.enums.ReviewPhotoStatus;
import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "review_photo")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ReviewPhoto extends BaseEntity {
    @Column(nullable = false)
    private Long reviewId;

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

    public void modifyStatus(ReviewPhotoStatus status) {
        this.status = status;
    }

}

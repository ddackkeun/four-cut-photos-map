package com.idea5.four_cut_photos_map.domain.memberTitle.entity;

import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class MemberTitle extends BaseEntity {
    @Column(length = 50, nullable = false)
    private String name;    // 칭호명

    @Column(length = 100, nullable = false)
    private String content; // 설명

    @Column(columnDefinition = "TEXT", nullable = false)
    private String colorImageUrl; // 칭호 컬러 이미지 URL

    @Column(columnDefinition = "TEXT", nullable = false)
    private String bwImageUrl; // 칭호 흑백 이미지 URL
}

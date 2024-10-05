package com.idea5.four_cut_photos_map.domain.memberTitle.entity;

import com.idea5.four_cut_photos_map.global.base.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Table(
        name = "member_title_log",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "member_title_id"})}
)
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class MemberTitleLog extends BaseEntity {
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_title_id", nullable = false)
    private MemberTitle memberTitle;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain; // 대표 칭호 여부

    public static MemberTitleLog create(Long memberId, MemberTitle memberTitle) {
        return new MemberTitleLog(memberId, memberTitle, false);
    }

    public String getMemberTitleName() {
        return getMemberTitle().getName();
    }

    public void registerMain() {
        this.isMain = true;
    }

    public void cancelMain() {
        this.isMain = false;
    }
}

package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewResponse {
    // Review 관련 정보
    private Long id;                    // 리뷰 번호
    private LocalDateTime createDate;   // 리뷰 생성 일자
    private LocalDateTime modifyDate;   // 리뷰 수정 일자
    private int starRating;            // 별점
    private String content;             // 내용
    private PurityScore purity;         // 청결도
    private RetouchScore retouch;       // 보정
    private ItemScore item;             // 소품
}

package com.idea5.four_cut_photos_map.domain.review.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import lombok.*;

/**
 * 지점 전체 리뷰 조회 API 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopReviewResponse {
    private ReviewResponse reviewInfo;   // 리뷰 정보
    private MemberResponse memberInfo;  // 회원 정보
}

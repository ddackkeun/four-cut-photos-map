package com.idea5.four_cut_photos_map.domain.shop.dto.response;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.review.dto.response.ResponseReviewDto;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 상세 조회 응답 DTO
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponseShopDetail {
    private long id;
    private String placeName;
    private String roadAddressName;
    private String distance;
    private String placeUrl;
    private double starRatingAvg;
    private int reviewCnt;
    private boolean canBeAddedToFavorites;
    private List<ResponseReviewDto> recentReviews;


    public static ResponseShopDetail of(Shop dbShop, String placeName, String placeUrl, String distance){
        return ResponseShopDetail.builder()
                .id(dbShop.getId())
                .placeName(placeName)
                .roadAddressName(dbShop.getRoadAddressName())
                .distance(distance)
                .placeUrl(placeUrl)
                .starRatingAvg(dbShop.getStarRatingAvg())
                .reviewCnt(dbShop.getReviewCnt())
                .build();
    }

    // todo: ShopTitle 관련 로직 임의로 주석 처리, 리팩토링 필요
//    @JsonIgnore // 상점이 보유한 칭호가 없다면 null 보다는 응답 데이터에서 제외되는게 더 낫다고 생각
//    private List<String> shopTitles = new ArrayList<>();
//    public void setShopTitles(List<String> shopTitles) {
//        this.shopTitles = shopTitles;
//    }
}
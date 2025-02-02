package com.idea5.four_cut_photos_map.domain.shop.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.idea5.four_cut_photos_map.domain.brand.dto.response.ResponseBrandDto;
import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 키워드 조회, 전체/브랜드별 조회 공통 응답 DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponseShop {
    private Long id;
    private String placeName;
    private String longitude;
    private String latitude;
    private String distance;
    private Double starRatingAvg;
    private Integer reviewCnt;
    private Integer favoriteCnt;
    private Boolean isFavorite;
    private ResponseBrandDto brand;

    static public ResponseShop of(Shop dbShop, KakaoMapSearchDto apiShop, Brand brand){
        ResponseBrandDto brandDto = ResponseBrandDto.builder()
                .brandName(brand.getBrandName())
                .filePath(brand.getFilePath())
                .build();

        return ResponseShop.builder()
                .id(dbShop.getId())
                .placeName(dbShop.getPlaceName())
                .longitude(apiShop.getLongitude())
                .latitude(apiShop.getLatitude())
                .distance(apiShop.getDistance())
                .starRatingAvg(dbShop.getStarRatingAvg())
                .reviewCnt(dbShop.getReviewCnt())
                .favoriteCnt(dbShop.getFavoriteCnt())
                .brand(brandDto)
                .build();
    }
}
package com.idea5.four_cut_photos_map.domain.shop.mapper;

import com.idea5.four_cut_photos_map.domain.brand.entity.Brand;
import com.idea5.four_cut_photos_map.domain.shop.dto.response.ShopResponse;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {
    public ShopResponse toResponse(Shop shop, Brand brand) {
        return ShopResponse.builder()
                .id(shop.getId())
                .brand(brand.getBrandName())
                .placeName(shop.getPlaceName())
                .build();
    }
}

package com.idea5.four_cut_photos_map.domain.shop.service;

import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;
import com.idea5.four_cut_photos_map.domain.shop.repository.ShopRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopReadServiceImplTest {
    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private ShopReadServiceImpl shopReadServiceImpl;

    @Test
    @DisplayName("특정 id를 가진 상점이 존재하는 경우")
    void getShopWithThrow_WhenFoundShop_ReturnShop() {
        // given
        Long shopId = 1L;
        Shop shop = Shop.builder().id(shopId).build();
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        // when
        Shop response = shopReadServiceImpl.getShopWithThrow(shopId);

        // then
        assertEquals(shopId, response.getId());
        verify(shopRepository, times(1)).findById(shopId);
    }

    @Test
    @DisplayName("특정 id를 가진 상점이 존재하지 않는 경우 예외 발생")
    void getShopWithThrow_WhenNotFoundShop_ThrowsException() {
        // given
        Long shopId = 1L;
        when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> shopReadServiceImpl.getShopWithThrow(shopId));

        // then
        assertEquals(ErrorCode.SHOP_NOT_FOUND, exception.getErrorCode());
        verify(shopRepository, times(1)).findById(shopId);
    }
}
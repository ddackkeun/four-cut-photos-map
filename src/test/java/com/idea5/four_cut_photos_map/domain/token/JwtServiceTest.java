package com.idea5.four_cut_photos_map.domain.token;

import com.idea5.four_cut_photos_map.domain.token.model.TokenDTO;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.security.jwt.JwtProvider;
import com.idea5.four_cut_photos_map.security.jwt.JwtService;
import com.idea5.four_cut_photos_map.security.jwt.dto.response.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisDao redisDao;

    @InjectMocks
    private JwtService jwtService;


    @Test
    @DisplayName("회원 정보를 통해서 JWT 토큰 생성")
    void generateTokens_success() {
        // given
        Long memberId = 1L;
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
        long currentTimeMillis = System.currentTimeMillis();
        Date expiredDate = new Date(currentTimeMillis + 1000 * 60 * 15);
        Date refreshTokenExpiredDate = new Date(currentTimeMillis + 1000 * 60 * 60 * 24);
        long expectedDurationMillis = refreshTokenExpiredDate.getTime() - currentTimeMillis;

        TokenDTO accessToken = new TokenDTO("access-token",  expiredDate);
        TokenDTO refreshToken = new TokenDTO("refresh-token", refreshTokenExpiredDate);

        when(jwtProvider.generateAccessToken(memberId, authorities)).thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(memberId, authorities)).thenReturn(refreshToken);

        // when
        TokenResponse response = jwtService.generateTokens(memberId, authorities);

        // then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(redisDao, times(1)).setValues(eq(RedisDao.getRtkKey(memberId)), eq("refresh-token"), argThat(duration -> duration.toMillis() <= expectedDurationMillis && duration.toMillis() > 0));
        verify(jwtProvider, times(1)).generateAccessToken(memberId, authorities);
        verify(jwtProvider, times(1)).generateRefreshToken(memberId, authorities);
    }
}

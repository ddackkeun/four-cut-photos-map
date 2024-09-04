package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRequestServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisDao redisDao;

    @InjectMocks
    private MemberRequestServiceImpl memberRequestService;


    @Test
    @DisplayName("이미 회원이 있을 때 기존의 회원 정보를 변경 후 반환")
    void login_WhenFoundRegisteredMember_ReturnAfterChangingMember() {
        // given
        KakaoUserInfoParam kakaoUserInfo = KakaoUserInfoParam.builder().id(1L).build();
        KakaoTokenResp kakaoToken = KakaoTokenResp.builder().accessToken("access token").expiresIn(3600).refreshToken("refresh token").refreshTokenExpiresIn(3600).build();
        Member existingMember = Member.builder().id(1L).kakaoId(kakaoUserInfo.getId()).status(MemberStatus.REGISTERED).build();

        when(memberRepository.findByKakaoId(kakaoUserInfo.getId())).thenReturn(Optional.of(existingMember));

        // when
        LoginResponse response = memberRequestService.login(kakaoUserInfo, kakaoToken);

        // then
        assertNotNull(response);
        assertFalse(response.getIsNewMember());
        assertEquals(MemberStatus.REGISTERED, existingMember.getStatus());
        assertEquals(kakaoToken.getRefreshToken(), existingMember.getKakaoRefreshToken());
        verify(memberRepository, times(1)).findByKakaoId(kakaoUserInfo.getId());
        verify(redisDao, times(1)).setValues(anyString(), eq("access token"), any(Duration.class));
    }

    @Test
    @DisplayName("탈퇴한 회원을 가져올 때 회원의 상태 및 정보를 변경 후 반환")
    void login_WhenFoundDeletedMember_ReturnAfterChangingMember() {
        // given
        KakaoUserInfoParam kakaoUserInfo = KakaoUserInfoParam.builder().id(12345L).build();
        KakaoTokenResp kakaoToken = KakaoTokenResp.builder().accessToken("access token").expiresIn(3600).refreshToken("refresh token").refreshTokenExpiresIn(3600).build();
        Member deletedMember = Member.builder().id(1L).kakaoId(kakaoUserInfo.getId()).status(MemberStatus.DELETED).build();

        when(memberRepository.findByKakaoId(kakaoUserInfo.getId())).thenReturn(Optional.of(deletedMember));

        // When
        LoginResponse response = memberRequestService.login(kakaoUserInfo, kakaoToken);

        // Then
        assertNotNull(response);
        assertFalse(response.getIsNewMember());
        assertEquals(MemberStatus.REGISTERED, deletedMember.getStatus());
        assertEquals(kakaoToken.getRefreshToken(), deletedMember.getKakaoRefreshToken());
        verify(memberRepository, times(1)).findByKakaoId(kakaoUserInfo.getId());
        verify(redisDao, times(1)).setValues(anyString(), eq("access token"), any(Duration.class));
    }

    @Test
    @DisplayName("기존의 회원이 없을 때 새로운 회원을 생성 및 반환")
    void login_WhenMemberNotFound_ReturnAfterRegisteringMember() {
        // given
        KakaoUserInfoParam kakaoUserInfo = KakaoUserInfoParam.builder().id(12345L).nickname("nickname").build();
        KakaoTokenResp kakaoToken = KakaoTokenResp.builder().accessToken("access token").expiresIn(3600).refreshToken("refresh token").refreshTokenExpiresIn(3600).build();
        Member newMember = Member.builder().id(1L).kakaoId(kakaoUserInfo.getId()).nickname(kakaoUserInfo.getNickname() + "1234").kakaoRefreshToken(kakaoToken.getRefreshToken()).status(MemberStatus.REGISTERED).build();

        when(memberRepository.findByKakaoId(kakaoUserInfo.getId())).thenReturn(Optional.empty());
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // when
        LoginResponse response = memberRequestService.login(kakaoUserInfo, kakaoToken);

        // then
        assertNotNull(response);
        assertTrue(response.getIsNewMember());
        assertEquals(newMember.getId(), response.getMemberResponse().getId());
        assertEquals(newMember.getNickname(), response.getMemberResponse().getNickname());
        verify(memberRepository, times(1)).findByKakaoId(kakaoUserInfo.getId());
        verify(memberRepository, atLeast(1)).existsByNickname(anyString());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(redisDao, times(1)).setValues(anyString(), eq("access token"), any(Duration.class));

    }

    @Test
    @DisplayName("카카오 정보를 사용하여 회원 등록")
    void register_RegisterMember_UsingKakaoInfo() {
        // given
        KakaoUserInfoParam kakaoUserInfo = KakaoUserInfoParam.builder().id(12345L).nickname("nickname").build();
        KakaoTokenResp kakaoToken = KakaoTokenResp.builder().accessToken("access token").expiresIn(3600).refreshToken("refresh token").refreshTokenExpiresIn(3600).build();
        Member newMember = Member.builder().id(1L).kakaoId(kakaoUserInfo.getId()).nickname(kakaoUserInfo.getNickname() + "1234").kakaoRefreshToken(kakaoToken.getRefreshToken()).status(MemberStatus.REGISTERED).build();

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        // when
        Member response = memberRequestService.register(kakaoUserInfo, kakaoToken);

        // then
        assertEquals(newMember.getId(), response.getId());
        assertEquals(kakaoUserInfo.getId(), response.getKakaoId());
        assertEquals(kakaoToken.getRefreshToken(), response.getKakaoRefreshToken());
        verify(memberRepository, atLeast(1)).existsByNickname(anyString());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("해당 닉네임을 가진 회원이 없을 경우 닉네임 반환")
    void generateUniqueNickname_WhenNicknameNotExist_ReturnThatNickname() {
        // given
        String nickname = "nickname";

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        // when
        String response = memberRequestService.generateUniqueNickname(nickname);

        // then
        assertNotNull(response);
        assertTrue(response.startsWith(nickname));
        verify(memberRepository, times(1)).existsByNickname(anyString());
    }
}
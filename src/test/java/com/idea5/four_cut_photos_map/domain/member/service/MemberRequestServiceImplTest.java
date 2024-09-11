package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.favorite.entity.Favorite;
import com.idea5.four_cut_photos_map.domain.favorite.repository.FavoriteRepository;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.review.entity.Review;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRequestServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTitleLogRepository memberTitleLogRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ReviewRepository reviewRepository;

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

    @Test
    @DisplayName("회원이 존재하고 닉네임이 중복되지 않는 경우 회원 닉네임 변경")
    void updateNickname_WhenMemberExistsAndNicknameIsNotDuplicate_UpdateSuccessfully() {
        // given
        Long memberId = 1L;
        String currentNickname = "originNickname";
        String newNickname = "newNickname";
        Member existingMember = Member.builder().id(memberId).nickname(currentNickname).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(existingMember));
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        String response = memberRequestService.updateNickname(memberId, newNickname);

        // then
        assertNotEquals(currentNickname, response);
        assertEquals(newNickname, response);
        assertEquals(newNickname, existingMember.getNickname());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository).save(existingMember);
    }

    @Test
    @DisplayName("변경하려는 닉네임이 중복일 경우 데이터베이스에서 예외를 발생")
    void updateNickname_WhenNicknameIsDuplicate_ThrowsException() {
        // given
        Long memberId = 1L;
        String currentNickname = "oldNickname";
        String duplicateNickname = "duplicateNickname";
        Member existingMember = Member.builder().id(memberId).nickname(currentNickname).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(existingMember));
        given(memberRepository.save(any(Member.class))).willThrow(new DataIntegrityViolationException("Duplicate entry for nickname"));

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> memberRequestService.updateNickname(memberId, duplicateNickname));
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원이 존재하지 않을 경우 예외 발생")
    void updateNickname_WhenMemberNotFound_ThrowsBusinessException() {
        // given
        Long memberId = 99L;
        String newNickname = "newNickname";

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

        // when & then
        BusinessException response = assertThrows(BusinessException.class, () -> memberRequestService.updateNickname(memberId, newNickname));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository, never()).save(any(Member.class));
    }


    @Test
    @DisplayName("회원 삭제 및 관련 정보 삭제")
    void deleteMember_Success1() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).build();
        MemberTitle memberTitle = MemberTitle.builder().id(1L).build();
        List<MemberTitleLog> memberTitleLogs = List.of(MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle).build());
        List<Favorite> favorites = List.of(Favorite.builder().id(1L).member(member).build());
        Review review = Review.builder().id(1L).member(member).status(ReviewStatus.REGISTERED).build();
        List<Review> reviews = List.of(review);

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
        given(redisDao.hasKey(RedisDao.getRtkKey(memberId))).willReturn(true);
        given(redisDao.hasKey(RedisDao.getKakaoAtkKey(memberId))).willReturn(true);
        given(memberTitleLogRepository.findAllByMemberId(memberId)).willReturn(memberTitleLogs);
        given(favoriteRepository.findAllByMemberId(memberId)).willReturn(favorites);
        given(reviewRepository.findAllByMemberId(memberId)).willReturn(reviews);

        // when
        memberRequestService.deleteMember(memberId);

        // then
        assertEquals(ReviewStatus.DELETED, review.getStatus());
        assertEquals(MemberStatus.DELETED, member.getStatus());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(redisDao, times(2)).hasKey(anyString());
        verify(redisDao).deleteValues(RedisDao.getRtkKey(memberId));
        verify(redisDao).deleteValues(RedisDao.getKakaoAtkKey(memberId));
        verify(memberTitleLogRepository).deleteAll(memberTitleLogs);
        verify(favoriteRepository).deleteAll(favorites);
    }

    @Test
    @DisplayName("회원이 없을 때 예외 발생")
    void testDeleteMember_MemberNotFound() {
        // given
        Long memberId = 1L;

        // Mock MemberRepository to throw exception
        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

        // when & then
        BusinessException response = assertThrows(BusinessException.class, () -> memberRequestService.deleteMember(memberId));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(redisDao, never()).hasKey(anyString());
        verify(redisDao, never()).deleteValues(anyString());
        verify(memberTitleLogRepository, never()).findAllByMemberId(anyLong());
        verify(memberTitleLogRepository, never()).deleteAll(anyList());
        verify(favoriteRepository, never()).findAllByMemberId(anyLong());
        verify(favoriteRepository, never()).deleteAll(anyList());
        verify(reviewRepository, never()).findAllByMemberId(anyLong());
    }
}
package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.entity.NicknameStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberReadServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTitleLogRepository memberTitleLogRepository;

    @InjectMocks
    MemberReadServiceImpl memberReadService;

    @Test
    @DisplayName("memberId에 해당하는 회원이 있을 때 회원 반환")
    void getMemberWithThrow_WhenFoundMember_ReturnMember() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().id(memberId).build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member response = memberReadService.getMemberWithThrow(memberId);

        // then
        assertEquals(memberId, response.getId());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("memberId에 해당하는 회원이 없을 때 예외발생")
    void getMemberWithThrow_WhenNotFoundMember_ThrowsException() {
        // given
        Long memberId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> memberReadService.getMemberWithThrow(memberId));

        // then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("확인하는 닉네임이 현재 유저의 닉네임과 동일한 경우 NicknameStatus.USED 반환")
    void checkNickname_NicknameIsSameAsCurrent_ShouldReturnStatusUsed() {
        // given
        Long memberId = 1L;
        String currentNickname = "currentNickname";
        Member existingMember = Member.builder().id(memberId).nickname(currentNickname).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(existingMember));

        // when
        NicknameCheckResponse response = memberReadService.checkNickname(memberId, currentNickname);

        // then
        assertEquals(NicknameStatus.USED.getDescription(), response.getStatus());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository, never()).existsByNickname(currentNickname);
    }

    @Test
    @DisplayName("확인하는 닉네임이 다른 유저가 사용중인 닉네임일 경우 NicknameStatus.DUPLICATED 반환")
    void checkNickname_NicknameAlreadyExists_ShouldReturnStatusDuplicated() {
        // given
        Long memberId = 1L;
        String currentNickname = "currentNickname";
        String existingNickname = "existingNickname";
        Member existingMember = Member.builder().id(memberId).nickname(currentNickname).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(existingMember));
        given(memberRepository.existsByNickname(existingNickname)).willReturn(true);

        // when
        NicknameCheckResponse response = memberReadService.checkNickname(memberId, existingNickname);

        // then
        assertEquals(NicknameStatus.DUPLICATED.getDescription(), response.getStatus());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository).existsByNickname(existingNickname);
    }

    @Test
    @DisplayName("현재 닉네임이 아니고 다른 유저가 사용하지 않는 닉네임일 경우 NicknameStatus.AVAILABLE 반환")
    void checkNickname_NicknameIsNotSameAsCurrentAndNotUsed_ShouldReturnStatusAvailable() {
        // given
        Long memberId = 1L;
        String currentNickname = "currentNickname";
        String newNickname = "nickname";
        Member existingMember = Member.builder().id(memberId).nickname(currentNickname).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(existingMember));
        given(memberRepository.existsByNickname(newNickname)).willReturn(false);

        // when
        NicknameCheckResponse response = memberReadService.checkNickname(memberId, newNickname);

        // then
        assertEquals(NicknameStatus.AVAILABLE.getDescription(), response.getStatus());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository).existsByNickname(newNickname);
    }

    @Test
    @DisplayName("회원이 없을 경우 예외를 발생")
    void checkNickname_MemberNotFound_ThrowsException() {
        // given
        Long memberId = 1L;
        String newNickname = "newNickname";

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

        // when & then
        BusinessException response = assertThrows(BusinessException.class, () -> memberReadService.checkNickname(memberId, newNickname));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberRepository, never()).existsByNickname(newNickname);
    }

    @Test
    @DisplayName("회원과 회원 칭호가 존재할 때 회원 정보 반환")
    void getMemberInfo_MemberAndMemberTitleLogExist_ReturnMemberInfoResponse() {
        // given
        Long memberId = 1L;
        Long titleCount = 5L;
        Member member = Member.builder().id(memberId).status(MemberStatus.REGISTERED).build();

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
        given(memberTitleLogRepository.countByMemberId(memberId)).willReturn(titleCount);

        // when
        MemberInfoResponse response = memberReadService.getMemberInfo(memberId);

        // then
        assertEquals(memberId, response.getId());
        assertEquals(titleCount.intValue(), response.getMemberTitleCnt());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberTitleLogRepository).countByMemberId(memberId);
    }

    @Test
    @DisplayName("회원이 없을 때 예외 발생")
    void getMemberInfo_MemberNotFound_ThrowsException() {
        // given
        Long memberId = 1L;

        given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

        // when & then
        BusinessException response = assertThrows(BusinessException.class, () -> memberReadService.getMemberInfo(memberId));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
        verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
        verify(memberTitleLogRepository, never()).countByMemberId(anyLong());
    }

}
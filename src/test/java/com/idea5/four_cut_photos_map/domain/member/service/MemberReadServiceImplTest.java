package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberReadServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

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
}
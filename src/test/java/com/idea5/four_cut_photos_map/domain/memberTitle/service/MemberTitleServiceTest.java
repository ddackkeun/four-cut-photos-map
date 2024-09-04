package com.idea5.four_cut_photos_map.domain.memberTitle.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleType;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberTitleServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTitleRepository memberTitleRepository;

    @Mock
    private MemberTitleLogRepository memberTitleLogRepository;

    @InjectMocks
    private MemberTitleService memberTitleService;

    @Test
    @DisplayName("id에 해당하는 회원칭호가 있을 때 해당 회원 칭호를 반환")
    void findById_WhenMemberTitleFound_ReturnMemberTitle() {
        // given
        Long memberId = 1L;
        MemberTitle memberTitle = MemberTitle.builder().id(memberId).build();

        when(memberTitleRepository.findById(memberId)).thenReturn(Optional.of(memberTitle));

        // when
        MemberTitle response = memberTitleService.findById(memberId);

        // then
        assertNotNull(response);
        assertEquals(memberId, response.getId());
    }

    @Test
    @DisplayName("id에 해당하는 회원 칭호가 없을 때 예외를 발생")
    void findById_WhenMemberTitleNotFound_ThrowsException() {
        // given
        Long memberId = 1L;

        when(memberTitleRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // when
        // then
        BusinessException resultException = assertThrows(BusinessException.class, () -> memberTitleService.findById(memberId));

        Assertions.assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, resultException.getErrorCode());
        verify(memberTitleRepository, times(1)).findById(anyLong());
    }


    @Test
    @DisplayName("회원과 회원칭호가 있을 때 회원 칭호 기록 생성")
    void issueMemberTitle_WhenMemberAndMemberTitleFound_CreateAndReturnMemberTitleLog() {
        // given
        Long memberId = 1L;
        Long memberTitleId = 1L;
        Member member = Member.builder().id(memberId).build();
        MemberTitle memberTitle = MemberTitle.builder().id(memberTitleId).build();
        MemberTitleLog memberTitleLog = MemberTitleLog.builder().member(member).memberTitle(memberTitle).isMain(false).build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberTitleRepository.findById(memberTitleId)).thenReturn(Optional.of(memberTitle));
        when(memberTitleLogRepository.save(any(MemberTitleLog.class))).thenReturn(memberTitleLog);

        // when
        MemberTitleLog response = memberTitleService.issueMemberTitle(memberId, memberTitleId);

        // then
        assertNotNull(response);
        assertEquals(memberId, response.getMember().getId());
        assertEquals(memberTitleId, response.getMemberTitle().getId());
    }

    @Test
    @DisplayName("id에 해당하는 회원이 없을 때 예외 발생")
    void issueMemberTitle_WhenMemberNotFound_ThrowsException() {
        // given
        Long memberId = 1L;
        Long memberTitleId = 1L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when
        // then
        BusinessException resultException = assertThrows(BusinessException.class, () -> memberTitleService.issueMemberTitle(memberId, memberTitleId));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, resultException.getErrorCode());
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberTitleRepository, never()).findById(anyLong());
        verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
    }

    @Test
    @DisplayName("id에 해당하는 회원칭호가 없을 때 예외 발생")
    void issueMemberTitle_WhenMemberTitleNotFound_ThrowsException() {
        // given
        Long memberId = 1L;
        Long memberTitleId = 1L;
        Member member = Member.builder().id(memberId).build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberTitleRepository.findById(memberTitleId)).thenReturn(Optional.empty());

        // when
        // then
        BusinessException resultException = assertThrows(BusinessException.class, () -> memberTitleService.issueMemberTitle(memberId, memberTitleId));
        assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, resultException.getErrorCode());

        verify(memberRepository, times(1)).findById(memberId);
        verify(memberTitleRepository, times(1)).findById(memberTitleId);
        verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
    }

    @Test
    @DisplayName("id에 회원과 회원칭호를 통해 뉴비 칭호 발급")
    void issueNewbieTitle_WhenMemberAndMemberTitleFound_ReturnNewbieMemberTitle() {
        // given
        Long memberId = 1L;
        MemberTitleType newbie = MemberTitleType.NEWBIE;
        Member member = Member.builder().id(memberId).build();
        MemberTitle memberTitle = MemberTitle.builder().id(newbie.getCode()).build();
        MemberTitleLog memberTitleLog = MemberTitleLog.builder().member(member).memberTitle(memberTitle).isMain(false).build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberTitleRepository.findById(newbie.getCode())).thenReturn(Optional.of(memberTitle));
        when(memberTitleLogRepository.save(any(MemberTitleLog.class))).thenReturn(memberTitleLog);

        // when
        memberTitleService.issueNewbieTitle(memberId);

        // then
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberTitleRepository, times(1)).findById(newbie.getCode());
        verify(memberTitleLogRepository, times(1)).save(any(MemberTitleLog.class));
    }

}

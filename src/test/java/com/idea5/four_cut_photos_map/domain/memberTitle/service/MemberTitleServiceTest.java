package com.idea5.four_cut_photos_map.domain.memberTitle.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleResponse;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleType;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleRepository;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
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

    @Nested
    @DisplayName("회원의 칭호들 가져오기")
    class GetMemberTitles {
        @Test
        @DisplayName("회원 칭호 기록 및 대표칭호 여부에 따라 알맞은 응답 반환")
        void getMemberTitles_Success() {
            // given
            Long memberId = 1L;
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            List<MemberTitle> allMemberTitles = List.of(memberTitle1, memberTitle2);

            MemberTitleLog memberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle1).isMain(true).build();
            List<MemberTitleLog> memberTitleLogs = List.of(memberTitleLog);

            given(memberTitleLogRepository.findAllByMemberIdWithTitles(memberId)).willReturn(memberTitleLogs);
            given(memberTitleRepository.findAll()).willReturn(allMemberTitles);

            // when
            List<MemberTitleResponse> responses = memberTitleService.getMemberTitles(memberId);

            // then
            assertEquals(allMemberTitles.size(), responses.size());

            assertEquals(memberTitle1.getId(), responses.get(0).getId());
            assertTrue(responses.get(0).getIsHolding());
            assertTrue(responses.get(0).getIsMain());

            assertEquals(memberTitle2.getId(), responses.get(1).getId());
            assertFalse(responses.get(1).getIsHolding());
            assertFalse(responses.get(1).getIsMain());
            verify(memberTitleLogRepository, times(1)).findAllByMemberIdWithTitles(memberId);
            verify(memberTitleRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("회원 칭호 기록이 없을 때 흑백응답 반환")
        void getMemberTitles_Success2() {
            // given
            Long memberId = 1L;
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            List<MemberTitle> allMemberTitles = List.of(memberTitle1, memberTitle2);

            given(memberTitleLogRepository.findAllByMemberIdWithTitles(memberId)).willReturn(List.of());
            given(memberTitleRepository.findAll()).willReturn(allMemberTitles);

            // when
            List<MemberTitleResponse> responses = memberTitleService.getMemberTitles(memberId);

            // then
            assertEquals(allMemberTitles.size(), responses.size());
            assertEquals(memberTitle1.getId(), responses.get(0).getId());
            assertFalse(responses.get(0).getIsHolding());
            assertFalse(responses.get(0).getIsMain());

            assertEquals(memberTitle2.getId(), responses.get(1).getId());
            assertFalse(responses.get(1).getIsHolding());
            assertFalse(responses.get(1).getIsMain());
            verify(memberTitleLogRepository, times(1)).findAllByMemberIdWithTitles(memberId);
            verify(memberTitleRepository, times(1)).findAll();

        }
    }

    @Nested
    @DisplayName("회원의 대표 칭호 변경")
    class UpdateMainMemberTitle {
        @Test
        @DisplayName("기존의 대표 칭호와 대표 칭호로 변경할 칭호가 존재할 때 기존 대표 칭호 해지 및 새로운 대표 칭호 설정")
        void updateMainMemberTitle_Success1() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 2L;
            Member member = Member.builder().id(memberId).build();
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            MemberTitleLog currentMainMemberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle1).isMain(true).build();
            MemberTitleLog newMainMemberTitleLog = MemberTitleLog.builder().id(2L).memberId(memberId).memberTitle(memberTitle2).isMain(false).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
            given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.of(currentMainMemberTitleLog));
            given(memberTitleLogRepository.findByMemberIdAndMemberTitleId(memberId, memberTitleId)).willReturn(Optional.of(newMainMemberTitleLog));

            // when
            memberTitleService.updateMainMemberTitle(memberId, memberTitleId);

            // then
            assertFalse(currentMainMemberTitleLog.getIsMain());
            assertTrue(newMainMemberTitleLog.getIsMain());
            assertEquals(memberTitle2.getName(), member.getMainTitleName());
            verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository, times(1)).findById(memberTitleId);
            verify(memberTitleLogRepository, times(1)).findByMemberIdAndIsMainTrue(memberId);
            verify(memberTitleLogRepository, times(1)).findByMemberIdAndMemberTitleId(memberId, memberTitleId);
        }

        @Test
        @DisplayName("기존의 대표 칭호가 존재하지 않고 대표 칭호로 변경할 칭호가 존재할 때 새로운 대표 칭호 설정")
        void updateMainMemberTitle_Success2() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 2L;
            Member member = Member.builder().id(memberId).status(MemberStatus.REGISTERED).build();
            MemberTitle memberTitle = MemberTitle.builder().id(memberTitleId).name("name").content("content").colorImageUrl("colorUrl").bwImageUrl("bwUrl").build();
            MemberTitleLog newMainMemberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle).isMain(false).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle));
            given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.empty());
            given(memberTitleLogRepository.findByMemberIdAndMemberTitleId(memberId, memberTitleId)).willReturn(Optional.of(newMainMemberTitleLog));

            // when
            memberTitleService.updateMainMemberTitle(memberId, memberTitleId);

            // then
            assertTrue(newMainMemberTitleLog.getIsMain());
            assertEquals(memberTitle.getName(), member.getMainTitleName());
            verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository, times(1)).findById(memberTitleId);
            verify(memberTitleLogRepository, times(1)).findByMemberIdAndIsMainTrue(memberId);
            verify(memberTitleLogRepository, times(1)).findByMemberIdAndMemberTitleId(memberId, memberTitleId);
        }

        @Test
        @DisplayName("memberId에 해당하는 회원이 없을 때 예외 발생")
        void updateMainMemberTitle_Fail1() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 1L;

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

            // when & then
            BusinessException response = assertThrows(BusinessException.class, () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository, never()).findById(anyLong());
            verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            verify(memberTitleLogRepository, never()).findByMemberIdAndMemberTitleId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("memberTitleId에 해당하는 회원 칭호가 없을 때 예외 발생")
        void updateMainMemberTitle_Fail2() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 99L;
            Member member = Member.builder().id(memberId).status(MemberStatus.REGISTERED).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.empty());

            // when & then
            BusinessException response = assertThrows(BusinessException.class, () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, response.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(memberTitleId);
            verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            verify(memberTitleLogRepository, never()).findByMemberIdAndMemberTitleId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("대표 칭호로 설정하려는 회원칭호가 이미 대표 칭호로 지정되어 있는 경우 예외발생")
        void updateMainMemberTitle_Fail3() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 1L;
            Member member = Member.builder().id(memberId).status(MemberStatus.REGISTERED).build();
            MemberTitle memberTitle = MemberTitle.builder().id(memberTitleId).name("name").content("content").colorImageUrl("colorUrl").bwImageUrl("bwUrl").build();
            MemberTitleLog currentMainMemberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle).isMain(true).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle));
            given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.of(currentMainMemberTitleLog));

            // when & then
            BusinessException response = assertThrows(BusinessException.class, () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.DUPLICATE_MAIN_MEMBER_TITLE, response.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(memberTitleId);
            verify(memberTitleLogRepository).findByMemberIdAndIsMainTrue(memberId);
            verify(memberTitleLogRepository, never()).findByMemberIdAndMemberTitleId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("대표 칭호로 설정하려는 회원칭호를 회원이 가지고 있지 않은 경우 예외 발생")
        void updateMainMemberTitle_Fail4() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 2L;
            Member member = Member.builder().id(memberId).status(MemberStatus.REGISTERED).build();
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            MemberTitleLog currentMainMemberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle1).isMain(true).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
            given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.of(currentMainMemberTitleLog));
            given(memberTitleLogRepository.findByMemberIdAndMemberTitleId(memberId, memberTitleId)).willReturn(Optional.empty());

            // when & then
            BusinessException response = assertThrows(BusinessException.class, () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.MEMBER_TITLE_NOT_HAD, response.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(memberTitleId);
            verify(memberTitleLogRepository).findByMemberIdAndIsMainTrue(memberId);
            verify(memberTitleLogRepository).findByMemberIdAndMemberTitleId(memberId, memberTitleId);
        }
    }

    @Nested
    @DisplayName("모든 회원 칭호를 가져옴")
    class FindAllMemberTitle {
        @Test
        @DisplayName("회원 칭호가 있을 경우 회원칭호 리스트 반환")
        void findAllMemberTitle_Success1() {
            // given
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            List<MemberTitle> memberTitles = List.of(memberTitle1, memberTitle2);

            given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(memberTitles);

            // when
            List<MemberTitle> responses = memberTitleService.findAllMemberTitle();

            // then
            assertEquals(memberTitles.size(), responses.size());
            assertEquals(memberTitle1.getId(), responses.get(0).getId());
            assertEquals(memberTitle2.getId(), responses.get(1).getId());
            verify(memberTitleRepository).findAllByOrderByIdAsc();
        }

        @Test
        @DisplayName("회원 칭호가 없을 때 빈 회원칭호 리스트 반환")
        void findAllMemberTitle_Success2() {
            // given
            List<MemberTitle> memberTitles = List.of();

            given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(memberTitles);

            // when
            List<MemberTitle> responses = memberTitleService.findAllMemberTitle();

            // then
            assertEquals(0, responses.size());
            verify(memberTitleRepository).findAllByOrderByIdAsc();
        }
    }


    @Nested
    @DisplayName("회원이 가지고 있는 회원 칭호기록의 MemberTitleId 리스트를 반환")
    class GetMemberTitleIds {
        @Test
        @DisplayName("회원칭호 기록이 있을 때 MemberTitleId 리스트 반환")
        void getMemberTitleIds_Success1() {
            // given
            Long memberId = 1L;
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            MemberTitleLog memberTitleLog1 = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle1).isMain(false).build();
            MemberTitleLog memberTitleLog2 = MemberTitleLog.builder().id(2L).memberId(memberId).memberTitle(memberTitle2).isMain(false).build();
            List<MemberTitleLog> memberTitleLogs = List.of(memberTitleLog1, memberTitleLog2);

            given(memberTitleLogRepository.findAllByMemberIdWithTitles(memberId)).willReturn(memberTitleLogs);

            // when
            Set<Long> response = memberTitleService.getMemberTitleIds(memberId);

            // then
            assertEquals(memberTitleLogs.size(), response.size());
            assertTrue(response.contains(memberTitle1.getId()));
            assertTrue(response.contains(memberTitle2.getId()));
            verify(memberTitleLogRepository).findAllByMemberIdWithTitles(memberId);
        }

        @Test
        @DisplayName("회원칭호 기록이 없을 때 빈 리스트 반환")
        void getMemberTitleIds_Success2() {
            // given
            Long memberId = 1L;

            given(memberTitleLogRepository.findAllByMemberIdWithTitles(memberId)).willReturn(Collections.emptyList());

            // when
            Set<Long> response = memberTitleService.getMemberTitleIds(memberId);

            // then
            assertEquals(0, response.size());
            verify(memberTitleLogRepository).findAllByMemberIdWithTitles(memberId);
        }
    }

    @Nested
    @DisplayName("회원 칭호 발행")
    class IssueMemberTitle {
        @Test
        @DisplayName("회원과 회원칭호가 있을 때 회원 칭호 기록 생성")
        void issueMemberTitle_WhenMemberAndMemberTitleFound_CreateAndReturnMemberTitleLog() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 1L;
            Member member = Member.builder().id(memberId).build();
            MemberTitle memberTitle = MemberTitle.builder().id(memberTitleId).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle));
            given(memberTitleLogRepository.save(any(MemberTitleLog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberTitleLog response = memberTitleService.issueMemberTitle(memberId, memberTitleId);

            // then
            assertNotNull(response);
            assertEquals(memberId, response.getMemberId());
            assertEquals(memberTitleId, response.getMemberTitle().getId());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(memberTitleId);
            verify(memberTitleLogRepository).save(any(MemberTitleLog.class));
        }

        @Test
        @DisplayName("memberId에 해당하는 회원이 없을 때 예외 발생")
        void issueMemberTitle_WhenMemberNotFound_ThrowsException() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 1L;

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

            // when
            // then
            BusinessException resultException = assertThrows(BusinessException.class, () -> memberTitleService.issueMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, resultException.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository, never()).findById(anyLong());
            verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
        }

        @Test
        @DisplayName("memberTitleId에 해당하는 회원칭호가 없을 때 예외 발생")
        void issueMemberTitle_WhenMemberTitleNotFound_ThrowsException() {
            // given
            Long memberId = 1L;
            Long memberTitleId = 1L;
            Member member = Member.builder().id(memberId).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.empty());

            // when & then
            BusinessException resultException = assertThrows(BusinessException.class, () -> memberTitleService.issueMemberTitle(memberId, memberTitleId));
            assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, resultException.getErrorCode());
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(memberTitleId);
            verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
        }
    }

    @Nested
    @DisplayName("뉴비 회원칭호 발행")
    class IssueNewbieTitle {
        @Test
        @DisplayName("id에 회원과 회원칭호를 통해 뉴비 칭호 발급")
        void issueNewbieTitle_WhenMemberAndMemberTitleFound_ReturnNewbieMemberTitle() {
            // given
            Long memberId = 1L;
            MemberTitleType newbie = MemberTitleType.NEWBIE;
            Member member = Member.builder().id(memberId).build();
            MemberTitle memberTitle = MemberTitle.builder().id(newbie.getCode()).build();

            given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
            given(memberTitleRepository.findById(newbie.getCode())).willReturn(Optional.of(memberTitle));
            given(memberTitleLogRepository.save(any(MemberTitleLog.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            memberTitleService.issueNewbieTitle(memberId);

            // then
            verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
            verify(memberTitleRepository).findById(newbie.getCode());
            verify(memberTitleLogRepository).save(any(MemberTitleLog.class));
        }
    }

}

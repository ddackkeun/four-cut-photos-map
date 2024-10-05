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
import org.junit.jupiter.api.BeforeEach;
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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberTitleServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTitleRepository memberTitleRepository;

    @Mock
    private MemberTitleLogRepository memberTitleLogRepository;

    @InjectMocks
    private MemberTitleServiceImpl memberTitleService;

    @Nested
    @DisplayName("회원의 칭호들 가져오기")
    class GetMemberTitles {
        private MemberTitle memberTitle1, memberTitle2, memberTitle3;
        private List<MemberTitle> allMemberTitles;

        @BeforeEach
        void setUp() {
            memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            memberTitle3 = MemberTitle.builder().id(3L).name("name3").content("content3").colorImageUrl("colorUrl3").bwImageUrl("bwUrl3").build();
            allMemberTitles = List.of(memberTitle1, memberTitle2, memberTitle3);
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("회원 칭호 기록 및 대표칭호 여부에 따라 알맞은 응답 반환")
            void getMemberTitles_Success() {
                // given
                Long memberId = 1L;
                MemberTitleLog memberTitleLog = MemberTitleLog.builder().id(1L).memberId(memberId).memberTitle(memberTitle1).isMain(true).build();
                List<MemberTitleLog> memberTitleLogs = List.of(memberTitleLog);

                given(memberTitleLogRepository.findAllByMemberId(memberId)).willReturn(memberTitleLogs);
                given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(allMemberTitles);

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
                assertEquals(memberTitle3.getId(), responses.get(2).getId());
                assertFalse(responses.get(2).getIsHolding());
                assertFalse(responses.get(2).getIsMain());

                verify(memberTitleLogRepository, times(1)).findAllByMemberId(memberId);
                verify(memberTitleRepository, times(1)).findAllByOrderByIdAsc();
            }

            @Test
            @DisplayName("회원 칭호 기록이 없을 때 흑백응답 반환")
            void getMemberTitles_Success2() {
                // given
                Long memberId = 1L;

                given(memberTitleLogRepository.findAllByMemberId(memberId)).willReturn(Collections.emptyList());
                given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(allMemberTitles);

                // when
                List<MemberTitleResponse> responses = memberTitleService.getMemberTitles(memberId);

                // then
                assertEquals(allMemberTitles.size(), responses.size());
                IntStream.range(0, allMemberTitles.size())
                        .forEach(i -> {
                            MemberTitleResponse response = responses.get(i);
                            MemberTitle memberTitle = allMemberTitles.get(i);

                            assertEquals(memberTitle.getId(), response.getId());
                            assertFalse(response.getIsHolding());
                            assertFalse(response.getIsMain());
                        });
                verify(memberTitleLogRepository, times(1)).findAllByMemberId(memberId);
                verify(memberTitleRepository, times(1)).findAllByOrderByIdAsc();
            }
        }

    }

    @Nested
    @DisplayName("회원의 대표 칭호 변경")
    class UpdateMainMemberTitle {
        private Member member;
        private MemberTitle memberTitle1, memberTitle2;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).status(MemberStatus.REGISTERED).build();
            memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("기존의 대표 칭호와 대표 칭호로 변경할 칭호가 존재할 때 기존 대표 칭호 해지 및 새로운 대표 칭호 설정")
            void updateMainMemberTitle_Success1() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 2L;

                MemberTitleLog currentMainMemberTitleLog = MemberTitleLog.builder().id(100L).memberId(memberId).memberTitle(memberTitle1).isMain(true).build();
                MemberTitleLog newMainMemberTitleLog = MemberTitleLog.builder().id(200L).memberId(memberId).memberTitle(memberTitle2).isMain(false).build();

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
                given(memberTitleLogRepository.findByMemberIdAndMemberTitle(memberId, memberTitle2)).willReturn(Optional.of(newMainMemberTitleLog));
                given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.of(currentMainMemberTitleLog));

                // when
                memberTitleService.updateMainMemberTitle(memberId, memberTitleId);

                // then
                assertFalse(currentMainMemberTitleLog.getIsMain());
                assertTrue(newMainMemberTitleLog.getIsMain());
                assertEquals(memberTitle2.getName(), member.getMainTitleName());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, times(1)).findById(memberTitleId);
                verify(memberTitleLogRepository, times(1)).findByMemberIdAndMemberTitle(memberId, memberTitle2);
                verify(memberTitleLogRepository, times(1)).findByMemberIdAndIsMainTrue(memberId);
            }

            @Test
            @DisplayName("기존의 대표 칭호가 존재하지 않고 대표 칭호로 변경할 칭호가 존재할 때 새로운 대표 칭호 설정")
            void updateMainMemberTitle_Success2() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 2L;

                MemberTitleLog newMainMemberTitleLog = MemberTitleLog.builder().id(200L).memberId(memberId).memberTitle(memberTitle2).isMain(false).build();

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
                given(memberTitleLogRepository.findByMemberIdAndMemberTitle(memberId, memberTitle2)).willReturn(Optional.of(newMainMemberTitleLog));
                given(memberTitleLogRepository.findByMemberIdAndIsMainTrue(memberId)).willReturn(Optional.empty());

                // when
                memberTitleService.updateMainMemberTitle(memberId, memberTitleId);

                // then
                assertTrue(newMainMemberTitleLog.getIsMain());
                assertEquals(memberTitle2.getName(), member.getMainTitleName());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, times(1)).findById(memberTitleId);
                verify(memberTitleLogRepository, times(1)).findByMemberIdAndMemberTitle(memberId, memberTitle2);
                verify(memberTitleLogRepository, times(1)).findByMemberIdAndIsMainTrue(memberId);
            }

        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("memberId에 해당하는 회원이 없을 때 예외 발생")
            void updateMainMemberTitle_Fail1() {
                // given
                Long memberId = 1000L;
                Long memberTitleId = 1L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

                // when & then
                BusinessException response = assertThrows(BusinessException.class,
                        () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
                assertEquals(ErrorCode.MEMBER_NOT_FOUND, response.getErrorCode());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, never()).findById(anyLong());
                verify(memberTitleLogRepository, never()).findByMemberIdAndMemberTitle(anyLong(), any(MemberTitle.class));
                verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            }

            @Test
            @DisplayName("memberTitleId에 해당하는 회원 칭호가 없을 때 예외 발생")
            void updateMainMemberTitle_Fail2() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 1000L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.empty());

                // when & then
                BusinessException response = assertThrows(BusinessException.class,
                        () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
                assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, response.getErrorCode());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, times(1)).findById(memberTitleId);
                verify(memberTitleLogRepository, never()).findByMemberIdAndMemberTitle(anyLong(), any(MemberTitle.class));
                verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            }

            @Test
            @DisplayName("대표 칭호로 설정하려는 회원칭호를 회원이 가지고 있지 않은 경우 예외 발생")
            void updateMainMemberTitle_Fail3() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 2L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
                given(memberTitleLogRepository.findByMemberIdAndMemberTitle(memberId, memberTitle2)).willReturn(Optional.empty());

                // when & then
                BusinessException response = assertThrows(BusinessException.class,
                        () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
                assertEquals(ErrorCode.MEMBER_TITLE_NOT_HAD, response.getErrorCode());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, times(1)).findById(memberTitleId);
                verify(memberTitleLogRepository, times(1)).findByMemberIdAndMemberTitle(memberId, memberTitle2);
                verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            }

            @Test
            @DisplayName("대표 칭호로 설정하려는 회원칭호가 이미 대표 칭호로 지정되어 있는 경우 예외발생")
            void updateMainMemberTitle_Fail4() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 2L;

                MemberTitleLog newMainMemberTitleLog = MemberTitleLog.builder().id(200L).memberId(memberId).memberTitle(memberTitle2).isMain(true).build();

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle2));
                given(memberTitleLogRepository.findByMemberIdAndMemberTitle(memberId, memberTitle2)).willReturn(Optional.of(newMainMemberTitleLog));

                // when & then
                BusinessException response = assertThrows(BusinessException.class,
                        () -> memberTitleService.updateMainMemberTitle(memberId, memberTitleId));
                assertEquals(ErrorCode.DUPLICATE_MAIN_MEMBER_TITLE, response.getErrorCode());
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository).findById(memberTitleId);
                verify(memberTitleLogRepository).findByMemberIdAndMemberTitle(memberId, memberTitle2);
                verify(memberTitleLogRepository, never()).findByMemberIdAndIsMainTrue(anyLong());
            }
        }
    }

    @Nested
    @DisplayName("회원 칭호 기록 발행하기")
    class IssueMemberTitleLog {
        private Member member;
        private MemberTitle memberTitle;

        @BeforeEach
        void setUp() {
            member = Member.builder().id(1L).status(MemberStatus.REGISTERED).build();
            memberTitle = MemberTitle.builder().id(1L).build();
        }

        @Nested
        @DisplayName("성공 테스트")
        class SuccessCase {
            @Test
            @DisplayName("회원과 회원칭호가 있을 때 회원 칭호 기록 생성")
            void issueMemberTitleLog_Success1() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 1L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.of(memberTitle));

                // when
                memberTitleService.issueMemberTitleLog(memberId, memberTitleId);

                // then
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository).findById(memberTitleId);
                verify(memberTitleLogRepository).save(any(MemberTitleLog.class));
            }
        }

        @Nested
        @DisplayName("실패 테스트")
        class FailCase {
            @Test
            @DisplayName("memberId에 해당하는 회원이 없을 때 예외 발생")
            void issueMemberTitleLog_Fail1() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 1L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.empty());

                // when
                // then
                BusinessException resultException = assertThrows(BusinessException.class,
                        () -> memberTitleService.issueMemberTitleLog(memberId, memberTitleId));
                assertEquals(ErrorCode.MEMBER_NOT_FOUND, resultException.getErrorCode());
                verify(memberRepository, times(1)).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository, never()).findById(anyLong());
                verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
            }

            @Test
            @DisplayName("memberTitleId에 해당하는 회원칭호가 없을 때 예외 발생")
            void issueMemberTitleLog_Fail2() {
                // given
                Long memberId = 1L;
                Long memberTitleId = 1L;

                given(memberRepository.findByIdAndStatus(memberId, MemberStatus.REGISTERED)).willReturn(Optional.of(member));
                given(memberTitleRepository.findById(memberTitleId)).willReturn(Optional.empty());

                // when & then
                BusinessException resultException = assertThrows(BusinessException.class,
                        () -> memberTitleService.issueMemberTitleLog(memberId, memberTitleId));
                assertEquals(ErrorCode.MEMBER_TITLE_NOT_FOUND, resultException.getErrorCode());
                verify(memberRepository).findByIdAndStatus(memberId, MemberStatus.REGISTERED);
                verify(memberTitleRepository).findById(memberTitleId);
                verify(memberTitleLogRepository, never()).save(any(MemberTitleLog.class));
            }
        }
    }

    @Nested
    @DisplayName("모든 회원 칭호를 가져옴")
    class GetAllMemberTitles {
        @Test
        @DisplayName("회원 칭호가 있을 경우 회원칭호 리스트 반환")
        void getAllMemberTitles_Success1() {
            // given
            MemberTitle memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            MemberTitle memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            List<MemberTitle> memberTitles = List.of(memberTitle1, memberTitle2);

            given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(memberTitles);

            // when
            List<MemberTitle> responses = memberTitleService.getAllMemberTitles();

            // then
            assertEquals(memberTitles.size(), responses.size());
            assertEquals(memberTitle1.getId(), responses.get(0).getId());
            assertEquals(memberTitle2.getId(), responses.get(1).getId());
            verify(memberTitleRepository, times(1)).findAllByOrderByIdAsc();
        }

        @Test
        @DisplayName("회원 칭호가 없을 때 빈 회원칭호 리스트 반환")
        void getAllMemberTitles_Success2() {
            // given
            given(memberTitleRepository.findAllByOrderByIdAsc()).willReturn(Collections.emptyList());

            // when
            List<MemberTitle> responses = memberTitleService.getAllMemberTitles();

            // then
            assertTrue(responses.isEmpty());
            verify(memberTitleRepository, times(1)).findAllByOrderByIdAsc();
        }
    }


    @Nested
    @DisplayName("회원이 가지고 있는 회원 칭호기록의 MemberTitleId 리스트를 반환")
    class GetMemberTitleIds {
        private MemberTitle memberTitle1, memberTitle2, memberTitle3;

        @BeforeEach
        void setUp() {
            memberTitle1 = MemberTitle.builder().id(1L).name("name1").content("content1").colorImageUrl("colorUrl1").bwImageUrl("bwUrl1").build();
            memberTitle2 = MemberTitle.builder().id(2L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
            memberTitle3 = MemberTitle.builder().id(3L).name("name2").content("content2").colorImageUrl("colorUrl2").bwImageUrl("bwUrl2").build();
        }
        @Test
        @DisplayName("회원칭호 기록이 있을 때 MemberTitleId 리스트 반환")
        void getMemberTitleIds_Success1() {
            // given
            Long memberId = 1L;
            MemberTitleLog memberTitleLog1 = MemberTitleLog.builder().id(1000L).memberId(memberId).memberTitle(memberTitle1).isMain(false).build();
            MemberTitleLog memberTitleLog2 = MemberTitleLog.builder().id(2000L).memberId(memberId).memberTitle(memberTitle2).isMain(false).build();
            MemberTitleLog memberTitleLog3 = MemberTitleLog.builder().id(3000L).memberId(memberId).memberTitle(memberTitle3).isMain(false).build();
            List<MemberTitleLog> memberTitleLogs = List.of(memberTitleLog1, memberTitleLog2, memberTitleLog3);

            given(memberTitleLogRepository.findAllByMemberId(memberId)).willReturn(memberTitleLogs);

            // when
            Set<Long> response = memberTitleService.getMemberTitleIds(memberId);

            // then
            assertEquals(memberTitleLogs.size(), response.size());
            IntStream.range(0, memberTitleLogs.size())
                            .forEach(i -> {
                                MemberTitleLog memberTitleLog = memberTitleLogs.get(i);
                                assertTrue(response.contains(memberTitleLog.getMemberTitle().getId()));
                            });
            verify(memberTitleLogRepository).findAllByMemberId(memberId);
        }

        @Test
        @DisplayName("회원칭호 기록이 없을 때 빈 리스트 반환")
        void getMemberTitleIds_Success2() {
            // given
            Long memberId = 1L;

            given(memberTitleLogRepository.findAllByMemberId(memberId)).willReturn(Collections.emptyList());

            // when
            Set<Long> response = memberTitleService.getMemberTitleIds(memberId);

            // then
            assertTrue(response.isEmpty());
            verify(memberTitleLogRepository).findAllByMemberId(memberId);
        }
    }
}

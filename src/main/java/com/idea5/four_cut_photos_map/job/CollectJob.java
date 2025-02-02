package com.idea5.four_cut_photos_map.job;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.service.MemberService;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleType;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @See <a href="https://github.com/hayeon17kim/TIL/blob/master/project-badge-system.md">배지 시스템 참고</a>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CollectJob {
    private final MemberService memberService;
    private final MemberTitleService memberTitleService;
    private final CollectService collectService;

    // 초 분 시 일 월 요일
    @Scheduled(cron = "0 0 2 * * *")    // 매일 02시 실행
    @Transactional
    public void add() {
        // 인증된 API 요청 -> 로그 남기자
        // TODO: 전체 회원 말고 오늘 요청보낸 회원만
        // 회원, 회원칭호 전체조회
        List<MemberTitle> memberTitles = memberTitleService.getAllMemberTitles();
        List<Member> members = memberService.findAll();

//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();

        // 회원별로 각 칭호 부여하기
        for(Member member : members) {
            Set<Long> collectedMemberTitleIds = memberTitleService.getMemberTitleIds(member.getId());
            for(MemberTitle memberTitle : memberTitles) {
                // 1. 회원이 보유한 회원칭호는 패스
                if(collectedMemberTitleIds.contains(memberTitle.getId()))
                    continue;
                // 2. 회원이 보유하지 않은 회원칭호는 부여기준 검사 -> 부여
                if(collectService.canGiveMemberTitle(member, memberTitle)) {
                    // 회원가입 칭호와 다른 칭호를 같은 날에 부여 받는 경우 회원가입 칭호를 대표 칭호로 설정
                    boolean isMain = memberTitle.getId().equals(MemberTitleType.NEWBIE.getCode());
                    collectService.addMemberTitle(member, memberTitle, isMain);
                }
            }
        }
//        stopWatch.stop();
//        log.info(stopWatch.prettyPrint());
//        log.info(String.valueOf(stopWatch.getTotalTimeSeconds()));
    }
}

package com.idea5.four_cut_photos_map.domain.memberTitle.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleInfoResp;
import com.idea5.four_cut_photos_map.domain.memberTitle.dto.response.MemberTitleResp;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitle;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberTitleService {
    private final MemberTitleRepository memberTitleRepository;
    private final MemberTitleLogRepository memberTitleLogRepository;
    private final MemberRepository memberRepository;

    public MemberTitle findById(Long id) {
        return memberTitleRepository.findById(id).orElseThrow(() -> {
            throw new RuntimeException("memberTitle 없음");
        });
    }

    // 회원 칭호 정보 조회
    public MemberTitleInfoResp getMemberTitleInfo(Long id) {
        MemberTitle memberTitle = findById(id);
        return MemberTitleInfoResp.toDto(memberTitle);
    }

    // TODO : 로직 리팩토링
    public List<MemberTitleResp> getMemberTitles(Long memberId) {
        // 1. 전체 칭호
        log.info("----Before memberTitleRepository.findAllByOrderByIdAsc()----");
        List<MemberTitle> memberTitles = memberTitleRepository.findAllByOrderByIdAsc();
        // 2. 회원이 갖고 있는 칭호
        log.info("----Before memberTitleLogRepository.findAllByMemberIdOrderByIdAsc(memberId)----");
        List<MemberTitleLog> myMemberTitleLogs = memberTitleLogRepository.findAllByMemberIdOrderByIdAsc(memberId);
        List<MemberTitle> myMemberTitles = myMemberTitleLogs.stream()
                .map(memberTitleLog -> memberTitleLog.getMemberTitle())
                .collect(Collectors.toList());
        // 3. DTO 변환해서 담기
        List<MemberTitleResp> memberTitleResps = new ArrayList<>();
        for(MemberTitle mt : memberTitles) {
            MemberTitleResp memberTitleResp = MemberTitleResp.toDto(mt);
            // 내가 갖고 있는 칭호는 상태를 y 로 바꾸기
            if(myMemberTitles.contains(mt)) {
                memberTitleResp.setStatus('y');
            }
            memberTitleResps.add(memberTitleResp);
        }
        return memberTitleResps;
    }

    public List<MemberTitleLog> findByMember(Member member) {
        return memberTitleLogRepository.findByMember(member);
    }

    @Transactional
    public void addMemberTitle(Member member, Long memberTitleId, Boolean isMain) {
        MemberTitle memberTitle = findById(memberTitleId);
        memberTitleLogRepository.save(new MemberTitleLog(member, memberTitle, isMain));
    }

    // 회원 대표 칭호 수정
    @Transactional
    public void updateMainMemberTitle(Member member, Long memberTitleId) {
        // 1. 기존 회원의 대표 칭호 해제
        log.info("----Before memberTitleLogRepository.findByMemberIdAndIsMainTrue()----");
        MemberTitleLog memberTitleLog = memberTitleLogRepository.findByMemberAndIsMainTrue(member).orElse(null);
        memberTitleLog.cancelMain();
        // 2. 새로운 칭호로 대표 칭호 설정
        log.info("----Before memberTitleLogRepository.findByMemberIdAndMemberTitleId()----");
        MemberTitleLog newMemberTitleLog = memberTitleLogRepository.findByMemberAndMemberTitleId(member, memberTitleId)
                .orElseThrow(() -> {
                    throw new RuntimeException("해당 회원이 칭호를 소유하고 있지 않습니다.");
                });
        newMemberTitleLog.registerMain();
    }
}

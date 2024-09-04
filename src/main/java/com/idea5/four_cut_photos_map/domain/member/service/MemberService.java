package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.favorite.service.FavoriteService;
import com.idea5.four_cut_photos_map.domain.member.dto.request.MemberUpdateReq;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberTitleInfoResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberWithdrawlResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.NicknameCheckResp;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.mapper.MemberMapper;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import com.idea5.four_cut_photos_map.domain.review.service.ReviewRequestService;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RedisDao redisDao;
    private final MemberTitleService memberTitleService;
    private final FavoriteService favoriteService;
    private final ReviewRequestService reviewRequestService;

    public Member findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    // 회원 id 로 기본 정보 조회
    public MemberInfoResp getMemberInfo(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
        );
        MemberTitleInfoResp memberTitleInfo = getMemberTitleInfo(member);
        return MemberInfoResp.toDto(member, memberTitleInfo.getMainMemberTitle(), memberTitleInfo.getMemberTitleCnt());
    }

    public MemberTitleInfoResp getMemberTitleInfo(Member member) {
        log.info("----Before memberTitleService.findByMember(member)----");
        List<MemberTitleLog> memberTitleLogs = memberTitleService.findByMember(member);
        String mainMemberTitle = "";
        // 대표 칭호 조회(회원가입 후 바로 칭호가 부여되지 않기 때문에 회원가입 당일에는 대표 칭호가 없을 수 있음)
        for(MemberTitleLog memberTitleLog : memberTitleLogs) {
            if(memberTitleLog.getIsMain()) {
                log.info("----Before memberTitleLog.getMemberTitleName()----");
                mainMemberTitle = memberTitleLog.getMemberTitleName();
                break;
            }
        }
        return new MemberTitleInfoResp(memberTitleLogs.size(), mainMemberTitle);
    }

    // 서비스 로그아웃
    public void logout(Long id) {
        // Redis 에 회원의 kakaoAccessToken, refreshToken 이 있으면 삭제
        if(redisDao.hasKey(RedisDao.getRtkKey(id)))
            redisDao.deleteValues(RedisDao.getRtkKey(id));
        if(redisDao.hasKey(RedisDao.getKakaoAtkKey(id)))
            redisDao.deleteValues(RedisDao.getKakaoAtkKey(id));
    }

    // 회원 삭제
    @Transactional
    public MemberWithdrawlResp deleteMember(Long id) {
        // 1. 회원의 kakaoAccessToken, refreshToken 이 있으면 삭제
        if (redisDao.hasKey(RedisDao.getRtkKey(id)))
            redisDao.deleteValues(RedisDao.getRtkKey(id));
        if(redisDao.hasKey(RedisDao.getKakaoAtkKey(id)))
            redisDao.deleteValues(RedisDao.getKakaoAtkKey(id));
        // 2. Member 삭제하기 전 Member 를 참조하고 있는 엔티티(MemberTitleLog, Favorite, Review) 먼저 삭제하기
        memberTitleService.deleteByMemberId(id);
        favoriteService.deleteByMemberId(id);
        reviewRequestService.deleteAllReviewsFromMember(id);
        // 3. DB 에서 회원 삭제
        memberRepository.deleteById(id);
        return new MemberWithdrawlResp(id);
    }

    // 회원 닉네임 수정
    @Transactional
    public void updateNickname(Long id, MemberUpdateReq memberUpdateReq) {
        // 닉네임 중복 검사
        if(memberRepository.existsByNickname(memberUpdateReq.getNickname()))
            throw new BusinessException(ErrorCode.DUPLICATE_MEMBER_NICKNAME);
        Member member = findById(id);
        member.updateNickname(memberUpdateReq);
    }

    // 회원 대표칭호 수정
    @Transactional
    public void updateMainMemberTitle(Member member, Long memberTitleId) {
        memberTitleService.updateMainMemberTitle(member, memberTitleId);
    }

    // 회원 Kakao Access Token 조회
    public String getKakaoAccessToken(Long id) {
        return redisDao.getValues(RedisDao.getKakaoAtkKey(id));
    }

    // 회원 Kakao Refresh Token 조회
    public String getKakaoRefreshToken(Long id) {
        return findById(id).getKakaoRefreshToken();
    }

    public List<Member> findAll() {
        return memberRepository.findAllByOrderByIdAsc();
    }

    public NicknameCheckResp checkDuplicatedNickname(String nickname) {
        boolean status = (memberRepository.existsByNickname(nickname) == false);
        return new NicknameCheckResp(nickname, status);
    }

}

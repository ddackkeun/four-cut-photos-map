package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.favorite.service.FavoriteService;
import com.idea5.four_cut_photos_map.domain.member.dto.request.MemberUpdateReq;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberInfoResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberTitleInfoResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberWithdrawlResp;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.service.MemberTitleService;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.Util;
import com.idea5.four_cut_photos_map.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RedisDao redisDao;
    private final MemberTitleService memberTitleService;
    private final FavoriteService favoriteService;

    // 회원 가져오기
    @Transactional
    public Member getMember(KakaoUserInfoParam kakaoUserInfoParam, KakaoTokenResp kakaoTokenResp) {
        // Unique 한 값인 kakaoId 로 조회
        Member member = memberRepository.findByKakaoId(kakaoUserInfoParam.getId()).orElse(null);
        if(member != null) {
            // DB 에 Refresh Token 갱신
            member.updateKakaoRefreshToken(kakaoTokenResp.getRefreshToken());
        } else {
            // 신규 사용자인 경우 회원가입
            // 유니크한 닉네임 설정
            kakaoUserInfoParam.updateNickname(generateUniqueNickname(kakaoUserInfoParam.getNickname()));
            member = KakaoUserInfoParam.toEntity(kakaoUserInfoParam);
            member.updateKakaoRefreshToken(kakaoTokenResp.getRefreshToken());
            memberRepository.save(member);
        }
        // redis 에 Access Token 저장
        redisDao.setValues(
                RedisDao.getKakaoAtkKey(member.getId()),
                kakaoTokenResp.getAccessToken(),
                Duration.ofSeconds(kakaoTokenResp.getExpiresIn()));
        return member;
    }

    // 유니크한 닉네임 생성
    public String generateUniqueNickname(String nickname) {
        while(true) {
            String newNickname = nickname + Util.generateRandomNumber(4);
            if(memberRepository.existsByNickname(newNickname)) continue;
            return newNickname;
        }
    }

    public Member findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    // 회원 id 로 기본 정보 조회
    public MemberInfoResp getMemberInfo(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException());
        MemberTitleInfoResp memberTitleInfo = getMemberTitleInfo(member);
        return MemberInfoResp.toDto(member, memberTitleInfo.getMainMemberTitle(), memberTitleInfo.getMemberTitleCnt());
    }

    public MemberTitleInfoResp getMemberTitleInfo(Member member) {
        log.info("----Before memberTitleService.findByMember(member)----");
        List<MemberTitleLog> memberTitleLogs = memberTitleService.findByMember(member);
        String mainMemberTitle = null;
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

    // 서비스 로그아웃(accessToken 무효화)
    public void logout(String accessToken) {
        // 1. 회원의 refreshToken 이 있으면 삭제
        Long memberId = jwtProvider.getId(accessToken);
        if(redisDao.hasKey(memberId.toString())) {
            redisDao.deleteValues(memberId.toString());
        }
        // 2. redis 에 해당 accessToken 블랙리스트로 등록
        redisDao.setValues(
                RedisDao.getBlackListAtkKey(accessToken),
                "logout",
                Duration.ofMillis(jwtProvider.getExpiration(accessToken)));
    }

    // 회원 삭제
    @Transactional
    public MemberWithdrawlResp deleteMember(Long id, String accessToken) {
        // 1. 회원의 refreshToken 이 있으면 삭제
        if (redisDao.hasKey(RedisDao.getRtkKey(id))) {
            redisDao.deleteValues(RedisDao.getRtkKey(id));
        }
        // 2. redis 에 해당 accessToken 블랙리스트로 등록
        redisDao.setValues(
                RedisDao.getBlackListAtkKey(accessToken),
                "withdrawl",
                Duration.ofMillis(jwtProvider.getExpiration(accessToken)));
        // TODO: 양방향 매핑으로 변경할지 고민중
        // Member 삭제하기 전 Member 를 참조하고 있는 엔티티(MemberTitleLog, Favorite) 먼저 삭제하기
        memberTitleService.deleteByMemberId(id);
        favoriteService.deleteByMemberId(id);
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
}

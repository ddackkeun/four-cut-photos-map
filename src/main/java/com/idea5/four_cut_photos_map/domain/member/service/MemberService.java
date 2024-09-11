package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final RedisDao redisDao;

    public Member findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    // 서비스 로그아웃
    public void logout(Long id) {
        // Redis 에 회원의 kakaoAccessToken, refreshToken 이 있으면 삭제
        if(redisDao.hasKey(RedisDao.getRtkKey(id)))
            redisDao.deleteValues(RedisDao.getRtkKey(id));
        if(redisDao.hasKey(RedisDao.getKakaoAtkKey(id)))
            redisDao.deleteValues(RedisDao.getKakaoAtkKey(id));
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

package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.global.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MemberRequestServiceImpl implements MemberRequestService {
    private final MemberRepository memberRepository;
    private final RedisDao redisDao;

    @Transactional
    @Override
    public LoginResponse login(KakaoUserInfoParam kakaoUserInfo, KakaoTokenResp kakaoToken) {
        LoginResponse response = memberRepository.findByKakaoId(kakaoUserInfo.getId())
                .map(existingMember -> {
                    if (existingMember.getStatus() == MemberStatus.DELETED) {
                        existingMember.updateStatus(MemberStatus.REGISTERED);
                    }
                    existingMember.updateKakaoRefreshToken(kakaoToken.getRefreshToken());
                    return new LoginResponse(MemberResponse.toResponse(existingMember), false);
                })
                .orElseGet(() -> {
                    Member newMember = register(kakaoUserInfo, kakaoToken);
                    return new LoginResponse(MemberResponse.toResponse(newMember), true);
                });

        redisDao.setValues(
                RedisDao.getKakaoAtkKey(response.getMemberResponse().getId()),
                kakaoToken.getAccessToken(),
                Duration.ofSeconds(kakaoToken.getExpiresIn())
        );

        return response;
    }

    @Override
    public Member register(KakaoUserInfoParam kakaoUserInfo, KakaoTokenResp kakaoToken) {
        String nickname = generateUniqueNickname(kakaoUserInfo.getNickname());
        Member member = Member.builder().kakaoId(kakaoUserInfo.getId()).nickname(nickname).kakaoRefreshToken(kakaoToken.getRefreshToken()).build();
        member.updateStatus(MemberStatus.REGISTERED);

        return memberRepository.save(member);
    }

    @Override
    public String generateUniqueNickname(String nickname) {
        String newNickname;
        do {
            newNickname = nickname + Util.generateRandomNumber(4);
        } while (memberRepository.existsByNickname(newNickname));

        return newNickname;
    }

}

package com.idea5.four_cut_photos_map.domain.member.service;

import com.idea5.four_cut_photos_map.domain.auth.dto.param.KakaoUserInfoParam;
import com.idea5.four_cut_photos_map.domain.auth.dto.response.KakaoTokenResp;
import com.idea5.four_cut_photos_map.domain.favorite.entity.Favorite;
import com.idea5.four_cut_photos_map.domain.favorite.repository.FavoriteRepository;
import com.idea5.four_cut_photos_map.domain.member.dto.response.LoginResponse;
import com.idea5.four_cut_photos_map.domain.member.dto.response.MemberResponse;
import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.member.entity.MemberStatus;
import com.idea5.four_cut_photos_map.domain.member.repository.MemberRepository;
import com.idea5.four_cut_photos_map.domain.memberTitle.entity.MemberTitleLog;
import com.idea5.four_cut_photos_map.domain.memberTitle.repository.MemberTitleLogRepository;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.review.repository.ReviewRepository;
import com.idea5.four_cut_photos_map.global.common.RedisDao;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberRequestServiceImpl implements MemberRequestService {
    private final MemberRepository memberRepository;
    private final MemberTitleLogRepository memberTitleLogRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
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

    @Transactional
    @Override
    public String updateNickname(Long id, String nickname) {
        Member member = memberRepository.findByIdAndStatus(id, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(nickname);
        Member updatedMember = memberRepository.save(member);

        return updatedMember.getNickname();
    }

    @Transactional
    @Override
    public void deleteMember(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, MemberStatus.REGISTERED)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (redisDao.hasKey(RedisDao.getRtkKey(id)))
            redisDao.deleteValues(RedisDao.getRtkKey(id));
        if(redisDao.hasKey(RedisDao.getKakaoAtkKey(id)))
            redisDao.deleteValues(RedisDao.getKakaoAtkKey(id));

        // TODO 비동기 처리
        List<MemberTitleLog> memberTitleLogs = memberTitleLogRepository.findAllByMemberId(id);
        memberTitleLogRepository.deleteAll(memberTitleLogs);

        List<Favorite> favorites = favoriteRepository.findAllByMemberId(id);
        favoriteRepository.deleteAll(favorites);

        reviewRepository.findAllByMemberId(id)
                .forEach(review -> review.changeStatus(ReviewStatus.DELETED));

        // 3. DB 에서 회원 삭제
        member.updateStatus(MemberStatus.DELETED);
    }

}

package com.idea5.four_cut_photos_map.domain.review.entity;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;

public class ReviewFactory {
    public static Review from(Member member, Shop shop, int starRating, String content, ReviewStatus status, PurityScore purity, RetouchScore retouch, ItemScore item) {
        return Review.builder()
                .member(member)
                .shop(shop)
                .starRating(starRating)
                .content(content)
                .status(status)
                .purity(purity)
                .retouch(retouch)
                .item(item)
                .build();
    }

    public static Review from(Member member, Shop shop, ReviewStatus status, ReviewRequest request) {
        PurityScore purity = request.getPurity() != null ? PurityScore.valueOf(request.getPurity()) : null;
        RetouchScore retouch = request.getRetouch() != null ? RetouchScore.valueOf(request.getRetouch()) : null;
        ItemScore item = request.getItem() != null ? ItemScore.valueOf(request.getItem()) : null;

        return from(member, shop, request.getStarRating(), request.getContent(), status, purity, retouch, item);
    }
}

package com.idea5.four_cut_photos_map.domain.review.entity;

import com.idea5.four_cut_photos_map.domain.member.entity.Member;
import com.idea5.four_cut_photos_map.domain.review.dto.request.ReviewRequest;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ItemScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.PurityScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.RetouchScore;
import com.idea5.four_cut_photos_map.domain.review.entity.enums.ReviewStatus;
import com.idea5.four_cut_photos_map.domain.shop.entity.Shop;

public class ReviewFactory {
    public static Review from(Member member, Shop shop, int starRating, String content, PurityScore purity, RetouchScore retouch, ItemScore item, ReviewStatus status) {
        return Review.builder()
                .member(member)
                .shop(shop)
                .starRating(starRating)
                .content(content)
                .purity(purity)
                .retouch(retouch)
                .item(item)
                .status(status)
                .build();
    }

    public static Review from(Member member, Shop shop, ReviewRequest request, ReviewStatus status) {
        PurityScore purity = request.getPurity() != null ? PurityScore.valueOf(request.getPurity()) : null;
        RetouchScore retouch = request.getRetouch() != null ? RetouchScore.valueOf(request.getRetouch()) : null;
        ItemScore item = request.getItem() != null ? ItemScore.valueOf(request.getItem()) : null;

        return from(member, shop, request.getStarRating(), request.getContent(), purity, retouch, item, status);
    }
}

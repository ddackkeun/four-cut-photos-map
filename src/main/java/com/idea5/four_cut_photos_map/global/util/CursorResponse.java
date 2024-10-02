package com.idea5.four_cut_photos_map.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorResponse<T> {
    private CursorRequest nextCursorRequest;
    private List<T> body;
}

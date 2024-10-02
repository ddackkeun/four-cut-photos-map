package com.idea5.four_cut_photos_map.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CursorRequest {
    public static final Long NONE_KEY = -1L;

    @Max(Long.MAX_VALUE)
    @Min(2)
    private Long key;

    @Max(100)
    @Min(1)
    private Integer size;

    public static CursorRequest of(Long key, Integer size) {
        return new CursorRequest(key, size);
    }

    public CursorRequest next(Long lastKey) {
        return new CursorRequest(lastKey, this.size);
    }

    public Long getKeyOrDefault(long defaultValue) {
        return (this.key == null) ? defaultValue : this.key;
    }

    public int getSizeOrDefault(int defaultValue) {
        return (this.size == null) ? defaultValue : this.size;
    }

}

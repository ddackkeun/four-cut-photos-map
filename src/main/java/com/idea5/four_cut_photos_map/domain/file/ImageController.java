package com.idea5.four_cut_photos_map.domain.file;

import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.reviewphoto.service.S3Service;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.security.jwt.dto.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final S3Service s3Service;

    @PostMapping("")
    public ResponseEntity<List<ImageUploadResponse>> uploadImages(
            @AuthenticationPrincipal MemberContext memberContext,
            @RequestParam(value = "dirName") String dirName,
            @RequestPart(value = "files") List<MultipartFile> files
    ) {
        if(files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_REQUEST_DATA);
        }

        List<ImageUploadResponse> responses = s3Service.uploadImages(memberContext.getId(), dirName, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @DeleteMapping("")
    public ResponseEntity<Void> deleteImages(
            @AuthenticationPrincipal MemberContext memberContext,
            @RequestBody Map<String, String> request
    ) {
        s3Service.deleteImage(memberContext.getId(), request.get("image-url"));
        return ResponseEntity.noContent().build();
    }
}

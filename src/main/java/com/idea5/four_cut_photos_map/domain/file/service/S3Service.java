package com.idea5.four_cut_photos_map.domain.file.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.idea5.four_cut_photos_map.domain.file.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.domain.file.dto.response.ImageUploadResultResponse;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import com.idea5.four_cut_photos_map.global.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class S3Service {
    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image";

    private final AmazonS3Client amazonS3Client;
    private final String bucketName;
    private final String cloudFrontDomain;

    public S3Service(AmazonS3Client amazonS3Client,
                     @Value("${cloud.aws.s3.bucket-name}") String bucketName,
                     @Value("${cloud.aws.cloudFront.domain}") String cloudFrontDomain) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.cloudFrontDomain = cloudFrontDomain;
    }

    // 단일 이미지 파일 업로드
    public ImageUploadResponse uploadImage(String category, MultipartFile file) {
        // 이미지 파일이 아닌 경우 예외발생
        validImageFile(file);

        // 2. 객체 키 생성(키 이름 중복 방지)
        String key = Util.generateS3ObjectKey(category, file.getOriginalFilename());
        log.info("key : {}" , key);

        // 3. 파일 업로드
        String imageUrl = putS3(key, file);
        return new ImageUploadResponse(file.getOriginalFilename(), imageUrl);
    }

    // 다중 이미지 파일 업로드
    public ImageUploadResultResponse uploadImages(String category, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_FILES_PROVIDED);
        }

        List<ImageUploadResponse> successfulUploads = new ArrayList<>();
        List<ImageUploadResponse> failedUploads = new ArrayList<>();

        // 업로드 실패 시 개별 처리 방식 정책 진행
        for (MultipartFile file : files) {
            try {
                successfulUploads.add(uploadImage(category, file));
            } catch (BusinessException e) {
                failedUploads.add(new ImageUploadResponse(file.getOriginalFilename(), null));
            }
        }

        return new ImageUploadResultResponse(successfulUploads, failedUploads);
    }

    // 이미지 파일인지 검사
    public void validImageFile(MultipartFile file) {
        if(file.getContentType() == null || !file.getContentType().startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            log.warn("file is not an image file.");
            throw new BusinessException(ErrorCode.NOT_IMAGE_FILE);
        }
    }

    // S3 객체 생성
    public String putS3(String key, MultipartFile file) {
        // metadata 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // s3버킷 파일 업로드
        try {
            amazonS3Client.putObject(bucketName, key, file.getInputStream(), metadata);
        } catch (IOException e) {
            log.error("Failed to upload image to S3", e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        // 업로드한 이미지 URL 리턴(cloudFront 캐시된 이미지 URL)
        return getImageUrl(key);
    }

    // 이미지 URL 조회
    private String getImageUrl(String key) {
        return cloudFrontDomain + "/" + key;
    }

}

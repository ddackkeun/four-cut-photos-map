package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * 다중 이미지 파일 업로드
     * 1. 요청 파일리스트 중 이미지 파일만 필터링
     * 2. 이미지 파일 내용 설정 및 s3 버킷 저장
     * 3. 저장에 성공한 이미지 응답 생성 및 반환
     * @param shopId
     * @param files
     * @return List<ImageUploadResponse>
     */
    public List<ImageUploadResponse> uploadImages(Long shopId, List<MultipartFile> files) {
        if (files != null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .filter(this::validateImageFile)
                .map(file -> {
                    String uniqueFileName = generateFileName(shopId.toString(), file.getOriginalFilename());
                    String fileUrl = uploadS3(uniqueFileName, file);
                    return ImageUploadResponse.from(uniqueFileName, fileUrl, file.getContentType(), file.getSize());
                })
                .collect(Collectors.toList());
    }

    public boolean validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX);
    }

    public String generateFileName(String dirName, String originalFileName) {
        return dirName + "/" + UUID.randomUUID() + "_" + originalFileName;
    }

    public String uploadS3(String fileName, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return getImageUrl(fileName);
    }

    public String getImageUrl(String fileName) {
        return cloudFrontDomain + "/" + fileName;
    }

}

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
     * 1. 여려 이미지를 반복하여 단일 이미지 파일 업로드 로직 처리
     * 2. 단일 이미지 업로드 성공시 성공 응답 저장
     * 3. 단일 이미지 업로드 실행 중 예외 발생시 실패 응답 저장
     * @param category
     * @param files
     * @return List<ImageUploadResponse>
     */
    public List<ImageUploadResponse> uploadImages(String category, List<MultipartFile> files) {
        return files.stream()
                .map(file -> uploadFileSafely(category, file))
                .toList();
    }

    private ImageUploadResponse uploadFileSafely(String category, MultipartFile file) {
        try {
            return uploadImage(category, file);
        } catch (Exception e) {
            return new ImageUploadResponse(file.getOriginalFilename(), null, file.getContentType(), file.getSize(), false, e.getMessage());
        }
    }

    /**
     * 단일 이미지 파일 업로드
     * 1. 이미지 파일이 아닌 경우 예외 발생
     * 2. 고유 파일명 생성
     * 3. s3 객체 생성 및 버킷 저장
     * 4. 업로드 응답 리턴
     * @param category
     * @param file
     * @return ImageUploadResponse
     */
    public ImageUploadResponse uploadImage(String category, MultipartFile file) {
        return Optional.of(file)
                .filter(this::validImageFile)
                .map(it -> {
                    String fileName = generateFileName(category, it);
                    String url = putS3(fileName, it);
                    return new ImageUploadResponse(fileName, url, it.getContentType(), it.getSize(), true, null);
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_IMAGE_FILE));
    }

    // 이미지 파일인지 검사
    public boolean validImageFile(MultipartFile file) {
        if (file.getContentType() == null || !file.getContentType().startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            return false;
        }
        return true;
    }

    // 고유한 파일 이름 생성
    public String generateFileName(String dirName, MultipartFile file) {
        return dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    }

    // S3 객체 생성
    public String putS3(String fileName, MultipartFile file) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

            return getImageUrl(fileName);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // 이미지 URL 조회
    public String getImageUrl(String fileName) {
        return cloudFrontDomain + "/" + fileName;
    }

}

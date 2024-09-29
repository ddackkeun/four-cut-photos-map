package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3Service {
    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image";
    private static final String MEMBER_ID_KEY = "member-id";

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

    public List<ImageUploadResponse> uploadImages(Long memberId, String dirName, List<MultipartFile> files) {
        return files.stream()
                .filter(this::validateImageFile)
                .map(file -> uploadImage(memberId, dirName, file))
                .collect(Collectors.toList());
    }

    private ImageUploadResponse uploadImage(Long memberId, String dirName, MultipartFile file) {
        String uniqueFileName = generateFileName(dirName, file.getOriginalFilename());
        String fileUrl = uploadS3(memberId, uniqueFileName, file);

        return ImageUploadResponse.from(uniqueFileName, fileUrl, file.getContentType(), file.getSize());
    }

    private boolean validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX);
    }

    private String generateFileName(String dirName, String originalFileName) {
        return dirName + "/" + UUID.randomUUID() + "_" + originalFileName;
    }

    private String uploadS3(Long memberId, String fileName, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.addUserMetadata(MEMBER_ID_KEY, String.valueOf(memberId));

        try {
            amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return getImageUrl(fileName);
    }

    private String getImageUrl(String fileName) {
        return cloudFrontDomain + "/" + fileName;
    }

    public void deleteImage(Long memberId, String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        ObjectMetadata metadata = amazonS3Client.getObjectMetadata(bucketName, key);

        String ownerId = metadata.getUserMetaDataOf(MEMBER_ID_KEY);
        if (ownerId == null || !ownerId.equals(String.valueOf(memberId))) {
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
        }

        amazonS3Client.deleteObject(bucketName, key);
    }

    private String extractKeyFromUrl(String imageUrl) {
        if (!imageUrl.startsWith(cloudFrontDomain)) {
            throw new BusinessException(ErrorCode.INVALID_URL);
        }
        return imageUrl.substring(cloudFrontDomain.length() + 1);
    }

}

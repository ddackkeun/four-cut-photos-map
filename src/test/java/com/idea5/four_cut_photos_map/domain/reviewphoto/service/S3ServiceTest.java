package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    @Mock
    private AmazonS3Client amazonS3Client;

    private S3Service s3Service;

    private MockMultipartFile validFile;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setUp() {
        String bucketName = "test-bucket";
        String cloudFrontDomain = "https://test.cloudfront.net";
        s3Service = new S3Service(amazonS3Client, bucketName, cloudFrontDomain);

        validFile = new MockMultipartFile("file", "valid.jpg", "image/jpeg", "valid image content".getBytes());
        invalidFile = new MockMultipartFile("file", "invalid.txt", "text/plain", "invalid text content".getBytes());
    }

    @Test
    @DisplayName("단일 이미지 업로드_이미지 파일일 경우 이미지 업로드 후 성공 응답 반환")
    void uploadImage_WhenValidImage_ThenReturnsSuccessResponse() throws IOException {
        // given
        String category = "test";
        when(amazonS3Client.putObject(anyString(), anyString(), any(), any())).thenReturn(new PutObjectResult());

        // when
        ImageUploadResponse response = s3Service.uploadImage(category, validFile);

        // then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(category, response.getFileName().split("/")[0]);
        verify(amazonS3Client, times(1)).putObject(anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("단일 이미지 업로드_이미지 파일이 아닐 경우 예외 발생")
    void uploadImage_WhenInvalidImage_ThenThrowsException() {
        // given
        String category = "test";

        // when
        // then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3Service.uploadImage(category, invalidFile));

        assertEquals(ErrorCode.NOT_IMAGE_FILE, exception.getErrorCode());
        verify(amazonS3Client, never()).putObject(anyString(), anyString(), any(), any());
    }

    @Test
    @DisplayName("다중 이미지 업로드_여러 형식의 파일이 섞여 있을 경우 이미지 파일만 업로드 처리")
    void uploadImages_WithMixedFiles_ShouldReturnProperResponses() throws IOException {
        // given
        String category = "test";
        when(amazonS3Client.putObject(anyString(), anyString(), any(), any())).thenReturn(new PutObjectResult());

        // when
        List<ImageUploadResponse> responses = s3Service.uploadImages(category, List.of(validFile, invalidFile));

        // then
        assertEquals(2, responses.size());

        ImageUploadResponse validResponse = responses.get(0);
        assertTrue(validResponse.getSuccess());
        assertNotNull(validResponse.getUrl());

        ImageUploadResponse invalidResponse = responses.get(1);
        assertFalse(invalidResponse.getSuccess());
        assertNull(invalidResponse.getUrl());
        assertEquals(ErrorCode.NOT_IMAGE_FILE.getMessage(), invalidResponse.getErrorMessage());
    }


    @Test
    @DisplayName("이미지 파일인지 유효성 검사_이미지 파일일 경우 true, 이외의 경우 false 반환")
    void validImageFile() {
        // given

        // when / then
        assertTrue(s3Service.validImageFile(validFile));
        assertFalse(s3Service.validImageFile(invalidFile));
    }


    @Test
    @DisplayName("고유한 파일명 생성_기존 파일명을 전달받아 고유한 파일 이름 반환")
    void generateFileName_ShouldReturnUniqueFileName() {
        // given
        String category = "test";

        // when
        String fileName = s3Service.generateFileName(category, validFile);

        // Then
        assertTrue(fileName.startsWith(category + "/"));
        assertTrue(fileName.contains(validFile.getOriginalFilename()));
    }

    @Test
    @DisplayName("s3 버킷에 이미지 업로드_알 수 없는 예외가 발생할 경우 ErrorCode 예외 발생 처리")
    void putS3_Failure_ThrowsException() throws IOException {
        // given
        String category = "test";
        String fileName = "test-file.jpg";
        doThrow(new RuntimeException())
                .when(amazonS3Client)
                .putObject(anyString(), anyString(), any(), any());

        // when / then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3Service.putS3(fileName, validFile));

        assertEquals(ErrorCode.IMAGE_UPLOAD_FAILED, exception.getErrorCode());
    }

}
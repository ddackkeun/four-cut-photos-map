package com.idea5.four_cut_photos_map.domain.reviewphoto.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.idea5.four_cut_photos_map.domain.reviewphoto.dto.response.ImageUploadResponse;
import com.idea5.four_cut_photos_map.global.error.ErrorCode;
import com.idea5.four_cut_photos_map.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    @Mock
    private AmazonS3Client amazonS3Client;

    private S3Service s3Service;

    private String bucketName;
    private String cloudFrontDomain;

    @BeforeEach
    void setUp() {
        bucketName = "test-bucket";
        cloudFrontDomain = "https://test.cloudfront.net";
        s3Service = new S3Service(amazonS3Client, bucketName, cloudFrontDomain);
    }

    @Nested
    @DisplayName("1개 이상의 이미지를 S3 업로드")
    class UploadImages {

        @Nested
        class SuccessCase {
            @Test
            @DisplayName("유효한 이미지 파일 1개를 s3에 업로드")
            void uploadImages_Success1() {
                // given
                Long memberId = 1L;
                String dirName = "test-directory";
                MultipartFile file = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "test image content".getBytes());
                List<MultipartFile> files = List.of(file);

                given(amazonS3Client.putObject(eq(bucketName), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class)))
                        .willAnswer(invocation -> null);

                // when
                List<ImageUploadResponse> responses = s3Service.uploadImages(memberId, dirName, files);

                // then
                ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
                verify(amazonS3Client, times(1)).putObject(eq(bucketName), anyString(), any(ByteArrayInputStream.class), metadataCaptor.capture());

                assertEquals(1, responses.size());
                assertTrue(responses.get(0).getFileName().startsWith(dirName));
                assertTrue(responses.get(0).getFileName().endsWith(file.getOriginalFilename()));
                assertTrue(responses.get(0).getUrl().startsWith(cloudFrontDomain));
                assertEquals(file.getContentType(), responses.get(0).getType());
                assertEquals(file.getSize(), responses.get(0).getSize());

                ObjectMetadata capturedMetadata = metadataCaptor.getValue();
                assertEquals(file.getContentType(), capturedMetadata.getContentType());
                assertEquals(file.getSize(), capturedMetadata.getContentLength());
                assertEquals(String.valueOf(memberId), capturedMetadata.getUserMetaDataOf("member-id"));
            }

            @Test
            @DisplayName("유효한 이미지 파일 여러 개를 s3에 업로드")
            void uploadImages_Success2() {
                // given
                Long memberId = 1L;
                String dirName = "test-directory";
                MultipartFile file1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "test image content".getBytes());
                MultipartFile file2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "test image content".getBytes());
                MultipartFile file3 = new MockMultipartFile("file", "image3.jpg", "image/jpeg", "test image content".getBytes());
                List<MultipartFile> files = List.of(file1, file2, file3);

                given(amazonS3Client.putObject(eq(bucketName), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class)))
                        .willAnswer(invocation -> null);

                // when
                List<ImageUploadResponse> responses = s3Service.uploadImages(memberId, dirName, files);

                // then
                ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
                verify(amazonS3Client, times(3)).putObject(eq(bucketName), any(String.class), any(ByteArrayInputStream.class), metadataCaptor.capture());

                assertEquals(files.size(), responses.size());
                IntStream.range(0, responses.size())
                                .forEach(i -> {
                                    MultipartFile file = files.get(i);
                                    ImageUploadResponse response = responses.get(i);

                                    assertTrue(response.getFileName().startsWith(dirName));
                                    assertTrue(response.getFileName().endsWith(file.getOriginalFilename()));
                                    assertTrue(response.getUrl().startsWith(cloudFrontDomain));
                                    assertEquals(file.getContentType(), response.getType());
                                    assertEquals(file.getSize(), response.getSize());
                                });

                List<ObjectMetadata> capturedMetadatas  = metadataCaptor.getAllValues();
                IntStream.range(0, capturedMetadatas.size())
                                .forEach(i -> {
                                    MultipartFile file = files.get(i);
                                    ObjectMetadata metadata = capturedMetadatas.get(i);

                                    assertEquals(file.getContentType(), metadata.getContentType());
                                    assertEquals(file.getSize(), metadata.getContentLength());
                                    assertEquals(String.valueOf(memberId), metadata.getUserMetadata().get("member-id"));
                                });
            }

            @Test
            @DisplayName("여러 개의 파일 중 유효하지 않은 파일이 포함되었을 때 유효한 이미지만 s3 업로드")
            void uploadImages_Success3() {
                // given
                Long memberId = 1L;
                String dirName = "test-directory";
                MultipartFile imageFile1 = new MockMultipartFile("file", "image1.jpg", "image/jpeg", "test image content".getBytes());
                MultipartFile imageFile2 = new MockMultipartFile("file", "image2.jpg", "image/jpeg", "test image content".getBytes());
                MultipartFile textFile1 = new MockMultipartFile("file", "text1.txt", "text/plain", "test text content".getBytes());
                MultipartFile textFile2 = new MockMultipartFile("file", "text2.txt", "text/plain", "test text content".getBytes());
                List<MultipartFile> imageFiles = List.of(imageFile1, imageFile2);
                List<MultipartFile> files = List.of(imageFile1, textFile1, imageFile2, textFile2);

                given(amazonS3Client.putObject(eq(bucketName), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class)))
                        .willAnswer(invocation -> null);

                // when
                List<ImageUploadResponse> responses = s3Service.uploadImages(memberId, dirName, files);

                // then
                ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
                verify(amazonS3Client, times(2)).putObject(eq(bucketName), any(String.class), any(ByteArrayInputStream.class), metadataCaptor.capture());

                assertEquals(2, responses.size());
                IntStream.range(0, responses.size())
                        .forEach(i -> {
                            ImageUploadResponse response = responses.get(i);
                            MultipartFile imageFile = imageFiles.get(i);

                            assertTrue(response.getFileName().startsWith(dirName));
                            assertTrue(response.getFileName().endsWith(imageFile.getOriginalFilename()));
                            assertTrue(response.getUrl().startsWith(cloudFrontDomain));
                            assertEquals(imageFile.getContentType(), response.getType());
                            assertEquals(imageFile.getSize(), response.getSize());
                        });

                List<ObjectMetadata> capturedMetadatas  = metadataCaptor.getAllValues();
                IntStream.range(0, capturedMetadatas.size())
                        .forEach(i -> {
                            MultipartFile imageFile = imageFiles.get(i);
                            ObjectMetadata metadata = capturedMetadatas.get(i);

                            assertEquals(imageFile.getContentType(), metadata.getContentType());
                            assertEquals(imageFile.getSize(), metadata.getContentLength());
                            assertEquals(String.valueOf(memberId), metadata.getUserMetadata().get("member-id"));
                        });
            }

            @Test
            @DisplayName("여러 개의 파일 중 유효하지 않은 파일만 있을 때 빈 리스트 응답")
            void uploadImages_Success4() {
                // given
                Long memberId = 1L;
                String dirName = "test-directory";
                MultipartFile textFile1 = new MockMultipartFile("file", "text1.txt", "text/plain", "test text content".getBytes());
                MultipartFile textFile2 = new MockMultipartFile("file", "text2.txt", "text/plain", "test text content".getBytes());
                MultipartFile textFile3 = new MockMultipartFile("file", "text1.txt", "text/plain", "test text content".getBytes());
                List<MultipartFile> textFiles = List.of(textFile1, textFile2, textFile3);

                // when
                List<ImageUploadResponse> responses = s3Service.uploadImages(memberId, dirName, textFiles);

                // then
                assertTrue(responses.isEmpty());
                verify(amazonS3Client, never()).putObject(anyString(), anyString(), any(ByteArrayInputStream.class), any(ObjectMetadata.class));
            }
        }

        @Nested
        class FailCase {
            @Test
            @DisplayName("s3 이미지 업로드 시 예외가 발생할 때 예외 처리")
            void uploadImages_Fail1() {
                // given
                Long memberId = 1L;
                String dirName = "test-directory";
                MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test image content".getBytes());
                List<MultipartFile> files = List.of(file);

                willThrow(new RuntimeException("S3 upload failed")).given(amazonS3Client)
                        .putObject(eq(bucketName), anyString(), any(InputStream.class), any(ObjectMetadata.class));

                // when & then
                BusinessException result = assertThrows(BusinessException.class, () -> {
                    s3Service.uploadImages(memberId, dirName, files);
                });
                assertEquals(ErrorCode.IMAGE_UPLOAD_FAILED, result.getErrorCode());
            }
        }

    }

    @Nested
    @DisplayName("s3 버킷의 이미지 삭제")
    class DeleteImage {
        @Nested
        class SuccessCase {
            @Test
            @DisplayName("해당 imageUrl의 이미지가 유효하고 작성자가 일치하면 s3 버킷에서 삭제")
            void deleteImage_Success1() {
                // given
                Long memberId = 1L;
                String key = "test-directory/image1.jpg";
                String imageUrl = cloudFrontDomain + "/" + key;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("member-id", String.valueOf(memberId));

                given(amazonS3Client.getObjectMetadata(eq(bucketName), anyString())).willReturn(metadata);

                // when
                s3Service.deleteImage(memberId, imageUrl);

                // then
                verify(amazonS3Client).getObjectMetadata(bucketName, key);
                verify(amazonS3Client).deleteObject(bucketName, key);
            }
        }

        @Nested
        class FailCase {
            @Test
            @DisplayName("imageUrl이 cloudFront 도메인의 url 아닐 경우 예외 발생")
            void deleteImage_Fail() {
                // given
                Long memberId = 1L;
                String imageUrl = "https://test.naver.com";

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> s3Service.deleteImage(memberId, imageUrl));
                assertEquals(ErrorCode.INVALID_URL, result.getErrorCode());

                verify(amazonS3Client, never()).getObjectMetadata(anyString(), anyString());
                verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
            }

            @Test
            @DisplayName("이미지 등록한 소유자와 현재 유저가 다를 경우 예외 발생")
            void deleteImage_Fai2() {
                // given
                Long memberId = 1L;
                String key = "test-directory/image1.jpg";
                String imageUrl = cloudFrontDomain + "/" + key;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("member-id", String.valueOf(1000L));

                given(amazonS3Client.getObjectMetadata(eq(bucketName), anyString())).willReturn(metadata);

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> s3Service.deleteImage(memberId, imageUrl));

                assertEquals(ErrorCode.FILE_ACCESS_DENIED, result.getErrorCode());
                verify(amazonS3Client).getObjectMetadata(bucketName, key);
                verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
            }

            @Test
            @DisplayName("ObjectMetadata의 UserMetaData null 일 때 예외발생")
            void deleteImage_Fai3() {
                // given
                Long memberId = 1L;
                String key = "test-directory/image1.jpg";
                String imageUrl = cloudFrontDomain + "/" + key;
                ObjectMetadata metadata = new ObjectMetadata();

                given(amazonS3Client.getObjectMetadata(eq(bucketName), anyString())).willReturn(metadata);

                // when & then
                BusinessException result = assertThrows(BusinessException.class,
                        () -> s3Service.deleteImage(memberId, imageUrl));

                assertEquals(ErrorCode.FILE_ACCESS_DENIED, result.getErrorCode());
                verify(amazonS3Client).getObjectMetadata(bucketName, key);
                verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
            }

            @Test
            @DisplayName("s3 버킷에서 ObjectMetadata 가져올 때 예외가 발생 시 예외 처리")
            public void deleteImage_Fail4() {
                // given
                Long memberId = 1L;
                String key = "test-directory/image1.jpg";
                String imageUrl = cloudFrontDomain + "/" + key;
                String errorMessage = "amazonS3Client.getObjectMetadata error";

                willThrow(new AmazonS3Exception(errorMessage)).given(amazonS3Client).getObjectMetadata(eq(bucketName), anyString());

                // when & then
                AmazonS3Exception result = assertThrows(AmazonS3Exception.class, () -> {
                    s3Service.deleteImage(memberId, imageUrl);
                });

                assertTrue(result.getMessage().startsWith(errorMessage));
                verify(amazonS3Client).getObjectMetadata(bucketName, key);
                verify(amazonS3Client, never()).deleteObject(anyString(), anyString());
            }

            @Test
            @DisplayName("s3 버킷에서 이미지를 삭제할 때 예외 발생 시 예외처리")
            public void deleteImage_Fail5() {
                // given
                Long memberId = 1L;
                String key = "test-directory/image1.jpg";
                String imageUrl = cloudFrontDomain + "/" + key;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("member-id", String.valueOf(memberId));
                String errorMessage = "amazonS3Client.deleteObject error";

                given(amazonS3Client.getObjectMetadata(eq(bucketName), anyString())).willReturn(metadata);
                willThrow(new AmazonS3Exception(errorMessage)).given(amazonS3Client).deleteObject(eq(bucketName), anyString());

                // when & then
                AmazonS3Exception result = assertThrows(AmazonS3Exception.class, () -> {
                    s3Service.deleteImage(memberId, imageUrl);
                });

                assertTrue(result.getMessage().startsWith(errorMessage));
                verify(amazonS3Client).getObjectMetadata(bucketName, key);
                verify(amazonS3Client).deleteObject(bucketName, key);
            }
        }
    }

}

package com.uniConnect.s3;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.*;
import java.net.URL;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Template s3Template;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public String upload(String bucket, String key, MultipartFile file) throws Exception {
        try {
            // content-type을 함께 올리고 싶다면 PutObjectRequest를 직접 사용해도 됩니다.
            s3Template.upload(bucket, key, file.getInputStream());
            return key;
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    public InputStreamResource download(String bucket, String key) throws IOException {
        S3Resource s3Res = s3Template.download(bucket, key);
        return new InputStreamResource(s3Res.getInputStream());
    }

    /** 객체 삭제 */
    public void delete(String bucket, String key) {
        s3Template.deleteObject(bucket, key);
    }

    /** prefix 기준으로 객체 키 나열 */
    public List<String> list(String bucket, String prefix) {
        String p= (prefix==null) ? "" : prefix;
        return s3Template.listObjects(bucket, p)
                .stream()
                .map(res -> res.getLocation().getObject()) // 또는 res.getFilename()
                .collect(Collectors.toList());
    }

    /** GET presigned URL (다운로드 링크) */
    public URL presignGet(String bucket, String key, Duration expiresIn) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .getObjectRequest(getReq)
                .build();
        return s3Presigner.presignGetObject(presignReq).url();
    }

    /** PUT presigned URL (직접 업로드용) */
    public URL presignPut(String bucket, String key, Duration expiresIn, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .putObjectRequest(putReq)
                .build();
        return s3Presigner.presignPutObject(presignReq).url();
    }

    /** (옵션) SDK로 바로 업로드하고 싶을 때의 예시 */
    public void uploadWithMeta(String bucket, String key, MultipartFile file, String contentType) {
        try {
            String ct = contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(ct)
                    .build();

            s3Client.putObject(req,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void uploadWithTemplateMeta(String bucket, String key, MultipartFile file, String contentType) {
        try {
            io.awspring.cloud.s3.ObjectMetadata metadata = io.awspring.cloud.s3.ObjectMetadata.builder()
                    .contentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build();
            s3Template.upload(bucket, key, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 upload with metadata failed", e);
        }
    }
}
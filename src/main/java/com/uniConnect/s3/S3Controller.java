package com.uniConnect.s3;

import com.uniConnect.s3.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Validated
public class S3Controller {

    private final S3FileService s3FileService;
    private final S3Props props;

    // 업로드: JSON + multipart 혼합을 피하고, key 검증 + prefix 강제
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponse>> upload(
            @Valid @RequestPart("meta") UploadRequest meta,
            @RequestPart("file") MultipartFile file // DTO의 file과 동일 객체를 써도 되지만 분리 시 직관적
    ) throws Exception {
        // 파일 크기 추가 검증(예: 50MB 이하)
        if (file.getSize() <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("빈 파일은 업로드할 수 없습니다."));
        }

        String safeKey = KeySanitizer.normalize(props.keyPrefix(), meta.key());
        s3FileService.upload(props.bucket(), safeKey, file);
        return ResponseEntity.ok(ApiResponse.ok(new UploadResponse(safeKey)));
    }

    // 다운로드: key만 입력받고, 버킷은 서버 설정 사용
    @PostMapping("/download")
    public ResponseEntity<Resource> download(@Valid @RequestBody PresignGetRequest req) throws IOException {
        String safeKey = KeySanitizer.normalize(props.keyPrefix(), req.key());
        Resource resource = s3FileService.download(props.bucket(), safeKey);

        // 파일 이름 노출은 마지막 경로만
        String filename = safeKey.substring(safeKey.lastIndexOf('/') + 1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 삭제
    @DeleteMapping("/object")
    public ResponseEntity<ApiResponse<Void>> delete(@Valid @RequestBody DeleteRequest req) {
        String safeKey = KeySanitizer.normalize(props.keyPrefix(), req.key());
        s3FileService.delete(props.bucket(), safeKey);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 리스트
    @PostMapping("/objects")
    public ResponseEntity<ApiResponse<List<String>>> list(@Valid @RequestBody ListRequest req) {
        String prefix = KeySanitizer.normalize(props.keyPrefix(), req.prefix() == null ? "" : req.prefix());
        List<String> items = s3FileService.list(props.bucket(), prefix);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    // presigned GET
    @PostMapping("/presign-get")
    public ResponseEntity<ApiResponse<PresignResponse>> presignGet(@Valid @RequestBody PresignGetRequest req) {
        long ttl = Math.min(req.expiresInSec(), props.maxPresignSeconds() != null ? props.maxPresignSeconds() : 3600);
        String safeKey = KeySanitizer.normalize(props.keyPrefix(), req.key());
        URL url = s3FileService.presignGet(props.bucket(), safeKey, Duration.ofSeconds(ttl));
        return ResponseEntity.ok(ApiResponse.ok(new PresignResponse(url.toString(), ttl)));
    }

    // presigned PUT (허용 Content-Type 체크)
    @PostMapping("/presign-put")
    public ResponseEntity<ApiResponse<PresignResponse>> presignPut(@Valid @RequestBody PresignPutRequest req) {
        long ttl = Math.min(req.expiresInSec(), props.maxPresignSeconds() != null ? props.maxPresignSeconds() : 3600);
        if (props.allowedContentTypes() != null && !props.allowedContentTypes().isEmpty()
                && !props.allowedContentTypes().contains(req.contentType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("허용되지 않은 Content-Type 입니다."));
        }

        String safeKey = KeySanitizer.normalize(props.keyPrefix(), req.key());
        URL url = s3FileService.presignPut(props.bucket(), safeKey, Duration.ofSeconds(ttl), req.contentType());
        return ResponseEntity.ok(ApiResponse.ok(new PresignResponse(url.toString(), ttl)));
    }
}
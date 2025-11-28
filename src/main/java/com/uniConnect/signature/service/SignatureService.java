package com.uniConnect.signature.service;

import com.uniConnect.signature.dto.SignatureRequest;
import com.uniConnect.signature.entity.Signature;
import com.uniConnect.signature.repository.SignatureRepository;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final SignatureRepository signatureRepository;

    public void saveSignature(SignatureRequest req, Long userId) {

        // 1. timestamp 보정
        long timestamp = normalizeTimestamp(req.getTimestamp());

        // 2. 시간 검증
        long now = System.currentTimeMillis();
        if (Math.abs(now - timestamp) > 5 * 60 * 1000) {
            throw new CustomException(ErrorCode.SIGNATURE_EXPIRED);
        }

        // 3. 이미지 디코딩
        byte[] imageBytes = decodeBase64(req.getSignatureImage());

        // 4. 해시 계산
        String serverHash = calcHash(imageBytes, timestamp, userId);

        // 5. DB 저장
        Signature signature = Signature.builder()
                .userId(userId)
                .signatureHash(serverHash)
                .timestamp(timestamp)
                .build();

        signatureRepository.save(signature);
    }

    private long normalizeTimestamp(Long rawTimestamp) {
        if (rawTimestamp == null) {
            throw new CustomException(ErrorCode.INVALID_SIGNATURE);
        }

        long ts = rawTimestamp;

        // 초 단위 → 밀리초 변환
        if (ts < 1000000000000L) {
            ts = ts * 1000;
        }

        return ts;
    }

    private byte[] decodeBase64(String base64) {

        if (base64 == null || !base64.contains(",")) {
            throw new CustomException(ErrorCode.INVALID_SIGNATURE);
        }

        String pure = base64.substring(base64.indexOf(",") + 1);
        return Base64.getDecoder().decode(pure);
    }

    private String calcHash(byte[] imageBytes, long timestamp, Long userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(imageBytes);
            digest.update(String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8));
            digest.update(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));

            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("서명 해시 계산 실패", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
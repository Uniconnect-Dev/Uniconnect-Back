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

        long now = System.currentTimeMillis();
        if (Math.abs(now - req.getTimestamp()) > 5 * 60 * 1000) {
            throw new CustomException(ErrorCode.SIGNATURE_EXPIRED);
        }

        byte[] imageBytes = decodeBase64(req.getSignatureImage());

        String hash = calcHash(imageBytes, req.getTimestamp(), userId);

        Signature signature = Signature.builder()
                .userId(userId)
                .signatureHash(hash)
                .timestamp(req.getTimestamp())
                .build();

        signatureRepository.save(signature);
    }

    private byte[] decodeBase64(String base64) {
        String pure = base64.substring(base64.indexOf(",") + 1);
        return Base64.getDecoder().decode(pure);
    }

    private String calcHash(byte[] imageBytes, Long timestamp, Long userId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(imageBytes);
            digest.update(timestamp.toString().getBytes(StandardCharsets.UTF_8));
            digest.update(userId.toString().getBytes(StandardCharsets.UTF_8));

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
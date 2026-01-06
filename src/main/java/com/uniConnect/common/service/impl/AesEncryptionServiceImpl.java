package com.uniConnect.common.service.impl;

import com.uniConnect.common.service.EncryptionService;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class AesEncryptionServiceImpl implements EncryptionService {

    // AES 암호화 알고리즘 설정
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  // 96-bit IV
    private static final int GCM_TAG_LENGTH = 128;  // 128-bit authentication tag
    private static final int KEY_SIZE = 256;  // 256-bit key

    @Value("${encryption.key:default-secret-key-must-be-32-chars}")
    private String encryptionKeyString;

    private SecretKey getSecretKey() {
        // 키를 32바이트(256비트)로 정규화
        byte[] decodedKey = encryptionKeyString.getBytes();

        // 키 길이가 32바이트가 아니면 해시를 사용하거나 패딩
        if (decodedKey.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(decodedKey, 0, paddedKey, 0, decodedKey.length);
            decodedKey = paddedKey;
        } else if (decodedKey.length > 32) {
            byte[] trimmedKey = new byte[32];
            System.arraycopy(decodedKey, 0, trimmedKey, 0, 32);
            decodedKey = trimmedKey;
        }

        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * AES-GCM을 사용한 암호화
     * IV는 암호화된 텍스트의 앞부분에 포함되어 저장됨
     */
    @Override
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }

            // 1) IV(Initialization Vector) 생성
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);

            // 2) GCM 파라미터 설정
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 3) Cipher 초기화 및 암호화
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), gcmParameterSpec);
            byte[] encryptedText = cipher.doFinal(plainText.getBytes());

            // 4) IV + 암호화된 텍스트를 합쳐서 Base64로 인코딩
            byte[] ivAndEncrypted = new byte[iv.length + encryptedText.length];
            System.arraycopy(iv, 0, ivAndEncrypted, 0, iv.length);
            System.arraycopy(encryptedText, 0, ivAndEncrypted, iv.length, encryptedText.length);

            return Base64.getEncoder().encodeToString(ivAndEncrypted);

        } catch (Exception e) {
            log.error("[암호화 실패] {}", e.getMessage());
            throw new CustomException(ErrorCode.ENCRYPTION_FAILED);
        }
    }

    /**
     * AES-GCM을 사용한 복호화
     * 암호화된 텍스트에서 IV를 추출하여 복호화
     */
    @Override
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }

            // 1) Base64 디코딩
            byte[] decodedValue = Base64.getDecoder().decode(encryptedText);

            // 2) IV와 암호화된 텍스트 분리
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[decodedValue.length - GCM_IV_LENGTH];

            System.arraycopy(decodedValue, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(decodedValue, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            // 3) GCM 파라미터 설정
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 4) Cipher 초기화 및 복호화
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmParameterSpec);
            byte[] decryptedText = cipher.doFinal(cipherText);

            return new String(decryptedText);

        } catch (Exception e) {
            log.error("[복호화 실패] {}", e.getMessage());
            throw new CustomException(ErrorCode.DECRYPTION_FAILED);
        }
    }
}
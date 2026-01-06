
package com.uniConnect.common.service;

public interface EncryptionService {

    /**
     * 평문을 암호화
     * @param plainText 평문
     * @return 암호화된 텍스트
     */
    String encrypt(String plainText);

    /**
     * 암호화된 텍스트를 복호화
     * @param encryptedText 암호화된 텍스트
     * @return 복호화된 평문
     */
    String decrypt(String encryptedText);
}
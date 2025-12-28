package com.uniConnect.common.util;

import java.util.Base64;

public class Base64ToMultipartFileUtil {

    public static ByteArrayMultipartFile convert(String base64, String filename) {

        String pureBase64 = base64.substring(base64.indexOf(",") + 1);

        byte[] decodedBytes = Base64.getDecoder().decode(pureBase64);

        return new ByteArrayMultipartFile(
                decodedBytes,
                filename,
                filename,
                "image/png"
        );
    }
}

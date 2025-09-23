package com.uniConnect.s3;

public final class KeySanitizer {
    private KeySanitizer() {}

    public static String normalize(String prefix, String key) {
        // 앞뒤 공백 제거
        String normalized = key.trim();

        // 디렉터리 트래버설 방지: "//", "../", "/.." 등 제거
        while (normalized.contains("//")) normalized = normalized.replace("//", "/");
        if (normalized.startsWith("/")) normalized = normalized.substring(1);
        if (normalized.contains("..")) throw new IllegalArgumentException("Invalid key");

        // keyPrefix 강제 적용
        String p = (prefix == null) ? "" : prefix.trim();
        if (!p.isEmpty() && !p.endsWith("/")) p = p + "/";
        return p + normalized;
    }
}

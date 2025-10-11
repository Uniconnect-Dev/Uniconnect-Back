package com.uniConnect.member.security.oauth.dto;

import java.util.Map;

public class GoogleUserInfo implements OAuthUserInfo {
    private final Map<String, Object> a;
    public GoogleUserInfo(Map<String, Object> a) { this.a = a; }

    public String provider()       { return "google"; }
    public String providerUserId() { return (String) a.get("sub"); }
    public String email()          { return (String) a.get("email"); }

    @Override
    public String username() {
        // Prefer email; if absent, fall back to providerUserId@google.local
        String emailVal = email();
        return emailVal != null ? emailVal : providerUserId() + "@google.local";
    }

    public String picture()        { return (String) a.get("picture"); }
    public Map<String,Object> attributes(){ return a; }
}

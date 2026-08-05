package com.bites.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Controls how the refresh-token cookie is issued. Differs between dev and
// prod (see application-dev.yml / application-prod.yml): local dev runs
// plain HTTP on localhost, while production runs cross-site over HTTPS
@Component
public class RefreshCookieProperties {

    @Value("${app.jwt.refresh-cookie.secure}")
    private boolean secure;

    @Value("${app.jwt.refresh-cookie.same-site}")
    private String sameSite;

    public boolean isSecure() {
        return secure;
    }

    public String getSameSite() {
        return sameSite;
    }
}

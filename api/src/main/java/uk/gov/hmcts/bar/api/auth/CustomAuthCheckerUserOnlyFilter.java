package uk.gov.hmcts.bar.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.useronly.AuthCheckerUserOnlyFilter;

import javax.servlet.http.HttpServletRequest;

public class CustomAuthCheckerUserOnlyFilter <T extends User> extends AbstractPreAuthenticatedProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthCheckerUserOnlyFilter.class);
    private final RequestAuthorizer<T> userRequestAuthorizer;

    public CustomAuthCheckerUserOnlyFilter(RequestAuthorizer<T> userRequestAuthorizer) {
        this.userRequestAuthorizer = userRequestAuthorizer;
    }

    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return this.authorizeUser(request);
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private T authorizeUser(HttpServletRequest request) {
        try {
            return this.userRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.error("Error during authentication", e);
            return null;
        }
    }
}

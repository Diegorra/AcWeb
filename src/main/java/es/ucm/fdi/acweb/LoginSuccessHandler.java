package es.ucm.fdi.acweb;

import java.io.IOException;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import es.ucm.fdi.acweb.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

/**
 * Called when a user is first authenticated (via login).
 * Called from SecurityConfig; see https://stackoverflow.com/a/53353324
 *
 * Adds a "u" variable to the session when a user is first authenticated.
 * Important: the user is retrieved from the database, but is not refreshed at
 * each request.
 * You should refresh the user's information if anything important changes; for
 * example, after
 * updating the user's profile.
 */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private HttpSession session;

    @Autowired
    private EntityManager entityManager;

    private static Logger log = LogManager.getLogger(LoginSuccessHandler.class);
    private RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * Called whenever a user authenticates correctly.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        /*
         * Avoids following warning:
         * Cookie “JSESSIONID” will be soon rejected because it has the “SameSite”
         * attribute set to “None” or an invalid value, without the “secure” attribute.
         * To know more about the “SameSite“ attribute, read
         * https://developer.mozilla.org/docs/Web/HTTP/Headers/Set-Cookie/SameSite
         */
        addSameSiteCookieAttribute(response);

        String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal())
                .getUsername();

        // add a 'u' session variable, accessible from thymeleaf via ${session.u}
        log.info("Storing user info for {} in session {}", username, session.getId());
        User u = entityManager.createNamedQuery("User.byUsername", User.class)
                .setParameter("username", username)
                .getSingleResult();
        session.setAttribute("u", u);

        log.info("LOG IN: {} (id {}) -- session is {}",
                u.getUsername(), u.getId(), session.getId());

        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        String targetUrlParameter = "/";
        if (isAlwaysUseDefaultTargetUrl()
                || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
            this.requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        clearAuthenticationAttributes(request);
        // Use the DefaultSavedRequest URL
        String targetUrl = savedRequest.getRedirectUrl();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Set samesite cookie - see https://stackoverflow.com/a/58996747/15472
     *
     * @param response
     */
    private void addSameSiteCookieAttribute(HttpServletResponse response) {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        // there can be multiple Set-Cookie attributes
        for (String header : headers) {
            if (firstHeader) {
                response.setHeader(HttpHeaders.SET_COOKIE,
                        String.format("%s; %s", header, "SameSite=Strict"));
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE,
                    String.format("%s; %s", header, "SameSite=Strict"));
        }
    }
}

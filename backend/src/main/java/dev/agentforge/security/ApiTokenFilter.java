package dev.agentforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Protects state-changing tool and approval routes with a server-configured shared token. */
@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private final String configuredToken;

    public ApiTokenFilter(@Value("${agentforge.security.api-token:}") String configuredToken) {
        this.configuredToken = configuredToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean toolExecution = path.startsWith("/api/tools/") && path.endsWith("/execute");
        boolean approvalRoute = path.startsWith("/api/approvals");
        return !toolExecution && !approvalRoute;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String supplied = request.getHeader("X-AgentForge-Token");
        boolean configured = configuredToken != null && !configuredToken.isBlank();
        boolean matches = configured && supplied != null
                && MessageDigest.isEqual(
                        configuredToken.getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            response.setStatus(configured ? HttpServletResponse.SC_UNAUTHORIZED
                    : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(configured
                    ? "{\"error\":\"unauthorized\"}"
                    : "{\"error\":\"access_control_not_configured\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}

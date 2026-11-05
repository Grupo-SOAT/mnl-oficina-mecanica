package br.com.fiap.postech.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates trusted internal services through a shared API key.
 *
 * <p>Used for service-to-service reads (owners, vehicles, catalog) in the local fleet and in the
 * cluster. The key is supplied by {@code app.internal.api-key}.</p>
 */
public class InternalApiKeyFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Internal-Api-Key";

    private final String apiKey;

    public InternalApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (apiKey != null && !apiKey.isBlank() && apiKey.equals(provided)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "internal-service", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}

package br.com.fiap.postech.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MdcUserFilterTest {

    private final MdcUserFilter filter = new MdcUserFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsUserIdInMdcWhileRequestIsProcessedWhenAuthenticated() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("user-123", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("user_id")).isEqualTo("user-123");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
        assertThat(MDC.get("user_id")).isNull();
    }

    @Test
    void clearsUserIdFromMdcWhenFilterChainThrows() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken("user-123", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        var chain = mock(FilterChain.class);
        doThrow(new ServletException("boom")).when(chain).doFilter(any(), any());

        assertThatThrownBy(() -> filter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), chain))
                .isInstanceOf(ServletException.class);

        assertThat(MDC.get("user_id")).isNull();
    }

    @Test
    void leavesUserIdUnsetWhenNotAuthenticated() throws Exception {
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("user_id")).isNull();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void leavesUserIdUnsetForAnonymousUser() throws Exception {
        var anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("user_id")).isNull();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void propagatesCorrelationIdFromHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "corr-123");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("correlation_id")).isEqualTo("corr-123");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("corr-123");
        assertThat(MDC.get("correlation_id")).isNull();
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsAbsent() throws Exception {
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("correlation_id")).isNotBlank();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotBlank();
    }

    @Test
    void extractsServiceOrderIdFromPath() throws Exception {
        var request = new MockHttpServletRequest("GET", "/service-orders/42/budget");
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("so_id")).isEqualTo("42");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(MDC.get("so_id")).isNull();
    }

    @Test
    void propagatesBranchIdFromRequestAttribute() throws Exception {
        var request = new MockHttpServletRequest();
        request.setAttribute("branchId", 7L);
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(MDC.get("branch_id")).isEqualTo("7");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(MDC.get("branch_id")).isNull();
    }
}

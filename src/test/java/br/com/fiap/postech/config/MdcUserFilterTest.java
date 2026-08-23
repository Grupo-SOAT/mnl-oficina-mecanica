package br.com.fiap.postech.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

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
}

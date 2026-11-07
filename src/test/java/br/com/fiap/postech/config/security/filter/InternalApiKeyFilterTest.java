package br.com.fiap.postech.config.security.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private final InternalApiKeyFilter filter = new InternalApiKeyFilter("secret");

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_authenticate_internal_service_with_valid_key() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "secret");
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                    .isEqualTo("internal-service");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void should_not_authenticate_with_invalid_key() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "wrong");
        var chain = mock(FilterChain.class);
        doAnswer(invocation -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void should_not_authenticate_when_key_is_blank() throws Exception {
        var filterWithBlankKey = new InternalApiKeyFilter("   ");
        var request = new MockHttpServletRequest();
        request.addHeader("X-Internal-Api-Key", "   ");
        var chain = mock(FilterChain.class);

        filterWithBlankKey.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}

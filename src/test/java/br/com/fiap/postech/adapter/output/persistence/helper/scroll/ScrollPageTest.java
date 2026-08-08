package br.com.fiap.postech.adapter.output.persistence.helper.scroll;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrollPageTest {

    @Test
    void should_build_with_all_fields() {
        ScrollPage<String> page = ScrollPage.<String>builder()
                .data(List.of("a", "b"))
                .cursor("2")
                .isLast(false)
                .pageSize(10)
                .build();

        assertThat(page.data()).containsExactly("a", "b");
        assertThat(page.cursor()).isEqualTo("2");
        assertThat(page.isLast()).isFalse();
        assertThat(page.pageSize()).isEqualTo(10);
    }

    @Test
    void should_represent_last_page_when_is_last_is_true_and_cursor_is_null() {
        ScrollPage<String> page = ScrollPage.<String>builder()
                .data(List.of())
                .cursor(null)
                .isLast(true)
                .pageSize(5)
                .build();

        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
        assertThat(page.data()).isEmpty();
        assertThat(page.pageSize()).isEqualTo(5);
    }

    @Test
    void should_equal_another_page_with_same_fields() {
        ScrollPage<String> a = ScrollPage.<String>builder()
                .data(List.of("x")).cursor("1").isLast(false).pageSize(3).build();
        ScrollPage<String> b = ScrollPage.<String>builder()
                .data(List.of("x")).cursor("1").isLast(false).pageSize(3).build();

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void should_not_equal_page_with_different_cursor() {
        ScrollPage<String> a = ScrollPage.<String>builder()
                .data(List.of("x")).cursor("1").isLast(false).pageSize(3).build();
        ScrollPage<String> b = ScrollPage.<String>builder()
                .data(List.of("x")).cursor("2").isLast(false).pageSize(3).build();

        assertThat(a).isNotEqualTo(b);
    }
}

package br.com.fiap.postech.adapter.output.persistence.helper.scroll;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrollerTest {

    @Test
    void should_use_zero_cursor_when_cursor_is_null() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll(null, 10, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(0L);
    }

    @Test
    void should_use_zero_cursor_when_cursor_is_blank() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll("   ", 10, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(0L);
    }

    @Test
    void should_use_zero_cursor_when_cursor_is_empty_string() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll("", 10, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(0L);
    }

    @Test
    void should_parse_valid_numeric_cursor_string() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll("42", 10, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(42L);
    }

    @Test
    void should_use_zero_cursor_when_cursor_is_not_numeric() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll("not-a-number", 10, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(0L);
    }

    @Test
    void should_request_page_size_plus_one_to_detect_overflow() {
        List<Integer> capturedSizes = new ArrayList<>();

        Scroller.scroll(null, 10, (cursor, pageable) -> {
            capturedSizes.add(pageable.getPageSize());
            return List.of();
        });

        assertThat(capturedSizes).containsExactly(11);
    }

    @Test
    void should_return_empty_page_when_query_returns_empty_list() {
        ScrollPage<String> page = Scroller.scroll(null, 10, (cursor, pageable) -> List.of());

        assertThat(page.data()).isEmpty();
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
        assertThat(page.pageSize()).isEqualTo(10);
    }

    @Test
    void should_return_empty_page_when_query_returns_null() {
        ScrollPage<String> page = Scroller.scroll(null, 10, (cursor, pageable) -> null);

        assertThat(page.data()).isEmpty();
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_return_last_page_when_results_equal_page_size() {
        ScrollPage<String> page = Scroller.scroll(null, 3, (cursor, pageable) ->
                List.of("a", "b", "c"));

        assertThat(page.data()).hasSize(3);
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_return_last_page_when_results_less_than_page_size() {
        ScrollPage<String> page = Scroller.scroll(null, 5, (cursor, pageable) ->
                List.of("a", "b"));

        assertThat(page.data()).hasSize(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_truncate_data_and_set_cursor_when_results_exceed_page_size() {
        ScrollPage<String> page = Scroller.scroll(null, 3, (cursor, pageable) ->
                List.of("a", "b", "c", "d"));

        assertThat(page.data()).hasSize(3);
        assertThat(page.isLast()).isFalse();
        assertThat(page.cursor()).isEqualTo("c");
    }

    @Test
    void should_set_cursor_from_last_item_of_current_page() {
        ScrollPage<String> page = Scroller.scroll("0", 3, (cursor, pageable) ->
                List.of("item-1", "item-2", "item-3", "item-4"));

        assertThat(page.cursor()).isEqualTo("item-3");
        assertThat(page.data()).containsExactly("item-1", "item-2", "item-3");
    }

    @Test
    void should_pass_parsed_cursor_to_query_on_subsequent_pages() {
        List<Long> captured = new ArrayList<>();

        Scroller.scroll("7", 5, (cursor, pageable) -> {
            captured.add(cursor);
            return List.of();
        });

        assertThat(captured).containsExactly(7L);
    }

    @Test
    void should_use_first_page_offset_in_pageable() {
        List<Integer> capturedOffsets = new ArrayList<>();

        Scroller.scroll(null, 10, (cursor, pageable) -> {
            capturedOffsets.add(pageable.getPageNumber());
            return List.of();
        });

        assertThat(capturedOffsets).containsExactly(0);
    }
}

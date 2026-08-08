package br.com.fiap.postech.adapter.output.persistence.helper.scroll;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Scroller {
    private static final long DEFAULT_CURSOR = 0;
    private static final int FIRST_PAGE = 0;
    private static final int EXTRA_CURSOR_ITEM = 1;

    private static long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return DEFAULT_CURSOR;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException ignored) {
            return DEFAULT_CURSOR;
        }
    }

    private static Pageable createPageable(Integer pageSize) {
        return PageRequest.of(FIRST_PAGE, pageSize + EXTRA_CURSOR_ITEM);
    }

    public static <T> ScrollPage<T> scroll(
            String cursor,
            Integer pageSize,
            BiFunction<Long, Pageable, List<T>> query
    ) {
        final var parsedCursor = parseCursor(cursor);
        final var pageable = createPageable(pageSize);
        final var result = query.apply(parsedCursor, pageable);

        if (result == null || result.isEmpty()) {
            return ScrollPage.<T>builder()
                    .data(new ArrayList<>())
                    .isLast(true)
                    .cursor(null)
                    .pageSize(pageSize)
                    .build();
        }

        final var isLast = result.size() <= pageSize;
        var data = result;
        String nextCursor = null;

        if (!isLast) {
            data = data.subList(0, pageSize);
            nextCursor = data.getLast().toString();
        }

        return ScrollPage.<T>builder()
                .data(data)
                .isLast(isLast)
                .cursor(nextCursor)
                .pageSize(pageSize)
                .build();
    }
}

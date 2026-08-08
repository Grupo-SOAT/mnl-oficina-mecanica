package br.com.fiap.postech.adapter.output.persistence.helper.scroll;

import lombok.Builder;

import java.util.List;

@Builder
public record ScrollPage<T>(List<T> data, String cursor, boolean isLast, int pageSize) {
}

package br.com.fiap.postech.adapter.input.reporting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
@AllArgsConstructor
public enum ReportType {
    PDF("pdf", MediaType.APPLICATION_PDF),
    CSV("csv", MediaType.valueOf("text/csv"));

    private static final String FILENAME_FORMAT = "%s.%s";

    private final String extension;
    private final MediaType mediaType;

    public String buildFilename(String baseName) {
        return String.format(FILENAME_FORMAT, baseName, extension);
    }
}

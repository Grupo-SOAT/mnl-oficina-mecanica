package br.com.fiap.postech.adapter.input.reporting.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportType Enum")
class ReportTypeTest {

    @Test
    @DisplayName("PDF deve retornar extensão 'pdf'")
    void should_return_pdf_extension() {
        assertThat(ReportType.PDF.getExtension()).isEqualTo("pdf");
    }

    @Test
    @DisplayName("CSV deve retornar extensão 'csv'")
    void should_return_csv_extension() {
        assertThat(ReportType.CSV.getExtension()).isEqualTo("csv");
    }

    @Test
    @DisplayName("PDF deve retornar media type application/pdf")
    void should_return_pdf_media_type() {
        assertThat(ReportType.PDF.getMediaType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    @DisplayName("CSV deve retornar media type text/csv")
    void should_return_csv_media_type() {
        assertThat(ReportType.CSV.getMediaType()).isEqualTo(MediaType.valueOf("text/csv"));
    }

    @ParameterizedTest(name = "{0} buildFilename com ''{1}'' deve retornar ''{2}''")
    @CsvSource({
            "PDF, service-1-average-time_2026-04-27_22-10-51, service-1-average-time_2026-04-27_22-10-51.pdf",
            "CSV, services-average-time_2026-04-27_22-10-51, services-average-time_2026-04-27_22-10-51.csv",
            "PDF, report, report.pdf",
            "CSV, export, export.csv"
    })
    @DisplayName("buildFilename deve formatar nome do arquivo com extensão apropriada")
    void should_build_filename_with_correct_extension(ReportType reportType, String baseName, String expectedFilename) {
        String result = reportType.buildFilename(baseName);

        assertThat(result).isEqualTo(expectedFilename);
    }

    @ParameterizedTest
    @EnumSource(ReportType.class)
    @DisplayName("Todos os tipos de relatório devem ter extensão e media type definidos")
    void should_have_extension_and_media_type_defined(ReportType reportType) {
        assertThat(reportType.getExtension())
                .isNotNull()
                .isNotBlank();

        assertThat(reportType.getMediaType())
                .isNotNull();
    }
}


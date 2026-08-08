package br.com.fiap.postech.domain.supply.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.supply.exception.DuplicatedSupplyException;
import br.com.fiap.postech.domain.supply.exception.NoMatchingSuppliesException;
import br.com.fiap.postech.domain.supply.exception.SupplyNotFoundException;
import br.com.fiap.postech.domain.supply.model.Supply;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class SupplyUseCaseTest {
    @Mock
    private SupplyPersistencePort persistencePort;

    @InjectMocks
    private SupplyUseCase useCase;

    @Test
    void should_delegate_scroll_to_persistence() {
        ScrollPage<Supply> expected = ScrollPage.<Supply>builder()
                .data(List.of(SupplyEntity.builder().id(1L).sku("SKU-1").build()))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();
        when(persistencePort.scroll("sku", 10, "5")).thenReturn(expected);

        ScrollPage<Supply> actual = useCase.scroll("sku", 10, "5");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty() {
        ScrollPage<Supply> empty = ScrollPage.<Supply>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("sku", 10, "5")).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll("sku", 10, "5"))
                .isInstanceOf(NoMatchingSuppliesException.class)
                .hasMessage("No matching supplies for sku: sku");
    }

    @Test
    void should_return_supply_when_found_by_id() {
        SupplyEntity supply = SupplyEntity.builder().id(1L).sku("SKU").build();
        when(persistencePort.findById(1L)).thenReturn(Optional.of(supply));

        Supply found = useCase.getById(1L);

        assertThat(found).isSameAs(supply);
    }

    @Test
    void should_throw_when_supply_not_found_by_id() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(99L))
                .isInstanceOf(SupplyNotFoundException.class)
                .hasMessage("Supply not found for id: 99");
    }

    @Test
    void should_throw_when_creating_duplicated_sku() {
        SupplyEntity supply = SupplyEntity.builder().sku("SKU-1").build();
        when(persistencePort.findBySku("SKU-1")).thenReturn(Optional.of(SupplyEntity.builder().sku("SKU-1").build()));

        assertThatThrownBy(() -> useCase.create(supply))
                .isInstanceOf(DuplicatedSupplyException.class)
                .hasMessage("Supply already exists for sku: SKU-1");

        verify(persistencePort, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("quantityDefaultsCases")
    void should_apply_quantity_defaults_on_create(
            Integer reserved,
            Integer available,
            int expectedReserved,
            int expectedAvailable
    ) {
        SupplyEntity input = SupplyEntity.builder()
                .sku("SKU-NEW")
                .reservedQuantity(reserved)
                .availableQuantity(available)
                .build();

        when(persistencePort.findBySku("SKU-NEW")).thenReturn(Optional.empty());
        when(persistencePort.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supply saved = useCase.create(input);

        assertThat(saved.getReservedQuantity()).isEqualTo(expectedReserved);
        assertThat(saved.getAvailableQuantity()).isEqualTo(expectedAvailable);
    }

    @Test
    void should_update_supply_preserving_created_at() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 10, 10, 0);
        SupplyEntity existing = SupplyEntity.builder().id(10L).createdAt(createdAt).build();
        SupplyEntity incoming = SupplyEntity.builder().sku("SKU-U").build();

        when(persistencePort.findById(10L)).thenReturn(Optional.of(existing));
        when(persistencePort.save(any(Supply.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supply updated = useCase.update(10L, incoming);

        assertThat(updated).isSameAs(incoming);
        assertThat(incoming.getId()).isEqualTo(10L);
        assertThat(incoming.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void should_throw_when_updating_non_existing_supply() {
        SupplyEntity incoming = SupplyEntity.builder().sku("SKU-X").build();
        when(persistencePort.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(101L, incoming))
                .isInstanceOf(SupplyNotFoundException.class)
                .hasMessage("Supply not found for id: 101");
    }

    @Test
    void should_delete_existing_supply() {
        when(persistencePort.existsById(20L)).thenReturn(true);

        useCase.delete(20L);

        verify(persistencePort).deleteById(20L);
    }

    @Test
    void should_throw_when_deleting_non_existing_supply() {
        when(persistencePort.existsById(22L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.delete(22L))
                .isInstanceOf(SupplyNotFoundException.class)
                .hasMessage("Supply not found for id: 22");

        verify(persistencePort, never()).deleteById(22L);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> quantityDefaultsCases() {
        return Stream.of(
                arguments(null, null, 0, 0),
                arguments(null, 5, 0, 5),
                arguments(7, null, 7, 0),
                arguments(7, 5, 7, 5)
        );
    }
}

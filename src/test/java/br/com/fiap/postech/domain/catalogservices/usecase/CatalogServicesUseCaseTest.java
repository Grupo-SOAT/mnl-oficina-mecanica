package br.com.fiap.postech.domain.catalogservices.usecase;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.catalogservices.exception.DuplicatedCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.exception.NoMatchingCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CatalogServicesUseCaseTest {
    @Mock
    private CatalogServicesPersistencePort persistencePort;

    @Mock
    private SupplyPersistencePort supplyPersistencePort;

    @InjectMocks
    private CatalogServicesUseCase useCase;

    @Test
    void should_delegate_scroll_to_persistence() {
        ScrollPage<CatalogServices> expected = ScrollPage.<CatalogServices>builder()
                .data(List.of(CatalogServicesEntity.builder().id(1L).name("Servico").build()))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, "ser", 10, "5")).thenReturn(expected);

        ScrollPage<CatalogServices> actual = useCase.scroll(null, "ser", 10, "5");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty() {
        ScrollPage<CatalogServices> empty = ScrollPage.<CatalogServices>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, "ser", 10, "5")).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll(null, "ser", 10, "5"))
                .isInstanceOf(NoMatchingCatalogServiceException.class)
                .hasMessage("No matching catalog service for name: ser");
    }

    @Test
    void should_return_catalog_services_when_found_by_id() {
        CatalogServicesEntity catalogServices = CatalogServicesEntity.builder().id(1L).name("Servico").build();
        when(persistencePort.findById(1L)).thenReturn(Optional.of(catalogServices));

        CatalogServices found = useCase.getById(1L);

        assertThat(found).isSameAs(catalogServices);
    }

    @Test
    void should_return_catalog_services_with_supplies_when_found_by_id() {
        CatalogServicesEntity catalogServices = CatalogServicesEntity.builder()
                .id(1L)
                .name("Servico")
                .supplies(List.of(NeededSupplyEntity.builder()
                        .servicesSuppliesId(1L)
                        .supplyAmount(100)
                        .build()))
                .build();

        when(persistencePort.findById(1L)).thenReturn(Optional.of(catalogServices));

        CatalogServices found = useCase.getById(1L);

        assertThat(found).isSameAs(catalogServices);
    }

    @Test
    void should_throw_when_catalog_services_not_found_by_id() {
        when(persistencePort.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(9L))
                .isInstanceOf(CatalogServiceNotFoundException.class)
                .hasMessage("Catalog services not found for id: 9");
    }

    @Test
    void should_throw_when_creating_duplicated_service_name() {
        CatalogServicesEntity supply = CatalogServicesEntity.builder().name("Servico").basePrice(BigDecimal.TEN).build();
        when(persistencePort.findByName("Servico")).thenReturn(Optional.of(CatalogServicesEntity.builder().name("Servico").build()));

        assertThatThrownBy(() -> useCase.create(supply))
                .isInstanceOf(DuplicatedCatalogServiceException.class)
                .hasMessage("Catalog services already exists for name: Servico");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_updating_non_existing_catalog_services() {
        CatalogServicesEntity incoming = CatalogServicesEntity.builder().name("Servico").basePrice(BigDecimal.TEN).build();
        when(persistencePort.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(10L, incoming))
                .isInstanceOf(CatalogServiceNotFoundException.class)
                .hasMessage("Catalog services not found for id: 10");
    }

    @Test
    void should_delete_existing_catalog_servies() {
        when(persistencePort.existsById(20L)).thenReturn(true);

        useCase.delete(20L);

        verify(persistencePort).deleteById(20L);
    }

    @Test
    void should_throw_when_deleting_non_existing_catalog_services() {
        when(persistencePort.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.delete(2L))
                .isInstanceOf(CatalogServiceNotFoundException.class)
                .hasMessage("Catalog services not found for id: 2");

        verify(persistencePort, never()).deleteById(22L);
    }

    @Test
    void should_create_valid_catalog_service() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(null)
                .build();
        when(persistencePort.findByName("Servico")).thenReturn(Optional.empty());
        when(persistencePort.save(input)).thenReturn(input);

        CatalogServices saved = useCase.create(input);

        assertThat(saved).isSameAs(input);
    }

    @Test
    void should_reject_create_with_null_name() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name(null)
                .basePrice(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServiceNameException.class);
    }

    @Test
    void should_reject_create_with_blank_name() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("   ")
                .basePrice(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServiceNameException.class);
    }

    @Test
    void should_reject_create_with_null_price() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(null)
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServicePriceException.class);
    }

    @Test
    void should_reject_create_with_zero_price() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.ZERO)
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServicePriceException.class);
    }

    @Test
    void should_update_catalog_service() {
        CatalogServicesEntity existing = CatalogServicesEntity.builder().id(5L).name("Antigo").build();
        CatalogServicesEntity incoming = CatalogServicesEntity.builder()
                .name("Novo")
                .basePrice(BigDecimal.TEN)
                .build();
        when(persistencePort.findById(5L)).thenReturn(Optional.of(existing));
        when(persistencePort.save(any(CatalogServices.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogServices updated = useCase.update(5L, incoming);

        assertThat(updated.getId()).isEqualTo(5L);
    }

    @Test
    void should_reject_update_with_invalid_name() {
        CatalogServicesEntity incoming = CatalogServicesEntity.builder()
                .name("   ")
                .basePrice(BigDecimal.TEN)
                .build();

        assertThatThrownBy(() -> useCase.update(5L, incoming))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServiceNameException.class);
    }

    @Test
    void should_reject_update_with_invalid_price() {
        CatalogServicesEntity incoming = CatalogServicesEntity.builder()
                .name("Novo")
                .basePrice(BigDecimal.valueOf(-1))
                .build();

        assertThatThrownBy(() -> useCase.update(5L, incoming))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServicePriceException.class);
    }

    @Test
    void should_accept_supply_without_referenced_supply() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(List.of(NeededSupplyEntity.builder()
                        .supply(null)
                        .supplyAmount(5)
                        .build()))
                .build();
        when(persistencePort.findByName("Servico")).thenReturn(Optional.empty());
        when(persistencePort.save(input)).thenReturn(input);

        CatalogServices saved = useCase.create(input);

        assertThat(saved).isSameAs(input);
        verify(supplyPersistencePort, never()).findById(any());
    }

    @Test
    void should_accept_supply_when_referenced_supply_exists() {
        var referenced = br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity.builder()
                .id(7L)
                .build();
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(List.of(NeededSupplyEntity.builder()
                        .supply(referenced)
                        .supplyAmount(5)
                        .build()))
                .build();
        when(supplyPersistencePort.findById(7L)).thenReturn(Optional.of(referenced));
        when(persistencePort.findByName("Servico")).thenReturn(Optional.empty());
        when(persistencePort.save(input)).thenReturn(input);

        CatalogServices saved = useCase.create(input);

        assertThat(saved).isSameAs(input);
    }

    @Test
    void should_reject_supply_when_referenced_supply_is_missing() {
        var referenced = br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity.builder()
                .id(7L)
                .build();
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(List.of(NeededSupplyEntity.builder()
                        .supply(referenced)
                        .supplyAmount(5)
                        .build()))
                .build();
        when(supplyPersistencePort.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(CatalogServiceNotFoundException.class);
    }

    @Test
    void should_reject_supply_with_zero_amount() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(List.of(NeededSupplyEntity.builder()
                        .supply(null)
                        .supplyAmount(0)
                        .build()))
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidSupplyQuantityException.class);
    }

    @Test
    void should_reject_supply_with_null_amount() {
        CatalogServicesEntity input = CatalogServicesEntity.builder()
                .name("Servico")
                .basePrice(BigDecimal.TEN)
                .supplies(List.of(NeededSupplyEntity.builder()
                        .supply(null)
                        .supplyAmount(null)
                        .build()))
                .build();

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(br.com.fiap.postech.domain.catalogservices.exception.InvalidSupplyQuantityException.class);
    }
}

package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.service.exception.NoMatchingServicesException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServiceUseCaseTest {

    @Mock
    private ServicePersistencePort persistencePort;

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private CatalogServicesPersistencePort catalogServicesPersistencePort;

    @Mock
    private SupplyPersistencePort supplyPersistencePort;

    @Mock
    private ServiceStatusLabelPort statusLabelPort;

    @InjectMocks
    private ServiceUseCase useCase;

    @Test
    void should_delegate_scroll_to_persistence() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ScrollPage<Service> expected = ScrollPage.<Service>builder()
                .data(List.of(ServiceEntity.builder().id(1L).build()))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();
        when(persistencePort.scroll(1L, null, null, 10, "0")).thenReturn(expected);

        ScrollPage<Service> actual = useCase.scroll(1L, null, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty() {
        when(serviceOrderPersistencePort.findById(99L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ScrollPage<Service> empty = ScrollPage.<Service>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(99L, null, null, 10, null)).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll(99L, null, null, 10, null))
                .isInstanceOf(NoMatchingServicesException.class)
                .hasMessage("No matching services for service order id: 99");
    }

    @Test
    void should_return_service_when_found_by_id() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(1L).build();
        when(persistencePort.findByIdAndServiceOrderId(5L, 1L)).thenReturn(Optional.of(entity));

        Service found = useCase.getById(1L, 5L);

        assertThat(found).isSameAs(entity);
    }

    @Test
    void should_throw_when_service_not_found_by_id() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(persistencePort.findByIdAndServiceOrderId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(1L, 99L))
                .isInstanceOf(ServiceNotFoundException.class)
                .hasMessage("Service not found for id: 99");
    }

    @Test
    void should_create_service_with_awaiting_approval_status_and_service_order_id() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.catalogservices.model.CatalogServices.class)));
        ServiceEntity input = ServiceEntity.builder()
                .catalogServiceId(10L)
                .price(new BigDecimal("150.00"))
                .build();
        when(persistencePort.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        when(statusLabelPort.resolve("AWAITING_APPROVAL")).thenReturn("Aguardando Aprovação");

        Service saved = useCase.create(1L, input);

        assertThat(saved.getServiceOrderId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo("AWAITING_APPROVAL");
        assertThat(saved.getStatusLabel()).isEqualTo("Aguardando Aprovação");
    }

    @Test
    void should_throw_when_catalog_service_not_found() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.empty());
        ServiceEntity input = ServiceEntity.builder().catalogServiceId(10L).price(new BigDecimal("200.00")).build();

        assertThatThrownBy(() -> useCase.create(1L, input))
                .isInstanceOf(CatalogServiceNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void should_throw_when_service_order_not_found_on_create() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.empty());
        ServiceEntity input = ServiceEntity.builder().catalogServiceId(10L).price(new BigDecimal("150.00")).build();

        assertThatThrownBy(() -> useCase.create(1L, input))
                .isInstanceOf(br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException.class);
    }

    @Test
    void should_update_service_preserving_service_order_id() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.catalogservices.model.CatalogServices.class)));
        ServiceEntity existing = ServiceEntity.builder().id(5L).serviceOrderId(1L).build();
        ServiceEntity incoming = ServiceEntity.builder().catalogServiceId(10L).price(new BigDecimal("200.00")).build();
        when(persistencePort.findByIdAndServiceOrderId(5L, 1L)).thenReturn(Optional.of(existing));
        when(persistencePort.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));

        Service updated = useCase.update(1L, 5L, incoming);

        assertThat(updated.getId()).isEqualTo(5L);
        assertThat(updated.getServiceOrderId()).isEqualTo(1L);
    }

    @Test
    void should_throw_when_updating_non_existing_service() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ServiceEntity incoming = ServiceEntity.builder().catalogServiceId(10L).build();
        when(persistencePort.findByIdAndServiceOrderId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(1L, 99L, incoming))
                .isInstanceOf(ServiceNotFoundException.class)
                .hasMessage("Service not found for id: 99");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_delete_existing_service() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(1L).build();
        when(persistencePort.findByIdAndServiceOrderId(5L, 1L)).thenReturn(Optional.of(entity));

        useCase.delete(1L, 5L);

        verify(persistencePort).deleteById(5L);
    }

    @Test
    void should_throw_when_deleting_non_existing_service() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(persistencePort.findByIdAndServiceOrderId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(1L, 99L))
                .isInstanceOf(ServiceNotFoundException.class)
                .hasMessage("Service not found for id: 99");

        verify(persistencePort, never()).deleteById(any());
    }

    // Testes adicionais para cobertura de branches
    @Test
    void should_throw_when_service_order_not_found_on_scroll() {
        when(serviceOrderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.scroll(99L, null, null, 10, null))
                .isInstanceOf(br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException.class);
    }

    @Test
    void should_throw_when_service_order_not_found_on_get_by_id() {
        when(serviceOrderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(99L, 5L))
                .isInstanceOf(br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException.class);
    }

    @Test
    void should_throw_when_catalog_service_not_found_on_update() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.empty());
        ServiceEntity existing = ServiceEntity.builder().id(5L).serviceOrderId(1L).build();
        ServiceEntity incoming = ServiceEntity.builder().catalogServiceId(10L).price(new BigDecimal("200.00")).build();
        when(persistencePort.findByIdAndServiceOrderId(5L, 1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.update(1L, 5L, incoming))
                .isInstanceOf(CatalogServiceNotFoundException.class);

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_service_order_not_found_on_update() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.empty());
        ServiceEntity incoming = ServiceEntity.builder().catalogServiceId(10L).price(new BigDecimal("200.00")).build();

        assertThatThrownBy(() -> useCase.update(1L, 5L, incoming))
                .isInstanceOf(br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException.class);
    }

    @Test
    void should_throw_when_service_order_not_found_on_delete() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(1L, 5L))
                .isInstanceOf(br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException.class);
    }

    @Test
    void should_handle_scroll_with_service_id_filter() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ScrollPage<Service> expected = ScrollPage.<Service>builder()
                .data(List.of(ServiceEntity.builder().id(5L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(1L, 5L, null, 10, "0")).thenReturn(expected);

        ScrollPage<Service> actual = useCase.scroll(1L, 5L, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_handle_scroll_with_status_filter() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ScrollPage<Service> expected = ScrollPage.<Service>builder()
                .data(List.of(ServiceEntity.builder().id(1L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(1L, null, "COMPLETED", 10, "0")).thenReturn(expected);

        ScrollPage<Service> actual = useCase.scroll(1L, null, "COMPLETED", 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_handle_scroll_with_multiple_filters() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        ScrollPage<Service> expected = ScrollPage.<Service>builder()
                .data(List.of(ServiceEntity.builder().id(1L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(1L, 5L, "COMPLETED", 10, "0")).thenReturn(expected);

        ScrollPage<Service> actual = useCase.scroll(1L, 5L, "COMPLETED", 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_when_create_service_with_null_price() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.catalogservices.model.CatalogServices.class)));

        ServiceEntity input = ServiceEntity.builder()
                .catalogServiceId(10L)
                .price(null)
                .build();

        assertThatThrownBy(() -> useCase.create(1L, input))
                .isInstanceOf(ServiceUseCase.InvalidServiceException.class)
                .hasMessage("INVALID_PRICE");

        verify(persistencePort, never()).save(any(Service.class));
    }

    @Test
    void should_create_service_from_catalog_with_price_and_supplies() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));

        var catalog = mock(br.com.fiap.postech.domain.catalogservices.model.CatalogServices.class);
        when(catalog.getBasePrice()).thenReturn(new BigDecimal("200.00"));

        var catalogSupply = mock(br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity.class);
        var supplyEntity = mock(br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity.class);
        when(supplyEntity.getId()).thenReturn(5L);
        when(catalogSupply.getSupply()).thenReturn(supplyEntity);
        when(catalogSupply.getSupplyAmount()).thenReturn(3);
        when(catalog.getSupplies()).thenReturn(List.of(catalogSupply));

        when(catalogServicesPersistencePort.findById(10L)).thenReturn(Optional.of(catalog));
        when(supplyPersistencePort.findById(5L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.supply.model.Supply.class)));
        when(persistencePort.save(any(Service.class))).thenAnswer(inv -> inv.getArgument(0));
        when(statusLabelPort.resolve("AWAITING_APPROVAL")).thenReturn("Aguardando Aprovação");

        Service saved = useCase.createFromCatalog(1L, 10L);

        assertThat(saved.getServiceOrderId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo("AWAITING_APPROVAL");
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(saved.getNeededSupplies()).hasSize(1);
        assertThat(saved.getNeededSupplies().get(0).getIdSupply()).isEqualTo(5L);
        assertThat(saved.getNeededSupplies().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void should_throw_when_catalog_service_not_found_on_create_from_catalog() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(mock(br.com.fiap.postech.domain.serviceorder.model.ServiceOrder.class)));
        when(catalogServicesPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.createFromCatalog(1L, 99L))
                .isInstanceOf(CatalogServiceNotFoundException.class);
    }
}

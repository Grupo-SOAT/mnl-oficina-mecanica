package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstimateServiceOrderAmountUseCaseTest {

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @Mock
    private SupplyPersistencePort supplyPersistencePort;

    @InjectMocks
    private EstimateServiceOrderAmountUseCase useCase;

    @Test
    void should_compute_total_from_service_prices_and_supply_unit_prices_multiplied_by_quantity() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();

        var supply1 = SupplyEntity.builder()
                .id(10L)
                .unitPrice(new BigDecimal("30.00"))
                .build();
        var supply2 = SupplyEntity.builder()
                .id(20L)
                .unitPrice(new BigDecimal("20.00"))
                .build();

        var service1 = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(1L)
                .price(new BigDecimal("100.00"))
                .build();
        service1.setNeededSupplies(List.of(
                NeededSupply.builder().idSupply(10L).quantity(2).build()
        ));

        var service2 = ServiceEntity.builder()
                .id(2L)
                .serviceOrderId(1L)
                .price(new BigDecimal("50.00"))
                .build();
        service2.setNeededSupplies(List.of(
                NeededSupply.builder().idSupply(20L).quantity(1).build()
        ));

        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of(service1, service2));
        when(supplyPersistencePort.findAllById(List.of(10L, 20L))).thenReturn(List.of(supply1, supply2));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.estimate(1L);

        // 100 + (30*2) + 50 + (20*1) = 100 + 60 + 50 + 20 = 230
        assertThat(serviceOrder.getEstimatedAmount()).isEqualByComparingTo(new BigDecimal("230.00"));
    }

    @Test
    void should_use_service_prices_only_when_no_supplies() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(2L)
                .status("AWAITING_APPROVAL")
                .build();

        var service1 = ServiceEntity.builder()
                .id(3L)
                .serviceOrderId(2L)
                .price(new BigDecimal("150.00"))
                .build();
        var service2 = ServiceEntity.builder()
                .id(4L)
                .serviceOrderId(2L)
                .price(new BigDecimal("75.00"))
                .build();

        when(serviceOrderPersistencePort.findById(2L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(2L)).thenReturn(List.of(service1, service2));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.estimate(2L);

        assertThat(serviceOrder.getEstimatedAmount()).isEqualByComparingTo(new BigDecimal("225.00"));
    }

    @Test
    void should_set_zero_when_no_services() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(3L)
                .status("AWAITING_APPROVAL")
                .build();

        when(serviceOrderPersistencePort.findById(3L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(3L)).thenReturn(List.of());
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.estimate(3L);

        assertThat(serviceOrder.getEstimatedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_throw_when_service_order_not_found() {
        when(serviceOrderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.estimate(99L))
                .isInstanceOf(ServiceOrderNotFoundException.class);
    }
}

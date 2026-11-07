package br.com.fiap.postech.adapter.output.supply.persistence;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.adapter.output.supply.persistence.repository.SupplyRepository;
import br.com.fiap.postech.domain.supply.model.Supply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import br.com.fiap.postech.adapter.output.persistence.SpecificationProbe;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupplyPersistenceAdapterTest {
    @Mock
    private SupplyRepository repository;

    @InjectMocks
    private SupplyPersistenceAdapter adapter;

    @Test
    void should_scroll_without_sku_filter_using_find_all_after_cursor() {
        SupplyEntity first = SupplyEntity.builder().id(11L).sku("A").build();
        SupplyEntity second = SupplyEntity.builder().id(12L).sku("B").build();
        SupplyEntity third = SupplyEntity.builder().id(13L).sku("C").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, third)));

        ScrollPage<Supply> page = adapter.scroll(null, 2, "10");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));

        assertThat(page.data()).hasSize(2);
        assertThat(page.isLast()).isFalse();
    }

    @Test
    void should_scroll_with_sku_filter_using_find_by_sku_after_cursor() {
        SupplyEntity only = SupplyEntity.builder().id(1L).sku("SKU-X").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(only)));

        ScrollPage<Supply> page = adapter.scroll("SKU", 2, "invalid-cursor");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));

        assertThat(page.data()).hasSize(1);
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_map_find_by_id() {
        SupplyEntity entity = SupplyEntity.builder().id(5L).sku("SKU5").build();
        when(repository.findById(5L)).thenReturn(Optional.of(entity));

        Optional<Supply> found = adapter.findById(5L);

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_map_find_by_sku() {
        SupplyEntity entity = SupplyEntity.builder().id(8L).sku("SKU8").build();
        when(repository.findBySku("SKU8")).thenReturn(Optional.of(entity));

        Optional<Supply> found = adapter.findBySku("SKU8");

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_save_when_supply_already_entity() {
        SupplyEntity entity = SupplyEntity.builder().id(9L).sku("SKU9").build();
        when(repository.save(entity)).thenReturn(entity);

        Supply saved = adapter.save(entity);

        assertThat(saved).isSameAs(entity);
    }

    @Test
    void should_save_when_supply_is_not_entity() {
        Supply supply = new TestSupply(40L, "SKU40", "Nome", "Desc", new BigDecimal("77.10"), 2L, 3, 4,
                LocalDateTime.of(2024, 12, 1, 10, 0));
        SupplyEntity expected = SupplyEntity.builder()
            .id(40L)
            .sku("SKU40")
            .name("Nome")
            .description("Desc")
            .unitPrice(new BigDecimal("77.10"))
            .suppliedBy(2L)
            .reservedQuantity(3)
            .availableQuantity(4)
            .createdAt(LocalDateTime.of(2024, 12, 1, 10, 0))
            .build();

        when(repository.save(any(SupplyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supply saved = adapter.save(supply);

        assertThat(saved)
            .isNotNull()
            .isInstanceOf(SupplyEntity.class)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void should_delegate_delete_by_id() {
        adapter.deleteById(77L);
        verify(repository).deleteById(77L);
    }

    @Test
    void should_delegate_exists_by_id() {
        when(repository.existsById(55L)).thenReturn(true);

        boolean exists = adapter.existsById(55L);

        assertThat(exists).isTrue();
    }

    @Test
    void should_report_absence_when_supply_does_not_exist() {
        when(repository.existsById(55L)).thenReturn(false);

        assertThat(adapter.existsById(55L)).isFalse();
    }

    @Test
    void should_find_all_supplies_by_ids() {
        SupplyEntity first = SupplyEntity.builder().id(1L).sku("SKU1").build();
        SupplyEntity second = SupplyEntity.builder().id(2L).sku("SKU2").build();
        when(repository.findAllById(List.of(1L, 2L))).thenReturn(List.of(first, second));

        assertThat(adapter.findAllById(List.of(1L, 2L)))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(first, second);
    }

    @Test
    void should_return_empty_list_when_no_supplies_match_ids() {
        when(repository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThat(adapter.findAllById(List.of(99L))).isEmpty();
    }

    @Test
    void should_build_empty_specification_when_sku_is_blank_and_cursor_is_zero() {
        SpecificationProbe probe = evaluateScrollSpecification("   ", null);

        assertThat(probe.andPredicates()).isEmpty();
        probe.verifyOrderWasApplied();
    }

    @Test
    void should_build_sku_predicate_when_sku_is_informed() {
        SpecificationProbe probe = evaluateScrollSpecification("ABC", null);

        assertThat(probe.andPredicates()).hasSize(1);
        verify(probe.builder()).like(any(), eq("%abc%"));
    }

    @Test
    void should_build_cursor_predicate_when_cursor_is_positive() {
        SpecificationProbe probe = evaluateScrollSpecification(null, "10");

        assertThat(probe.andPredicates()).hasSize(1);
        verify(probe.builder()).greaterThan(any(), eq(10L));
    }

    @Test
    void should_build_sku_and_cursor_predicates_together() {
        SpecificationProbe probe = evaluateScrollSpecification("ABC", "10");

        assertThat(probe.andPredicates()).hasSize(2);
    }

    private SpecificationProbe evaluateScrollSpecification(String sku, String cursor) {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.scroll(sku, 10, cursor);

        ArgumentCaptor<Specification<SupplyEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(Pageable.class));
        return SpecificationProbe.evaluate(captor.getValue());
    }

    private static final class TestSupply implements Supply {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal unitPrice;
        private Long suppliedBy;
        private Integer reservedQuantity;
        private Integer availableQuantity;
        private LocalDateTime createdAt;

        private TestSupply(Long id,
                           String sku,
                           String name,
                           String description,
                           BigDecimal unitPrice,
                           Long suppliedBy,
                           Integer reservedQuantity,
                           Integer availableQuantity,
                           LocalDateTime createdAt) {
            this.id = id;
            this.sku = sku;
            this.name = name;
            this.description = description;
            this.unitPrice = unitPrice;
            this.suppliedBy = suppliedBy;
            this.reservedQuantity = reservedQuantity;
            this.availableQuantity = availableQuantity;
            this.createdAt = createdAt;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public String getSku() {
            return sku;
        }

        @Override
        public void setSku(String sku) {
            this.sku = sku;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        @Override
        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        @Override
        public Long getSuppliedBy() {
            return suppliedBy;
        }

        @Override
        public void setSuppliedBy(Long suppliedBy) {
            this.suppliedBy = suppliedBy;
        }

        @Override
        public Integer getReservedQuantity() {
            return reservedQuantity;
        }

        @Override
        public void setReservedQuantity(Integer reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }

        @Override
        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        @Override
        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Override
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}

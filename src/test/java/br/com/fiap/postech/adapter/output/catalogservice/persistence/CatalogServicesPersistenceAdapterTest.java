package br.com.fiap.postech.adapter.output.catalogservice.persistence;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.repository.CatalogServicesRepository;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogServicesPersistenceAdapterTest {
    @Mock
    private CatalogServicesRepository repository;

    @InjectMocks
    private CatalogServicesPersistenceAdapter adapter;

    @Test
    void should_scroll_without_name_filter_using_specification() {
        CatalogServicesEntity first = CatalogServicesEntity.builder().id(1L).name("A").build();
        CatalogServicesEntity second = CatalogServicesEntity.builder().id(2L).name("B").build();
        CatalogServicesEntity third = CatalogServicesEntity.builder().id(3L).name("C").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, third)));

        ScrollPage<CatalogServices> page = adapter.scroll(null, null, 2, "10");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);

        assertThat(page.data()).hasSize(2);
        assertThat(page.isLast()).isFalse();
        assertThat(page.cursor()).isEqualTo(second.toString());
    }

    @Test
    void should_scroll_with_name_filter_using_specification() {
        CatalogServicesEntity only = CatalogServicesEntity.builder().id(1L).name("Servico").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(only)));

        ScrollPage<CatalogServices> page = adapter.scroll(null, "Servi", 2, "invalid-cursor");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));

        assertThat(page.data()).hasSize(1);
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_map_find_by_id() {
        CatalogServicesEntity entity = CatalogServicesEntity.builder().id(20L).name("Servico").build();
        when(repository.findWithSuppliesById(20L)).thenReturn(Optional.of(entity));

        Optional<CatalogServices> found = adapter.findById(20L);

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_map_find_by_name() {
        CatalogServicesEntity entity = CatalogServicesEntity.builder().id(78L).name("Servico").supplies(List.of(NeededSupplyEntity.builder().servicesSuppliesId(1L).supplyAmount(300).build())).build();
        when(repository.findByName("Servico")).thenReturn(Optional.of(entity));

        Optional<CatalogServices> found = adapter.findByName("Servico");

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_save_when_catalog_services_already_entity() {
        CatalogServicesEntity entity = CatalogServicesEntity.builder().id(7L).name("Servico").supplies(List.of(NeededSupplyEntity.builder().servicesSuppliesId(1L).supplyAmount(300).build())).build();
        when(repository.save(entity)).thenReturn(entity);

        CatalogServices saved = adapter.save(entity);

        assertThat(saved).isSameAs(entity);
    }

    @Test
    void should_save_when_catalog_services_already_entity_supplies_null() {
        CatalogServicesEntity entity = CatalogServicesEntity.builder().id(7L).name("Servico").supplies(null).build();
        when(repository.save(entity)).thenReturn(entity);

        CatalogServices saved = adapter.save(entity);

        assertThat(saved).isSameAs(entity);
    }

    @Test
    void should_save_when_catalog_services_is_not_entity() {
        CatalogServices supply = new CatalogServicesPersistenceAdapterTest.TestCatalogServices(4L, "Servico", "Servico", new BigDecimal("7.1"), new ArrayList<>());
        CatalogServicesEntity expected = CatalogServicesEntity.builder()
                .id(4L)
                .name("SERVICO")
                .description("Servico")
                .basePrice(new BigDecimal("7.1"))
                .supplies(new ArrayList<>())
                .build();

        when(repository.save(any(CatalogServicesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogServices saved = adapter.save(supply);

        assertThat(saved)
                .isNotNull()
                .isInstanceOf(CatalogServicesEntity.class)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_delegate_delete_by_id() {
        adapter.deleteById(58L);
        verify(repository).deleteById(58L);
    }

    @Test
    void should_delegate_exists_by_id() {
        when(repository.existsById(5L)).thenReturn(true);

        boolean exists = adapter.existsById(5L);

        assertThat(exists).isTrue();
    }

    private static final class TestCatalogServices implements CatalogServices {
        private Long id;
        private String name;
        private String description;
        private BigDecimal basePrice;
        private List<NeededSupplyEntity> supplies;

        private TestCatalogServices(
                Long id,
                String name,
                String description,
                BigDecimal basePrice, List<NeededSupplyEntity> supplies
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.basePrice = basePrice;
            this.supplies = supplies;
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
        public BigDecimal getBasePrice() {
            return basePrice;
        }

        @Override
        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        @Override
        public List<NeededSupplyEntity> getSupplies() {
            return supplies;
        }

        @Override
        public void setSupplies(List<NeededSupplyEntity> neededSupplies) {
            this.supplies = neededSupplies;
        }
    }
}

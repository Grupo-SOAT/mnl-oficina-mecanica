package br.com.fiap.postech.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.BudgetApprovalTokenEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.BudgetApprovalTokenRepository;
import br.com.fiap.postech.domain.serviceorder.model.BudgetApprovalToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetApprovalTokenPersistenceAdapterTest {

    @Mock
    private BudgetApprovalTokenRepository repository;

    @InjectMocks
    private BudgetApprovalTokenPersistenceAdapter adapter;

    @Test
    void should_return_token_when_found_by_service_order_id() {
        var entity = BudgetApprovalTokenEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .token("abc-123")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();
        when(repository.findByServiceOrderId(10L)).thenReturn(Optional.of(entity));

        Optional<BudgetApprovalToken> result = adapter.findByServiceOrderId(10L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().serviceOrderId()).isEqualTo(10L);
        assertThat(result.get().token()).isEqualTo("abc-123");
    }

    @Test
    void should_return_empty_when_token_not_found() {
        when(repository.findByServiceOrderId(99L)).thenReturn(Optional.empty());

        Optional<BudgetApprovalToken> result = adapter.findByServiceOrderId(99L);

        assertThat(result).isEmpty();
    }
}

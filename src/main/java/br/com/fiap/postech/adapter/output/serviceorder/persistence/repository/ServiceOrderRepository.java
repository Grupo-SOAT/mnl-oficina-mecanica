package br.com.fiap.postech.adapter.output.serviceorder.persistence.repository;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrderEntity, Long>, JpaSpecificationExecutor<ServiceOrderEntity> {
}

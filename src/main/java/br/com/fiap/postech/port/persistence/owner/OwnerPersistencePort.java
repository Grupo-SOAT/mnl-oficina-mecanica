package br.com.fiap.postech.port.persistence.owner;

import java.util.Optional;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.model.Owner;

public interface OwnerPersistencePort {

    ScrollPage<Owner> scroll(String email, Integer pageSize, String cursor);

    Optional<Owner> findById(Long id);

    Optional<Owner> findByDocument(String document);

    Owner save(Owner owner);

    void deleteById(Long id);

    boolean existsById(Long id);
    
}

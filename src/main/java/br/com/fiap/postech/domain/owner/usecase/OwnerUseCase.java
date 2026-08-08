package br.com.fiap.postech.domain.owner.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.exception.DuplicatedOwnerException;
import br.com.fiap.postech.domain.owner.exception.InvalidDocumentException;
import br.com.fiap.postech.domain.owner.exception.InvalidEmailException;
import br.com.fiap.postech.domain.owner.exception.NoMatchingOwnersException;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.owner.validation.DocumentValidator;
import br.com.fiap.postech.domain.owner.validation.EmailValidator;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;

public class OwnerUseCase {
    private final OwnerPersistencePort persistencePort;

    public OwnerUseCase(OwnerPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    public ScrollPage<Owner> scroll(String document, Integer pageSize, String cursor) {
        final var result = persistencePort.scroll(document, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingOwnersException(document);
        }

        return result;
    }

    public Owner getById(Long id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException(id));
    }

    public Owner create(Owner owner) {
        validateDocument(owner);
        validateEmail(owner);

        persistencePort.findByDocument(owner.getDocument()).ifPresent(s -> {
            throw new DuplicatedOwnerException(owner.getDocument());
        });

        return persistencePort.save(owner);
    }

    public Owner update(Long id, Owner owner) {
        validateDocument(owner);
        validateEmail(owner);

        persistencePort.findByDocument(owner.getDocument())
            .ifPresent(existingOwner -> {
                if (!existingOwner.getId().equals(id)) {
                    throw new DuplicatedOwnerException(owner.getDocument());
                }
            });

        final var existing = persistencePort.findById(id)
                .orElseThrow(() -> new OwnerNotFoundException(id));
        owner.setId(id);
        owner.setCreatedAt(existing.getCreatedAt());

        return persistencePort.save(owner);
    }

    public void delete(Long id) {
        if (!persistencePort.existsById(id)) {
            throw new OwnerNotFoundException(id);
        }

        persistencePort.deleteById(id);
    }

    private void validateDocument(Owner owner) {
        boolean valid = DocumentValidator.isValid(
                owner.getDocument(),
                owner.getDocumentType()
        );

        if (!valid) {
            throw new InvalidDocumentException(owner.getDocument());
        }
    }

    private void validateEmail(Owner owner) {

        if (!EmailValidator.isValid(owner.getEmail())) {
            throw new InvalidEmailException(owner.getEmail());
        }
    }
}

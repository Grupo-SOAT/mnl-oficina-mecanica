package br.com.fiap.postech.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * ROLE_CLIENTE (emitida pela lambda de auth por CPF) e liberada em
 * RolePermissions para GET /owners/:id e GET /service-orders/:id, mas sem
 * escopo por dono - o RoleAuthorizationFilter so sabe checar o papel, nao
 * qual :id o cliente pode ver. Este guard fecha essa lacuna: chamado pelos
 * controllers depois de carregar o recurso, ele so age quando o papel
 * autenticado e ROLE_CLIENTE (staff continua sem restricao nenhuma).
 */
public final class ClienteScopeGuard {

    private static final String CLIENTE_AUTHORITY = "ROLE_CLIENTE";

    private ClienteScopeGuard() {
    }

    public static void assertOwnsResource(Long resourceOwnerId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return;
        }

        boolean isCliente = authentication.getAuthorities().stream()
                .anyMatch(authority -> CLIENTE_AUTHORITY.equals(authority.getAuthority()));

        if (!isCliente) {
            return;
        }

        Long callerOwnerId;
        try {
            callerOwnerId = toLong(authentication.getCredentials());
        } catch (RuntimeException e) {
            throw new ClienteResourceAccessDeniedException();
        }

        if (callerOwnerId == null || resourceOwnerId == null || !Objects.equals(callerOwnerId, resourceOwnerId)) {
            throw new ClienteResourceAccessDeniedException();
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }
}

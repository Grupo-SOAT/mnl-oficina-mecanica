package br.com.fiap.postech.config;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class RolePermissions {

    public static final Map<String, List<String>> PERMISSIONS = Map.ofEntries(

            // =========================
            // USERS
            // =========================
            Map.entry("GET_/users", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_STOREKEEPER")),
            Map.entry("POST_/users", List.of("ROLE_ADMIN")),
            Map.entry("GET_/users/:id", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_STOREKEEPER")),
            Map.entry("PATCH_/users/:id", List.of("ROLE_ADMIN")),
            Map.entry("DELETE_/users/:id", List.of("ROLE_ADMIN")),

            // =========================
            // OWNERS
            // =========================
            Map.entry("GET_/owners", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("POST_/owners", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("GET_/owners/:id", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("PATCH_/owners/:id", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("DELETE_/owners/:id", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),

            // =========================
            // VEHICLES
            // =========================
            Map.entry("GET_/vehicles", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("POST_/vehicles", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("GET_/vehicles/:id", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("PATCH_/vehicles/:id", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("DELETE_/vehicles/:id", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),

            // =========================
            // SERVICES
            // =========================
            Map.entry("GET_/catalog/services", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("POST_/catalog/services", List.of("ROLE_ADMIN")),
            Map.entry("GET_/catalog/services/:id", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("PATCH_/catalog/services/:id", List.of("ROLE_ADMIN")),
            Map.entry("DELETE_/catalog/services/:id", List.of("ROLE_ADMIN")),

            // =========================
            // SUPPLIES
            // =========================
            Map.entry("GET_/supplies",
                    List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("POST_/supplies", List.of("ROLE_ADMIN")),
            Map.entry("GET_/supplies/:id",
                    List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("PATCH_/supplies/:id", List.of("ROLE_ADMIN")),
            Map.entry("DELETE_/supplies/:id", List.of("ROLE_ADMIN")),

            // =========================
            // SUPPLIERS
            // =========================
            Map.entry("GET_/suppliers", List.of("ROLE_ADMIN", "ROLE_STOREKEEPER")),
            Map.entry("POST_/suppliers", List.of("ROLE_ADMIN")),
            Map.entry("GET_/suppliers/:id", List.of("ROLE_ADMIN", "ROLE_STOREKEEPER")),
            Map.entry("PATCH_/suppliers/:id", List.of("ROLE_ADMIN")),
            Map.entry("DELETE_/suppliers/:id", List.of("ROLE_ADMIN")),

            // =========================
            // PURCHASE ORDERS
            // =========================
            Map.entry("GET_/purchase-orders",
                    List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("POST_/purchase-orders", List.of("ROLE_ADMIN", "ROLE_STOREKEEPER")),
            Map.entry("GET_/purchase-orders/:id",
                    List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("PATCH_/purchase-orders/:id", List.of("ROLE_ADMIN", "ROLE_STOREKEEPER")),
            Map.entry("DELETE_/purchase-orders/:id", List.of("ROLE_ADMIN", "ROLE_STOREKEEPER")),

            // =========================
            // SERVICE ORDERS
            // =========================
            Map.entry("GET_/service-orders",
                    List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("POST_/service-orders", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT")),
            Map.entry("GET_/service-orders/:id",
                    List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT", "ROLE_CHATBOT", "ROLE_STOREKEEPER")),
            Map.entry("PATCH_/service-orders/:id", List.of("ROLE_ADMIN")),
            Map.entry("DELETE_/service-orders/:id", List.of("ROLE_ADMIN")),

            // =========================
            // SERVICE ORDER - SERVICES
            // =========================
            Map.entry("GET_/service-orders/:id/services", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT")),
            Map.entry("POST_/service-orders/:id/services", List.of("ROLE_ADMIN", "ROLE_MECHANIC")),
            Map.entry("GET_/service-orders/:id/services/:serviceId", List.of("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_ATTENDANT")),
            Map.entry("PATCH_/service-orders/:id/services/:serviceId", List.of("ROLE_ADMIN", "ROLE_MECHANIC")),
            Map.entry("DELETE_/service-orders/:id/services/:serviceId", List.of("ROLE_ADMIN", "ROLE_MECHANIC")),

            // =========================
            // SERVICE ORDER - PROGRESS / BUDGET
            // =========================
            Map.entry("POST_/service-orders/:id/progress", List.of("ROLE_ADMIN", "ROLE_MECHANIC")),
            Map.entry("POST_/service-orders/:id/budget", List.of("ROLE_ADMIN", "ROLE_ATTENDANT", "ROLE_CHATBOT"))

    );

    public static String buildKey(HttpServletRequest request) {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        return method + "_" + normalizePath(uri);
    }

    private static String normalizePath(String uri) {

        String[] segments = uri.split("/");

        StringBuilder normalized = new StringBuilder();
        String lastSegment = null;

        for (String segment : segments) {

            if (segment.isBlank())
                continue;

            normalized.append("/");

            if (STATIC_SEGMENTS.contains(segment)) {
                normalized.append(segment);
                lastSegment = segment;
            } else {
                // Este é um ID. Determinar qual ID:
                // Se o segmento anterior é "services", este é um :serviceId
                if ("services".equals(lastSegment)) {
                    normalized.append(":serviceId");
                } else {
                    // Default para IDs simples
                    normalized.append(":id");
                }
                lastSegment = null;  // Reset após processar ID
            }
        }

        return normalized.toString();
    }

    private static final List<String> STATIC_SEGMENTS = List.of(
            "catalog",
            "users",
            "owners",
            "vehicles",
            "services",
            "supplies",
            "suppliers",
            "purchase-orders",
            "service-orders",
            "progress",
            "budget");
}
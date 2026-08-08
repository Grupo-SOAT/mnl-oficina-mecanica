-- Limpar apenas os serviços, mantendo o catálogo base e o restante do baseline
DELETE FROM services;
ALTER SEQUENCE IF EXISTS services_service_id_seq RESTART WITH 1;
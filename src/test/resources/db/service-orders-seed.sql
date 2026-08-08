-- Seed mínimo para cenários de Service Orders (Cucumber)
-- Cria múltiplas ServiceOrders em diferentes estados para suportar testes isolados

DELETE FROM service_needed_supplies;
DELETE FROM services;
DELETE FROM service_orders;
DELETE FROM budget_approval_tokens;

-- ID 1: PENDING (para testar START_INSPECTION)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at)
VALUES (1, 1, 1, 'Troca de óleo e filtro', 'PENDING', 120.00, CURRENT_TIMESTAMP - INTERVAL '3 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 2: IN_INSPECTION (para testar COMPLETE_INSPECTION)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at, inspected_at)
VALUES (2, 2, 2, 'Revisão preventiva', 'IN_INSPECTION', 250.00, CURRENT_TIMESTAMP - INTERVAL '2 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 3: AWAITING_APPROVAL (para testar APPROVE e CANCEL via budget)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at)
VALUES (3, 1, 1, 'Troca de bateria', 'AWAITING_APPROVAL', 180.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 4: APPROVED (para testar START_SERVICE)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at, approved_at)
VALUES (4, 2, 2, 'Alinhamento', 'APPROVED', 200.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 5: IN_PROGRESS (para testar COMPLETE_SERVICE e CANCEL_SERVICE)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at, started_at)
VALUES (5, 1, 1, 'Troca de óleo completa', 'IN_PROGRESS', 150.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 6: COMPLETED (para testar DELIVER_VEHICLE)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at, completed_at)
VALUES (6, 2, 2, 'Revisão completa', 'COMPLETED', 300.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ID 7: CANCELLED (para testar DELIVER_VEHICLE de estado CANCELLED)
INSERT INTO service_orders (service_order_id, client_id, vehicle_id, description, status, estimated_amount, created_at, updated_at, cancelled_at)
VALUES (7, 1, 1, 'Revisão cancelada', 'CANCELLED', 100.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Services para testes
-- IMPORTANTE: Service IDs segregados por contexto de teste
--           OS 1: service 1 (usado por @services para testes de CRUD)
--           OS 4: service 10 (usado por @serviceOrders para START_SERVICE)
--           OS 5: services 11, 12 (usado por @serviceOrders para COMPLETE/CANCEL_SERVICE)

-- Para OS 1 (PENDING) - testes de CRUD de services com service IDs 1, 2, 3
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at)
VALUES 
  (1, 1, 1, 120.00, 'AWAITING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '3 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (4, 1, 2, 150.00, 'AWAITING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '2 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (5, 1, 3, 100.00, 'AWAITING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Para OS 4 (APPROVED) - testar START_SERVICE com service ID 10
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at, approved_at)
VALUES (10, 4, 1, 120.00, 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Para OS 5 (IN_PROGRESS) - testar COMPLETE_SERVICE e CANCEL_SERVICE com services IDs 11 e 12
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at, started_at)
VALUES 
  (11, 5, 2, 150.00, 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (12, 5, 3, 100.00, 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Para OS 6 (COMPLETED)
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at, completed_at)
VALUES (20, 6, 1, 300.00, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Para OS 7 (CANCELLED)
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at, cancelled_at)
VALUES (21, 7, 1, 100.00, 'CANCELLED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Para OS 2 (IN_INSPECTION) — usado pelo @asyncBudget COMPLETE_INSPECTION para validar o cálculo do estimatedAmount
-- Total esperado: 150.00 + 80.00 + (49.90*2) + 24.90 + 89.90 = 444.60
INSERT INTO services (service_id, service_order_id, catalog_service_id, price, status, created_at, updated_at)
VALUES
  (30, 2, 1, 150.00, 'AWAITING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '2 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
  (31, 2, 2, 80.00, 'AWAITING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '2 day', CURRENT_TIMESTAMP - INTERVAL '1 day');

INSERT INTO service_needed_supplies (service_id, id_supply, note, quantity)
VALUES
  (30, 1, 'Oleo troca completa', 2),
  (30, 2, 'Filtro original', 1),
  (31, 3, 'Jogo dianteiro', 1);

SELECT setval(pg_get_serial_sequence('service_orders', 'service_order_id'), (SELECT COALESCE(MAX(service_order_id), 0) FROM service_orders));
SELECT setval(pg_get_serial_sequence('services', 'service_id'), (SELECT COALESCE(MAX(service_id), 0) FROM services));

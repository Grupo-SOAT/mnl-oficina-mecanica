# language: pt
@serviceOrders
Funcionalidade: Gerenciamento de Ordens de Serviço
  Como um administrador da oficina
  Quero gerenciar ordens de serviço e seus fluxos
  Para controlar o atendimento do veículo e o orçamento do cliente

  # === LISTAGEM / PAGINAÇÃO / BUSCA ===

  Cenário: Listagem de ordens de serviço
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter uma lista de dados
    E a resposta deve conter o campo "pageSize"
    E a resposta deve conter o campo "cursor"
    E a resposta deve conter o campo "isLast"

  Cenário: Busca de ordens de serviço por status
    Dado que eu esteja devidamente logado
    E que o filtro status seja "PENDING"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve conter no maximo 1 ordens de serviço
    E a resposta deve conter ao menos uma ordem de serviço com status "PENDING"
    E a resposta deve conter ao menos uma ordem de serviço com statusLabel "Recebida"

  Cenário: Paginação sem resultados deve retornar no content
    Dado que eu esteja devidamente logado
    E que o filtro status seja "DELIVERED"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "204"

  Cenário: Paginação de ordens de serviço com cursor
    Dado que eu esteja devidamente logado
    E que o cursor seja "1"
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E o primeiro item retornado deve ter id maior que 1

  Cenário: Listagem padrão deve seguir a prioridade de status
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a ordem das ordens de serviço retornadas deve ser "5,3,2,1,7,4"

  Cenário: Listagem padrão deve excluir ordens de serviço concluídas
    Dado que eu esteja devidamente logado
    E que o tamanho da pagina seja 10
    Quando eu listar as ordens de serviço
    Então devo receber uma resposta com status "200"
    E a resposta não deve conter nenhuma ordem de serviço com status "COMPLETED"

  # === DETALHAMENTO POR ID ===

  Cenário: Detalhamento de ordem de serviço por ID existente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 1
    Quando eu consultar a ordem de serviço por id
    Então devo receber uma resposta com status "200"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "clientId"
    E a resposta deve conter o campo "vehicleId"
    E a resposta deve conter o campo "description"
    E a resposta deve conter o campo "status"
    E a resposta deve conter o campo "statusLabel"
    E a resposta deve conter o campo "estimatedAmount"

  Cenário: Detalhamento de ordem de serviço por ID inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    Quando eu consultar a ordem de serviço por id
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === CADASTRO ===

  Cenário: Cadastro de ordem de serviço com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | clientId | vehicleId | description            |
      | 1        | 1         | Troca de óleo e filtro |
    Quando eu criar a ordem de serviço
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "status"
    E o campo "statusLabel" deve ser "Recebida"
    E a resposta deve refletir o payload enviado

  Esquema do Cenário: Cadastro de ordem de serviço com ids inexistentes
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | clientId   | vehicleId   | description   |
      | <clientId> | <vehicleId> | <description> |
    Quando eu criar a ordem de serviço
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | clientId | vehicleId | description            | reason                          |
      | 99999    | 1         | Troca de óleo e filtro | SERVICE_ORDER_CLIENT_NOT_FOUND  |
      | 1        | 99999     | Troca de óleo e filtro | SERVICE_ORDER_VEHICLE_NOT_FOUND |

  # === CADASTRO EM CASCATA ===

  Cenário: Cadastro de ordem de serviço em cascata com dados válidos
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | vehicle                                                                                                                                                                                                                                    | description            | catalogServiceIds |
      | {"owner": {"name": "Creuza", "document": "09642137020", "documentType": "CPF", "phone": "(62) 90000-1111", "email": "creuza@email.com"}, "licensePlate": "MSM-9771", "brand": "Geep", "model": "Renegade", "year": 2021, "color": "Prata"} | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "201"
    E a resposta deve conter o campo "id"
    E a resposta deve conter o campo "status"
    E a resposta deve conter o campo "description"
    E a resposta deve conter o id do novo veículo
    E a resposta deve conter o id do novo cliente
    E o campo "statusLabel" deve ser "Recebida"

  Esquema do Cenário: Cadastro de ordem de serviço em cascata com ids inexistentes
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | ownerId   | vehicleId   | description   | catalogServiceIds   |
      | <ownerId> | <vehicleId> | <description> | <catalogServiceIds> |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | ownerId | vehicleId | description            | catalogServiceIds | reason                          |
      | 99999   | 1         | Troca de óleo e filtro | [1]               | SERVICE_ORDER_CLIENT_NOT_FOUND  |
      | 1       | 99999     | Troca de óleo e filtro | [1]               | SERVICE_ORDER_VEHICLE_NOT_FOUND |
      | 1       | 1         | Troca de óleo e filtro | [99999]           | CATALOG_SERVICE_NOT_FOUND       |

  Esquema do Cenário: Cadastro de ordem de serviço em cascata com dados duplicados
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | vehicle                                                                                                                                                                                                                                              | description            | catalogServiceIds |
      | {"owner": {"name": "Creuza", "document": "<ownerDocument>", "documentType": "CPF", "phone": "(62) 90000-1111", "email": "creuza@email.com"}, "licensePlate": "<licensePlate>", "brand": "Geep", "model": "Renegade", "year": 2021, "color": "Prata"} | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "409"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | ownerDocument | licensePlate | reason                                    |
      | 64261703050   | DEF-5678     | VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE |
      | 39544511075   | NEX-0666     | OWNER_ALREADY_EXISTS                      |

  Cenário: Cadastro de ordem de serviço em cascata sem dados de veículo
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | ownerId | description            | catalogServiceIds |
      | 1       | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_VEHICLE_DATA_ABSENT"

  Cenário: Cadastro de ordem de serviço em cascata sem dados de cliente
    Dado que eu esteja devidamente logado
    E que o corpo da nova ordem de serviço seja:
      | vehicle                                                                                            | description            | catalogServiceIds |
      | {"licensePlate": "KDP-9887", "brand": "Geep", "model": "Renegade", "year": 2021, "color": "Prata"} | Troca de óleo e filtro | [1]               |
    Quando eu criar a ordem de serviço em cascata
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "VEHICLE_OWNER_DATA_ABSENT"

  # === ATUALIZAÇÃO ===

  Cenário: Atualização de ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 1
    E que o corpo de atualização da ordem de serviço seja:
      | id | clientId | vehicleId | description                     | status   |
      | 1  | 1        | 1         | Troca de óleo, filtro e revisão | APPROVED |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "200"
    E a resposta deve refletir o payload enviado
    E o campo "statusLabel" deve ser "Aprovada"

  Esquema do Cenário: Atualização de ordem de serviço com payload inválido
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 1
    E que o corpo de atualização da ordem de serviço seja:
      | id   | clientId   | vehicleId   | description   | status   |
      | <id> | <clientId> | <vehicleId> | <description> | <status> |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "400"
    E a resposta deve conter o campo reason com valor "<reason>"
    Exemplos:
      | id | clientId | vehicleId | description            | status   | reason                          |
      | 1  | 99999    | 1         | Troca de óleo e filtro | APPROVED | SERVICE_ORDER_CLIENT_NOT_FOUND  |
      | 1  | 1        | 99999     | Troca de óleo e filtro | APPROVED | SERVICE_ORDER_VEHICLE_NOT_FOUND |

  Cenário: Atualização de ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o corpo de atualização da ordem de serviço seja:
      | id    | clientId | vehicleId | description    | status   |
      | 99999 | 1        | 1         | Nao encontrada | APPROVED |
    Quando eu atualizar a ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === EXCLUSÃO ===

  Cenário: Exclusão de ordem de serviço existente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 1
    Quando eu remover a ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Exclusão de ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    Quando eu remover a ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === PROGRESSO DA ORDEM DE SERVIÇO ===

  Cenário: Registrar ação START_INSPECTION
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 1
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo       | relatedServiceId |
      | START_INSPECTION | Recebida na recepcao |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Registrar ação COMPLETE_INSPECTION
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 2
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action              | additionalInfo      | relatedServiceId |
      | COMPLETE_INSPECTION | Inspecao finalizada |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Registrar ação START_SERVICE
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 4
    E que o id do serviço da ordem de serviço seja 10
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action        | additionalInfo          | relatedServiceId |
      | START_SERVICE | Inicio da troca de oleo | 10               |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Registrar ação COMPLETE_SERVICE
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 5
    E que o id do serviço da ordem de serviço seja 11
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo    | relatedServiceId |
      | COMPLETE_SERVICE | Serviço concluido | 11               |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Registrar ação CANCEL_SERVICE
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 5
    E que o id do serviço da ordem de serviço seja 12
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action         | additionalInfo    | relatedServiceId |
      | CANCEL_SERVICE | Serviço cancelado | 12               |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Registrar ação DELIVER_VEHICLE
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 6
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action          | additionalInfo   | relatedServiceId |
      | DELIVER_VEHICLE | Veiculo entregue |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"

  Cenário: Progresso em ordem de serviço inexistente
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 99999
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action           | additionalInfo       | relatedServiceId |
      | START_INSPECTION | Recebida na recepcao |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === ORÇAMENTO DA ORDEM DE SERVIÇO ===

  Esquema do Cenário: Registrar decisão do cliente sobre o orçamento
    Dado que o orçamento da ordem de serviço de ID "3" foi enviado ao cliente
    E que a decisão do cliente seja "<decision>"
    E que a observação da decisão seja "<comment>"
    E que os serviços rejeitados sejam "<rejectedServiceIds>"
    Quando eu registrar a decisão do cliente sobre o orçamento da ordem de serviço
    Então devo receber uma resposta com status "202"
    Exemplos:
      | decision | comment                       | rejectedServiceIds |
      | APPROVE  | Orçamento aprovado            |                    |
      | CANCEL   | Cliente desistiu              |                    |
      | REJECT   | Cliente recusou integralmente |                    |

  Cenário: Registrar rejeição parcial do orçamento (não implementado)
    Dado que o orçamento da ordem de serviço de ID "3" foi enviado ao cliente
    E que a decisão do cliente seja "PARTIALLY_REJECT"
    E que a observação da decisão seja "Cliente aceitou apenas parte"
    E que os serviços rejeitados sejam "1"
    Quando eu registrar a decisão do cliente sobre o orçamento da ordem de serviço
    Então devo receber uma resposta com status "501"
    E a resposta deve conter o campo reason com valor "PARTIAL_BUDGET_REJECTION_NOT_IMPLEMENTED"

  Cenário: Decisão de orçamento para ordem de serviço inexistente
    Dado que a decisão do cliente seja "APPROVE"
    E que a observação da decisão seja "Aprovado"
    E que os serviços rejeitados sejam ""
    E que o orçamento da ordem de serviço de ID "99999" foi enviado ao cliente
    Quando eu registrar a decisão do cliente sobre o orçamento da ordem de serviço
    Então devo receber uma resposta com status "404"
    E a resposta deve conter o campo reason com valor "SERVICE_ORDER_NOT_FOUND"

  # === ORÇAMENTO ASSÍNCRONO ===

  @asyncBudget
  Cenário: Publicar evento de orçamento ao completar inspeção
    Dado que eu esteja devidamente logado
    E que o id da ordem de serviço seja 2
    E que o corpo da ação de progresso da ordem de serviço seja:
      | action              | additionalInfo      | relatedServiceId |
      | COMPLETE_INSPECTION | Inspecao finalizada |                  |
    Quando eu registrar o progresso da ordem de serviço
    Então devo receber uma resposta com status "202"
    E um evento deve ser publicado no tópico "budget-approval-request" com o "serviceOrderId" igual a "2"
    E o evento publicado no tópico "budget-approval-request" deve conter o campo "token" com um valor não vazio
    E o campo decimal "estimatedAmount" da ordem de serviço de ID "2" deve ser "444.60"

  @asyncBudget
  Esquema do Cenário: Processar decisão assíncrona de orçamento via Kafka
    Dado que eu esteja devidamente logado
    E que exista uma ordem de serviço de ID "3" com status "AWAITING_APPROVAL"
    E que um evento de decisão seja publicado no tópico "budget-decision"
      | serviceOrderId | decision   |
      | 3              | <decision> |
    Quando o sistema consumir o evento do tópico "budget-decision"
    Então o status da ordem de serviço de ID "3" deve ser "<expectedStatus>"
    E o statusLabel da ordem de serviço de ID "3" deve ser "<expectedLabel>"
    Exemplos:
      | decision | expectedStatus | expectedLabel |
      | APPROVE  | APPROVED       | Aprovada      |
      | REJECT   | CANCELLED      | Cancelada     |

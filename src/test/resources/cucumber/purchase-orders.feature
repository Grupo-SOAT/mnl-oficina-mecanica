## language: pt
#@purchaseOrders
#Funcionalidade: Gerenciamento de Pedidos de Compra
#  Como um administrador da oficina
#  Quero gerenciar pedidos de compra
#  Para garantir reposicao de insumos
#
#  # === LISTAGEM / PAGINAÇÃO / BUSCA ===
#
#  Cenário: Listagem de pedidos de compra
#    Dado que eu esteja devidamente logado
#    E que o tamanho da pagina seja 10
#    Quando eu listar os pedidos de compra
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter uma lista de dados
#    E a resposta deve conter o campo "pageSize"
#    E a resposta deve conter o campo "cursor"
#    E a resposta deve conter o campo "isLast"
#
#  Esquema do Cenário: Busca de pedidos de compra por filtros
#    Dado que eu esteja devidamente logado
#    E com o parametro de busca "<filtro>" igual a "<valor>"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os pedidos de compra
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter no maximo <maximo> pedidos de compra
#    Exemplos:
#      | filtro           | valor              | maximo |
#      | supplierId       | 1                  | 10     |
#      | supplierDocument | 12.345.678/0001-95 | 1      |
#      | sku              | SKU-001            | 1      |
#      | status           | COMPLETED          | 1      |
#
#  Cenário: Paginação sem resultados deve retornar no content
#    Dado que eu esteja devidamente logado
#    E que o filtro supplierId seja "99999"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os pedidos de compra
#    Então devo receber uma resposta com status "204"
#
#  Cenário: Paginação de pedidos de compra com cursor
#    Dado que eu esteja devidamente logado
#    E que o cursor seja "1"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os pedidos de compra
#    Então devo receber uma resposta com status "200"
#    E o primeiro item retornado deve ter id maior que 1
#
#  # === DETALHAMENTO POR ID ===
#
#  Cenário: Detalhamento de pedido de compra por ID existente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    Quando eu consultar o pedido de compra por id
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter o campo "id"
#    E a resposta deve conter o campo "supplierId"
#    E a resposta deve conter o campo "requestedSupplies"
#    E a resposta deve conter o campo "status"
#
#  Cenário: Detalhamento de pedido de compra por ID inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 99999
#    Quando eu consultar o pedido de compra por id
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_NOT_FOUND"
#
#  # === CADASTRO ===
#
#  Cenário: Cadastro de pedido de compra com dados válidos
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo pedido de compra seja:
#      | supplierId | requestedSupplies                                    |
#      | 1          | supplyId=2,quotedUnitPrice=34.90,requestedQuantity=5 |
#    Quando eu criar o pedido de compra
#    Então devo receber uma resposta com status "201"
#    E a resposta deve conter o campo "id"
#    E a resposta deve refletir o payload enviado
#
#  Esquema do Cenário: Cadastro de pedido de compra com fornecedor inválido
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo pedido de compra seja:
#      | supplierId   | requestedSupplies                                    |
#      | <supplierId> | supplyId=2,quotedUnitPrice=34.90,requestedQuantity=5 |
#    Quando eu criar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_SUPPLIER_NOT_FOUND"
#    Exemplos:
#      | supplierId |
#      |            |
#      | 99999      |
#
#  Esquema do Cenário: Cadastro de pedido de compra com insumo inválido
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo pedido de compra seja:
#      | supplierId | requestedSupplies                                   |
#      | 1          | <requestedSupplies>                                 |
#    Quando eu criar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_SUPPLY_NOT_FOUND"
#    Exemplos:
#      | requestedSupplies                                        |
#      | supplyId=,quotedUnitPrice=34.90,requestedQuantity=5      |
#      | supplyId=99999,quotedUnitPrice=34.90,requestedQuantity=5 |
#
#  Esquema do Cenário: Cadastro de pedido de compra com quantidade inválida
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo pedido de compra seja:
#      | supplierId | requestedSupplies   |
#      | 1          | <requestedSupplies> |
#    Quando eu criar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "INVALID_REQUESTED_QUANTITY"
#    Exemplos:
#      | requestedSupplies                                       |
#      | supplyId=1,quotedUnitPrice=34.90,requestedQuantity=     |
#      | supplyId=1,quotedUnitPrice=34.90,requestedQuantity=-999 |
#
#  Esquema do Cenário: Cadastro de pedido de compra com cotação inválida
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo pedido de compra seja:
#      | supplierId | requestedSupplies   |
#      | 1          | <requestedSupplies> |
#    Quando eu criar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "INVALID_REQUESTED_QUANTITY"
#    Exemplos:
#      | requestedSupplies                                    |
#      | supplyId=1,quotedUnitPrice=,requestedQuantity=5      |
#      | supplyId=1,quotedUnitPrice=-1.50,requestedQuantity=5 |
#
#  # === ATUALIZAÇÃO ===
#
#  Cenário: Atualização de pedido de compra existente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    E que o corpo de atualização do pedido de compra seja:
#      | id | supplierId | requestedSupplies                                    | status     |
#      | 1  | 1          | supplyId=1,quotedUnitPrice=50.00,requestedQuantity=2 | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "200"
#    E a resposta deve refletir o payload enviado
#
#  Cenário: Atualização de pedido de compra inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 99999
#    E que o corpo de atualização do pedido de compra seja:
#      | id    | supplierId | requestedSupplies                                    | status     |
#      | 99999 | 1          | supplyId=1,quotedUnitPrice=50.00,requestedQuantity=2 | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_NOT_FOUND"
#
#  Esquema do Cenário: Atualização de pedido de compra com fornecedor inválido
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    E que o corpo de atualização do pedido de compra seja:
#      | id | supplierId   | requestedSupplies                                    | status     |
#      | 1  | <supplierId> | supplyId=2,quotedUnitPrice=34.90,requestedQuantity=5 | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_SUPPLIER_NOT_FOUND"
#    Exemplos:
#      | supplierId |
#      |            |
#      | 99999      |
#
#  Esquema do Cenário: Atualização de pedido de compra com insumo inválido
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    E que o corpo de atualização do pedido de compra seja:
#      | id | supplierId | requestedSupplies   | status     |
#      | 1  | 1          | <requestedSupplies> | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_SUPPLY_NOT_FOUND"
#    Exemplos:
#      | requestedSupplies                                        |
#      | supplyId=,quotedUnitPrice=34.90,requestedQuantity=5      |
#      | supplyId=99999,quotedUnitPrice=34.90,requestedQuantity=5 |
#
#  Esquema do Cenário: Atualização de pedido de compra com quantidade inválida
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    E que o corpo de atualização do pedido de compra seja:
#      | id | supplierId | requestedSupplies   | status     |
#      | 1  | 1          | <requestedSupplies> | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "INVALID_REQUESTED_QUANTITY"
#    Exemplos:
#      | requestedSupplies                                       |
#      | supplyId=1,quotedUnitPrice=34.90,requestedQuantity=     |
#      | supplyId=1,quotedUnitPrice=34.90,requestedQuantity=-999 |
#
#  Esquema do Cenário: Atualização de pedido de compra com cotação inválida
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    E que o corpo de atualização do pedido de compra seja:
#      | id | supplierId | requestedSupplies   | status     |
#      | 1  | 1          | <requestedSupplies> | DISPATCHED |
#    Quando eu atualizar o pedido de compra
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "INVALID_REQUESTED_QUANTITY"
#    Exemplos:
#      | requestedSupplies                                    |
#      | supplyId=1,quotedUnitPrice=,requestedQuantity=5      |
#      | supplyId=1,quotedUnitPrice=-1.50,requestedQuantity=5 |
#
#
#  # === EXCLUSÃO ===
#
#  Cenário: Exclusão de pedido de compra existente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 1
#    Quando eu remover o pedido de compra
#    Então devo receber uma resposta com status "202"
#
#  Cenário: Exclusão de pedido de compra inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do pedido de compra seja 99999
#    Quando eu remover o pedido de compra
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "PURCHASE_ORDER_NOT_FOUND"

## language: pt
#@suppliers
#Funcionalidade: Gerenciamento de Fornecedores
#  Como um administrador da oficina
#  Quero gerenciar fornecedores
#  Para manter os dados de abastecimento atualizados
#
#  # === LISTAGEM / PAGINAÇÃO / BUSCA ===
#
#  Cenário: Listagem de fornecedores
#    Dado que eu esteja devidamente logado
#    E que o tamanho da pagina seja 10
#    Quando eu listar os fornecedores
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter uma lista de dados
#    E a resposta deve conter o campo "pageSize"
#    E a resposta deve conter o campo "cursor"
#    E a resposta deve conter o campo "isLast"
#
#  Cenário: Busca de fornecedores por documento
#    Dado que eu esteja devidamente logado
#    E que o filtro document seja "12.345.678/0001-95"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os fornecedores
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter no maximo 1 fornecedores
#    E a resposta deve conter ao menos um fornecedor com document "12.345.678/0001-95"
#
#  Cenário: Paginação sem resultados deve retornar no content
#    Dado que eu esteja devidamente logado
#    E que o filtro document seja "00.000.000/0000-00"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os fornecedores
#    Então devo receber uma resposta com status "204"
#
#  Cenário: Paginação de fornecedores com cursor
#    Dado que eu esteja devidamente logado
#    E que o cursor seja "1"
#    E que o tamanho da pagina seja 10
#    Quando eu listar os fornecedores
#    Então devo receber uma resposta com status "200"
#    E o primeiro item retornado deve ter id maior que 1
#
#  # === DETALHAMENTO POR ID ===
#
#  Cenário: Detalhamento de fornecedor por ID existente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 1
#    Quando eu consultar o fornecedor por id
#    Então devo receber uma resposta com status "200"
#    E a resposta deve conter o campo "id"
#    E a resposta deve conter o campo "name"
#    E a resposta deve conter o campo "document"
#    E a resposta deve conter o campo "contactPerson"
#    E a resposta deve conter o campo "email"
#    E a resposta deve conter o campo "phone"
#
#  Cenário: Detalhamento de fornecedor por ID inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 99999
#    Quando eu consultar o fornecedor por id
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "SUPPLIER_NOT_FOUND"
#
#  # === CADASTRO ===
#
#  Esquema do Cenario: Cadastro de fornecedor com dados válidos
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo fornecedor seja:
#      | name   | document   | contactPerson   | email   | phone   |
#      | <name> | <document> | <contactPerson> | <email> | <phone> |
#    Quando eu criar o fornecedor
#    Então devo receber uma resposta com status "201"
#    E a resposta deve conter o campo "id"
#    E a resposta deve refletir o payload enviado
#    Exemplos:
#      | name                     | document           | contactPerson | email                       | phone          |
#      | Auto Pecas Paulista LTDA | 45.987.654/0001-10 | Carla Mendes  | compras@autopaulista.com.br | (11) 3344-7788 |
#      | Paraíba Lanternagem LTDA | 16303629000140     | José Cabral   | compras@autopaulista.com.br | (83) 5555-0000 |
#
#  Esquema do Cenário: Cadastro de fornecedor com campos inválidos
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo fornecedor seja:
#      | name   | document   | contactPerson   | email   | phone   |
#      | <name> | <document> | <contactPerson> | <email> | <phone> |
#    Quando eu criar o fornecedor
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "<reason>"
#    Exemplos:
#      | name                   | document           | contactPerson | email               | phone          | reason                    |
#      | Invalid LTDA           | 459876540001       | Inválido      | email@example.com   | (11) 1111-1111 | INVALID_SUPPLIER_DOCUMENT |
#      | Invalid LTDA           | 31473724000171     | Inválido      | email@example.com   | (11) 1111-1111 | INVALID_SUPPLIER_DOCUMENT |
#      | Telefone Invalido LTDA | 45.987.654/0001-10 | Carla Mendes  | compras@forn.com.br |                | INVALID_SUPPLIER_PHONE    |
#      | Telefone Invalido LTDA | 45.987.654/0001-10 | Carla Mendes  | compras@forn.com.br | 123            | INVALID_SUPPLIER_PHONE    |
#      | Email Invalido LTDA    | 45.987.654/0001-10 | Carla Mendes  |                     | (11) 1111-1111 | INVALID_SUPPLIER_PHONE    |
#      | Email Invalido LTDA    | 45.987.654/0001-10 | Carla Mendes  | sem-arroba.com      | (11) 1111-1111 | INVALID_SUPPLIER_PHONE    |
#
#  Cenário: Cadastro de fornecedor com CNPJ duplicado
#    Dado que eu esteja devidamente logado
#    E que o corpo do novo fornecedor seja:
#      | name                | document           | contactPerson | email                | phone          |
#      | Fornecedor Conflito | 12.345.678/0001-95 | Joao Silva    | conflito@forn.com.br | (11) 3333-4444 |
#    Quando eu criar o fornecedor
#    Então devo receber uma resposta com status "409"
#    E a resposta deve conter o campo reason com valor "SUPPLIER_CONFLICT_DUPLICATED_DOCUMENT"
#
#  # === ATUALIZAÇÃO ===
#
#  Cenário: Atualização de fornecedor existente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 1
#    E que o corpo de atualização do fornecedor seja:
#      | id | name             | document           | contactPerson | email               | phone          |
#      | 1  | Fornecedor Atual | 12.345.678/0001-95 | Ana Pereira   | novo@fornecedor.com | (11) 9999-9999 |
#    Quando eu atualizar o fornecedor
#    Então devo receber uma resposta com status "200"
#    E a resposta deve refletir o payload enviado
#
#  Esquema do Cenário: Atualização de fornecedor com campos inválidos
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 1
#    E que o corpo de atualização do fornecedor seja:
#      | id | name   | document   | contactPerson   | email   | phone   |
#      | 1  | <name> | <document> | <contactPerson> | <email> | <phone> |
#    Quando eu atualizar o fornecedor
#    Então devo receber uma resposta com status "400"
#    E a resposta deve conter o campo reason com valor "<reason>"
#    Exemplos:
#      | name                   | document           | contactPerson | email               | phone          | reason                    |
#      | Invalid LTDA           | 459876540001       | Inválido      | email@example.com   | (11) 1111-1111 | INVALID_SUPPLIER_DOCUMENT |
#      | Telefone Invalido LTDA | 45.987.654/0001-10 | Carla Mendes  | compras@forn.com.br |                | INVALID_SUPPLIER_PHONE    |
#      | Email Invalido LTDA    | 45.987.654/0001-10 | Carla Mendes  | sem-arroba.com      | (11) 1111-1111 | INVALID_SUPPLIER_PHONE    |
#
#  Cenário: Atualização de fornecedor com CNPJ duplicado
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 1
#    E que o corpo de atualização do fornecedor seja:
#      | id | name                | document           | contactPerson | email                | phone          |
#      | 1  | Fornecedor Conflito | 12.345.678/0001-95 | Joao Silva    | conflito@forn.com.br | (11) 3333-4444 |
#    Quando eu atualizar o fornecedor
#    Então devo receber uma resposta com status "409"
#    E a resposta deve conter o campo reason com valor "SUPPLIER_CONFLICT_DUPLICATED_DOCUMENT"
#
#  Cenário: Atualização de fornecedor inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 99999
#    E que o corpo de atualização do fornecedor seja:
#      | id    | name       | document           | contactPerson | email             | phone           |
#      | 99999 | Nao Existe | 66.777.888/0001-99 | Sem Registro  | ne@fornecedor.com | (11) 92222-1111 |
#    Quando eu atualizar o fornecedor
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "SUPPLIER_NOT_FOUND"
#
#  # === EXCLUSÃO ===
#
#  Cenário: Exclusão de fornecedor existente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 2
#    Quando eu remover o fornecedor
#    Então devo receber uma resposta com status "202"
#
#  Cenário: Exclusão de fornecedor inexistente
#    Dado que eu esteja devidamente logado
#    E que o id do fornecedor seja 99999
#    Quando eu remover o fornecedor
#    Então devo receber uma resposta com status "404"
#    E a resposta deve conter o campo reason com valor "SUPPLIER_NOT_FOUND"

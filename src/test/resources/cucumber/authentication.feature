# language: pt
@authentication
Funcionalidade: Login no sistema
  Como um usuário cadastrado
  Quero fazer login
  Para acessar a área restrita

# === Autenticação dos usuários humanos via usuário e senha ===

  Esquema do Cenário: Login bem-sucedido com diferentes perfis
    Dado que eu esteja deslogado
    Quando informo o usuário "<usuario>" e a senha "<senha>"
    Então devo receber uma resposta com status "200"
    E com um token de sessão válido
    E devo ser capaz de obter meus dados de usuário com "roles" igual a "<role>"
    Exemplos:
      | usuario    | senha      | role        |
      | admin      | admin      | admin       |
      | mecanico   | mecanico   | mechanic    |
      | atendente  | atendente  | attendant   |
      | almoxarife | almoxarife | storekeeper |

  Esquema do Cenário: Credenciais inválidas
    Dado que eu esteja deslogado
    Quando informo o usuário "<usuario>" e a senha "<senha>"
    Então devo receber um erro com status "400"
    E com a mensagem de erro igual a "<erro>"
    Exemplos:
      | usuario     | senha    | erro               |
      | inexistente | 123      | USERNAME_NOT_FOUND |
      | admin       | inválida | INVALID_PASSWORD   |

# === Autenticação do Chatbot via api-key ===

  Cenário: Autenticação bem-sucedida por chave de API
    Dado que eu esteja deslogado
    Quando informo a chave de API "chatbot-123"
    Então devo receber uma resposta com status "200"
    E com um token de sessão válido
    E devo ser capaz de obter o tempo de validade da minha chave de API

  Cenário: Chave de API inválida
    Dado que eu esteja deslogado
    Quando informo a chave de API "inválida"
    Então devo receber um erro com status "400"
    E com a mensagem de erro igual a "INVALID_CHATBOT_API_KEY"

# === Controle de Acesso por Role (Autorização) ===

  Esquema do Cenário: Admin - Acesso a recursos
    Dado que eu esteja logado como "admin"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status diferente de "403"
    Exemplos:
      | method | endpoint                                |
      | GET    | /users                                  |
      | POST   | /users                                  |
      | GET    | /users/:id                              |
      | PATCH  | /users/:id                              |
      | DELETE | /users/:id                              |
      | GET    | /owners                                 |
      | POST   | /owners                                 |
      | GET    | /owners/:id                             |
      | PATCH  | /owners/:id                             |
      | DELETE | /owners/:id                             |
      | GET    | /vehicles                               |
      | POST   | /vehicles                               |
      | GET    | /vehicles/:id                           |
      | PATCH  | /vehicles/:id                           |
      | DELETE | /vehicles/:id                           |
      | GET    | /catalog/services                       |
      | POST   | /catalog/services                       |
      | GET    | /catalog/services/:id                   |
      | PATCH  | /catalog/services/:id                   |
      | DELETE | /catalog/services/:id                   |
      | GET    | /supplies                               |
      | POST   | /supplies                               |
      | GET    | /supplies/:id                           |
      | PATCH  | /supplies/:id                           |
      | DELETE | /supplies/:id                           |
#      | GET    | /suppliers                              |
#      | POST   | /suppliers                              |
#      | GET    | /suppliers/:id                          |
#      | PATCH  | /suppliers/:id                          |
#      | DELETE | /suppliers/:id                          |
#      | GET    | /purchase-orders                        |
#      | POST   | /purchase-orders                        |
#      | GET    | /purchase-orders/:id                    |
#      | PATCH  | /purchase-orders/:id                    |
#      | DELETE | /purchase-orders/:id                    |
      | GET    | /service-orders                         |
      | POST   | /service-orders                         |
      | GET    | /service-orders/:id                     |
      | PATCH  | /service-orders/:id                     |
      | DELETE | /service-orders/:id                     |
      | GET    | /service-orders/:id/services            |
      | POST   | /service-orders/:id/services            |
      | GET    | /service-orders/:id/services/:serviceId |
      | PATCH  | /service-orders/:id/services/:serviceId |
      | DELETE | /service-orders/:id/services/:serviceId |
      | POST   | /service-orders/:id/progress            |
      | POST   | /service-orders/:id/budget              |

  Esquema do Cenário: Mecânico - Acesso permitido
    Dado que eu esteja logado como "mecanico"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status diferente de "403"
    Exemplos:
      | method | endpoint                                |
      | GET    | /users                                  |
      | GET    | /users/:id                              |
      | GET    | /owners                                 |
      | GET    | /owners/:id                             |
      | GET    | /vehicles                               |
      | GET    | /vehicles/:id                           |
      | GET    | /catalog/services                       |
      | GET    | /catalog/services/:id                   |
      | PATCH  | /catalog/services/:id                   |
      | DELETE | /catalog/services/:id                   |
      | GET    | /supplies                               |
      | GET    | /supplies/:id                           |
      | GET    | /service-orders                         |
      | GET    | /service-orders/:id                     |
      | GET    | /service-orders/:id/services            |
      | GET    | /service-orders/:id/services/:serviceId |
      | PATCH  | /service-orders/:id/services/:serviceId |
      | DELETE | /service-orders/:id/services/:serviceId |
      | POST   | /service-orders/:id/services            |
      | POST   | /service-orders/:id/progress            |

  Esquema do Cenário: Mecânico - Acesso bloqueado
    Dado que eu esteja logado como "mecanico"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status "403"
    Exemplos:
      | method | endpoint                   |
      | POST   | /users                     |
      | PATCH  | /users/:id                 |
      | DELETE | /users/:id                 |
      | POST   | /owners                    |
      | PATCH  | /owners/:id                |
      | DELETE | /owners/:id                |
      | POST   | /vehicles                  |
      | PATCH  | /vehicles/:id              |
      | DELETE | /vehicles/:id              |
      | POST   | /catalog/services          |
      | POST   | /supplies                  |
      | PATCH  | /supplies/:id              |
      | DELETE | /supplies/:id              |
#      | POST   | /suppliers                 |
#      | GET    | /suppliers                 |
#      | PATCH  | /suppliers/:id             |
#      | DELETE | /suppliers/:id             |
#      | GET    | /purchase-orders           |
#      | POST   | /purchase-orders           |
#      | PATCH  | /purchase-orders/:id       |
#      | DELETE | /purchase-orders/:id       |
      | POST   | /service-orders            |
      | PATCH  | /service-orders/:id        |
      | DELETE | /service-orders/:id        |
      | POST   | /service-orders/:id/budget |

  Esquema do Cenário: Atendente - Acesso permitido
    Dado que eu esteja logado como "atendente"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status diferente de "403"
    Exemplos:
      | method | endpoint                                |
      | GET    | /users                                  |
      | GET    | /users/:id                              |
      | GET    | /owners                                 |
      | POST   | /owners                                 |
      | GET    | /owners/:id                             |
      | PATCH  | /owners/:id                             |
      | DELETE | /owners/:id                             |
      | GET    | /vehicles                               |
      | POST   | /vehicles                               |
      | GET    | /vehicles/:id                           |
      | PATCH  | /vehicles/:id                           |
      | DELETE | /vehicles/:id                           |
      | GET    | /catalog/services                       |
      | GET    | /catalog/services/:id                   |
      | GET    | /supplies                               |
      | GET    | /supplies/:id                           |
      | GET    | /service-orders                         |
      | POST   | /service-orders                         |
      | GET    | /service-orders/:id                     |
      | GET    | /service-orders/:id/services            |
      | GET    | /service-orders/:id/services/:serviceId |
      | POST   | /service-orders/:id/budget              |
#      | GET    | /purchase-orders                        |
#      | GET    | /purchase-orders/:id                    |

  Esquema do Cenário: Atendente - Acesso bloqueado
    Dado que eu esteja logado como "atendente"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status "403"
    Exemplos:
      | method | endpoint                                |
      | POST   | /users                                  |
      | PATCH  | /users/:id                              |
      | DELETE | /users/:id                              |
      | POST   | /catalog/services                       |
      | POST   | /supplies                               |
      | PATCH  | /supplies/:id                           |
      | DELETE | /supplies/:id                           |
#      | POST   | /suppliers                              |
#      | GET    | /suppliers                              |
#      | PATCH  | /suppliers/:id                          |
#      | DELETE | /suppliers/:id                          |
      | PATCH  | /service-orders/:id                     |
      | DELETE | /service-orders/:id                     |
      | POST   | /service-orders/:id/services            |
      | POST   | /service-orders/:id/progress            |
      | PATCH  | /service-orders/:id/services/:serviceId |
      | DELETE | /service-orders/:id/services/:serviceId |
#      | POST   | /purchase-orders                        |
#      | PATCH  | /purchase-orders/:id                    |
#      | DELETE | /purchase-orders/:id                    |

  Esquema do Cenário: Chatbot - Acesso permitido
    Dado que eu esteja autenticado como "chatbot"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status diferente de "403"
    Exemplos:
      | method | endpoint                   |
      | GET    | /owners                    |
      | POST   | /owners                    |
      | GET    | /owners/:id                |
      | PATCH  | /owners/:id                |
      | DELETE | /owners/:id                |
      | GET    | /vehicles                  |
      | POST   | /vehicles                  |
      | GET    | /vehicles/:id              |
      | PATCH  | /vehicles/:id              |
      | DELETE | /vehicles/:id              |
      | GET    | /catalog/services          |
      | GET    | /catalog/services/:id      |
      | PATCH  | /catalog/services/:id      |
      | DELETE | /catalog/services/:id      |
      | GET    | /supplies                  |
      | GET    | /supplies/:id              |
      | GET    | /service-orders            |
      | POST   | /service-orders            |
      | GET    | /service-orders/:id        |
      | POST   | /service-orders/:id/budget |
#      | GET    | /purchase-orders                        |
#      | GET    | /purchase-orders/:id                    |

  Esquema do Cenário: Chatbot - Acesso bloqueado
    Dado que eu esteja autenticado como "chatbot"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status "403"
    Exemplos:
      | method | endpoint                                |
      | POST   | /users                                  |
      | GET    | /users                                  |
      | GET    | /users/:id                              |
      | PATCH  | /users/:id                              |
      | DELETE | /users/:id                              |
      | POST   | /catalog/services                       |
      | POST   | /supplies                               |
      | PATCH  | /supplies/:id                           |
      | DELETE | /supplies/:id                           |
#      | POST   | /suppliers                              |
#      | GET    | /suppliers                              |
#      | PATCH  | /suppliers/:id                          |
#      | DELETE | /suppliers/:id                          |
      | PATCH  | /service-orders/:id                     |
      | DELETE | /service-orders/:id                     |
      | GET    | /service-orders/:id/services            |
      | POST   | /service-orders/:id/services            |
      | POST   | /service-orders/:id/progress            |
      | GET    | /service-orders/:id/services/:serviceId |
      | PATCH  | /service-orders/:id/services/:serviceId |
      | DELETE | /service-orders/:id/services/:serviceId |
#      | POST   | /purchase-orders                        |
#      | PATCH  | /purchase-orders/:id                    |
#      | DELETE | /purchase-orders/:id                    |

  Esquema do Cenário: Almoxarife - Acesso permitido
    Dado que eu esteja logado como "almoxarife"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status diferente de "403"
    Exemplos:
      | method | endpoint              |
      | GET    | /users                |
      | GET    | /users/:id            |
      | GET    | /supplies             |
      | GET    | /supplies/:id         |
      | PATCH  | /catalog/services/:id |
      | DELETE | /catalog/services/:id |
#      | GET    | /suppliers           |
#      | GET    | /suppliers/:id       |
#      | GET    | /purchase-orders     |
#      | POST   | /purchase-orders     |
#      | GET    | /purchase-orders/:id |
#      | PATCH  | /purchase-orders/:id |
#      | DELETE | /purchase-orders/:id |
      | GET    | /service-orders       |
      | GET    | /service-orders/:id   |

  Esquema do Cenário: Almoxarife - Acesso bloqueado
    Dado que eu esteja logado como "almoxarife"
    Quando acesso o endpoint "<method>" "<endpoint>"
    Então devo receber uma resposta com status "403"
    Exemplos:
      | method | endpoint                                |
      | POST   | /users                                  |
      | PATCH  | /users/:id                              |
      | DELETE | /users/:id                              |
      | POST   | /owners                                 |
      | GET    | /owners                                 |
      | PATCH  | /owners/:id                             |
      | DELETE | /owners/:id                             |
      | POST   | /vehicles                               |
      | GET    | /vehicles                               |
      | PATCH  | /vehicles/:id                           |
      | DELETE | /vehicles/:id                           |
      | POST   | /catalog/services                       |
      | GET    | /catalog/services                       |
      | POST   | /supplies                               |
      | PATCH  | /supplies/:id                           |
      | DELETE | /supplies/:id                           |
#      | POST   | /suppliers                              |
#      | PATCH  | /suppliers/:id                          |
#      | DELETE | /suppliers/:id                          |
      | POST   | /service-orders                         |
      | PATCH  | /service-orders/:id                     |
      | DELETE | /service-orders/:id                     |
      | GET    | /service-orders/:id/services            |
      | POST   | /service-orders/:id/services            |
      | GET    | /service-orders/:id/services/:serviceId |
      | PATCH  | /service-orders/:id/services/:serviceId |
      | DELETE | /service-orders/:id/services/:serviceId |
      | POST   | /service-orders/:id/progress            |
      | POST   | /service-orders/:id/budget              |

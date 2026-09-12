# 🔧 Sistema Integrado de Gestão de Oficina Mecânica

Sistema de gestão de oficina mecânica desenvolvido em **Java 21** com **Spring Boot**, seguindo os princípios de **Domain-Driven Design (DDD)** e **Arquitetura Hexagonal**.

O sistema centraliza as principais operações de uma oficina mecânica, incluindo clientes, veículos, serviços, ordens de serviço, usuários, autenticação, catálogo de serviços e peças, suprimentos e geração de relatórios.

Este repositório contém o **monólito principal** da solução do Grupo-SOAT.

---

## 📋 Sumário

* [Sobre o projeto](#-sobre-o-projeto)
* [Arquitetura](#-arquitetura)
* [Domínios](#-domínios)
* [Funcionalidades](#-funcionalidades)
* [Tecnologias](#-tecnologias)
* [Estrutura do projeto](#-estrutura-do-projeto)
* [API](#-api)
* [Autenticação e autorização](#-autenticação-e-autorização)
* [Banco de dados](#-banco-de-dados)
* [Observabilidade](#-observabilidade)
* [Testes](#-testes)
* [Qualidade de código](#-qualidade-de-código)
* [Docker](#-docker)
* [Execução local](#-execução-local)
* [CI/CD](#-cicd)
* [Deploy na AWS](#-deploy-na-aws)
* [Relação com os demais componentes](#-relação-com-os-demais-componentes)
* [Licença](#-licença)

---

# 📖 Sobre o projeto

O **Sistema Integrado de Gestão de Oficina Mecânica** foi desenvolvido para fornecer uma plataforma para gerenciamento das operações de uma oficina.

O monólito concentra as regras de negócio principais da aplicação e é responsável por funcionalidades como:

* gerenciamento de clientes;
* gerenciamento de veículos;
* catálogo de serviços;
* controle de peças e suprimentos;
* abertura e acompanhamento de ordens de serviço;
* usuários e permissões;
* autenticação;
* geração de relatórios;
* integração com serviços externos;
* comunicação com Kafka;
* persistência em PostgreSQL.

A aplicação utiliza uma arquitetura modular internamente, separando os principais contextos de negócio em diferentes módulos de domínio.

---

# 🏗️ Arquitetura

O projeto segue **Arquitetura Hexagonal (Ports and Adapters)**.

A organização principal é:

```text
                    ┌───────────────────────┐
                    │      API / Web        │
                    │   Input Adapters      │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │         Ports         │
                    │     Input / Output    │
                    └───────────┬───────────┘
                                │
                                ▼
              ┌──────────────────────────────────┐
              │            DOMAIN                 │
              │                                  │
              │  Authentication                  │
              │  Owner                           │
              │  Vehicle                         │
              │  Service                         │
              │  Service Order                   │
              │  Supply                          │
              │  Catalog Services                │
              │  Reporting                       │
              │  User                            │
              └───────────────┬──────────────────┘
                              │
                              ▼
                    ┌───────────────────────┐
                    │   Output Adapters     │
                    │                       │
                    │ JPA / PostgreSQL      │
                    │ Kafka                 │
                    │ External services     │
                    └───────────────────────┘
```

A estrutura do código separa explicitamente `adapter`, `domain`, `port`, `config` e `exception`, característica da abordagem hexagonal utilizada no projeto.

---

# 🧠 Domínios

O domínio da aplicação está dividido em diferentes contextos:

| Domínio           | Responsabilidade         |
| ----------------- | ------------------------ |
| `authentication`  | Autenticação e segurança |
| `owner`           | Clientes/proprietários   |
| `vehicle`         | Veículos                 |
| `service`         | Serviços                 |
| `serviceorder`    | Ordens de serviço        |
| `supply`          | Peças e suprimentos      |
| `catalogservices` | Catálogo de serviços     |
| `reporting`       | Relatórios               |
| `user`            | Usuários e permissões    |

Esses contextos ficam isolados dentro da camada de domínio, permitindo que as regras de negócio permaneçam independentes dos detalhes de infraestrutura.

---

# ✨ Funcionalidades

## 👤 Clientes

* cadastro de clientes;
* consulta de clientes;
* atualização de informações;
* associação com veículos;
* autenticação através de CPF.

## 🚗 Veículos

* cadastro de veículos;
* associação com proprietários;
* consulta de veículos;
* gerenciamento dos dados do veículo.

## 🔧 Serviços

* cadastro de serviços;
* consulta do catálogo;
* gerenciamento dos serviços realizados.

## 📋 Ordens de Serviço

O domínio de ordens de serviço concentra o fluxo de atendimento da oficina.

Inclui conceitos relacionados a:

* criação de OS;
* serviços associados;
* peças utilizadas;
* status da ordem;
* execução do serviço;
* conclusão.

O domínio possui modelos, estados, exceções e casos de uso próprios.

## 📦 Suprimentos

Gerenciamento de peças e outros itens utilizados pela oficina.

## 👥 Usuários

Gerenciamento de usuários e seus papéis de acesso.

## 📊 Relatórios

O sistema possui recursos para geração de relatórios, incluindo geração de documentos PDF.

A aplicação utiliza a biblioteca iText para essa finalidade.

---

# 🛠️ Tecnologias

| Tecnologia                   | Utilização                    |
| ---------------------------- | ----------------------------- |
| **Java 21**                  | Linguagem principal           |
| **Spring Boot 4.0.5**        | Framework principal           |
| **Spring Web**               | APIs HTTP                     |
| **Spring Data JPA**          | Persistência                  |
| **Spring Security**          | Segurança                     |
| **JWT**                      | Autenticação baseada em token |
| **PostgreSQL**               | Banco de dados                |
| **H2**                       | Banco para testes             |
| **Spring Kafka**             | Mensageria                    |
| **Thymeleaf**                | Templates                     |
| **OpenAPI Generator**        | Geração das interfaces da API |
| **SpringDoc**                | Documentação OpenAPI          |
| **Actuator**                 | Health checks e métricas      |
| **Micrometer Prometheus**    | Métricas                      |
| **Logstash Logback Encoder** | Logs estruturados             |
| **JUnit**                    | Testes                        |
| **Mockito**                  | Testes unitários              |
| **Cucumber**                 | Testes BDD                    |
| **Testcontainers**           | Testes de integração          |
| **JaCoCo**                   | Cobertura de testes           |
| **SonarCloud**               | Qualidade de código           |
| **Docker**                   | Containerização               |
| **Maven**                    | Build e dependências          |

As dependências e versões principais estão declaradas no `pom.xml`.

---

# 📁 Estrutura do projeto

```text
mnl-oficina-mecanica/
│
├── .github/
│   ├── workflows/
│   │   ├── deploy.yaml
│   │   └── sonar.yml
│   └── CODEOWNERS
│
├── docker-local/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/fiap/postech/
│   │   │       ├── adapter/
│   │   │       │   ├── input/
│   │   │       │   └── output/
│   │   │       │
│   │   │       ├── config/
│   │   │       ├── domain/
│   │   │       │   ├── authentication/
│   │   │       │   ├── catalogservices/
│   │   │       │   ├── owner/
│   │   │       │   ├── reporting/
│   │   │       │   ├── service/
│   │   │       │   ├── serviceorder/
│   │   │       │   ├── supply/
│   │   │       │   ├── user/
│   │   │       │   └── vehicle/
│   │   │       │
│   │   │       ├── exception/
│   │   │       └── port/
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       ├── openapi/
│   │       ├── templates/
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       └── application-ci.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

A estrutura de recursos também contém scripts/artefatos de seed do banco, especificação OpenAPI, templates e configurações específicas para ambientes local e CI.

---

# 🌐 API

A API REST é implementada através do Spring Web.

As interfaces da API são geradas a partir de uma especificação **OpenAPI 3.0.3**, utilizando o OpenAPI Generator.

A configuração do projeto utiliza:

```text
OpenAPI Generator: 7.15.0
Specification:     OpenAPI 3.0.3
Generator:         Spring
Spring Boot 4:     enabled
```

O projeto também utiliza SpringDoc para documentação da API.

A especificação pode ser encontrada em:

```text
src/main/resources/openapi/
```

---

# 🔐 Autenticação e autorização

A aplicação utiliza **Spring Security** e **JWT** para autenticação.

O sistema possui diferentes papéis de acesso, permitindo controlar as operações disponíveis para cada tipo de usuário.

A camada de autenticação pertence ao domínio:

```text
domain/authentication
```

e as configurações de segurança ficam desacopladas das regras de negócio.

O objetivo é permitir que o domínio permaneça independente dos detalhes específicos do framework.

---

# 🗄️ Banco de dados

O banco de dados principal utilizado pela aplicação é:

```text
PostgreSQL
```

A persistência é implementada utilizando:

```text
Spring Data JPA
```

Para testes, o projeto também possui suporte ao:

```text
H2
```

e utiliza **Testcontainers** para cenários que necessitam de infraestrutura real durante os testes.

Os recursos relacionados ao banco ficam em:

```text
src/main/resources/db/
```

incluindo recursos de seed.

---

# 📡 Mensageria

O monólito possui integração com **Apache Kafka** através do Spring Kafka.

Essa integração permite desacoplar determinados fluxos assíncronos da aplicação e possibilita a comunicação com outros componentes da solução.

Um dos consumidores dessa arquitetura é o microsserviço de orçamentos.

```text
Monólito
   │
   │ evento
   ▼
 Kafka
   │
   ▼
MS Orçamentos
```

---

# 📊 Observabilidade

A aplicação possui suporte nativo a observabilidade.

## Actuator

O Spring Boot Actuator disponibiliza endpoints para:

* health checks;
* métricas;
* monitoramento da aplicação.

## Prometheus

As métricas são disponibilizadas através do:

```text
Micrometer Prometheus Registry
```

permitindo integração com a stack de observabilidade do Kubernetes.

## Logs

O projeto utiliza:

```text
Logstash Logback Encoder
```

para geração de logs estruturados.

Isso facilita a ingestão e análise dos logs em ambientes distribuídos.

---

# 🧪 Testes

O projeto possui diferentes níveis de testes.

### Testes unitários

Utilizando:

```text
JUnit
Mockito
```

### Testes BDD

Utilizando:

```text
Cucumber
```

### Testes de integração

Utilizando:

```text
Testcontainers
```

e bancos/infraestrutura reais em containers durante os testes.

O projeto também possui configuração de JaCoCo para geração do relatório de cobertura.

Executar:

```bash
./mvnw test
```

Windows:

```cmd
mvnw.cmd test
```

---

# 📈 Qualidade de código

O projeto possui integração com **SonarCloud**.

Configuração atual:

```text
Organization: grupo-soat
Project: Grupo-SOAT_mnl-oficina-mecanica
```

A análise de cobertura utiliza o relatório gerado pelo JaCoCo.

Algumas camadas técnicas são excluídas da análise de cobertura, como:

```text
config
port
adapter/input/api/model
port/api
```

permitindo concentrar a cobertura principalmente nas regras relevantes da aplicação.

---

# 🐳 Docker

O projeto possui um Dockerfile multi-stage.

### Build

O primeiro estágio utiliza:

```text
eclipse-temurin:21-jdk-alpine
```

para compilar a aplicação através do Maven.

### Runtime

A aplicação também utiliza:

```text
eclipse-temurin:21-jdk-alpine
```

como imagem de execução.

A aplicação expõe:

```text
8080
```

e possui um health check baseado no endpoint:

```text
/actuator/health/readiness
```

---

# 💻 Execução local

## Pré-requisitos

Instale:

* Java 21;
* Maven ou utilize o Maven Wrapper;
* Docker;
* PostgreSQL, caso não utilize container;
* Git.

Verifique:

```bash
java -version
```

---

## Clonar

```bash
git clone https://github.com/Grupo-SOAT/mnl-oficina-mecanica.git
cd mnl-oficina-mecanica
```

---

## Compilar

```bash
./mvnw clean package
```

No Windows:

```cmd
mvnw.cmd clean package
```

---

## Executar

```bash
./mvnw spring-boot:run
```

Ou:

```bash
java -jar target/*.jar
```

---

# 🐳 Executar com Docker

Construir a imagem:

```bash
docker build -t oficina-mecanica-mnl .
```

Executar:

```bash
docker run -p 8080:8080 oficina-mecanica-mnl
```

A aplicação estará disponível na porta:

```text
8080
```

---

# 🔄 CI/CD

O repositório possui workflows do GitHub Actions para:

```text
.github/workflows/
├── deploy.yaml
└── sonar.yml
```

O workflow de qualidade é responsável pela análise através do SonarCloud.

O workflow de deploy integra o código da aplicação ao processo de entrega da infraestrutura.

---

# ☁️ Deploy na AWS

No ambiente AWS, o monólito é executado como container no:

```text
Amazon EKS
```

A imagem Docker é publicada no:

```text
Amazon ECR
```

com repository:

```text
registry-oficina-mecanica-mnl
```

O deployment Kubernetes é mantido no repositório:

```text
Grupo-SOAT/k8s-infra-oficina-mecanica
```

e o Argo CD realiza a sincronização do estado desejado para o cluster.

Fluxo:

```text
Código
  │
  ▼
GitHub Actions
  │
  ├── Testes
  ├── Build
  └── Docker
       │
       ▼
     ECR
       │
       ▼
k8s-infra-oficina-mecanica
       │
       ▼
    Argo CD
       │
       ▼
      EKS
```

---

# 🔗 Relação com os demais componentes

O monólito é o componente central da aplicação.

```text
                    ┌──────────────────┐
                    │      Cliente     │
                    └────────┬─────────┘
                             │
                             ▼
                       API Gateway
                             │
                             ▼
                          Lambda
                             │
                             ▼
                    ┌──────────────────┐
                    │     Monólito     │
                    │                  │
                    │ Oficina Mecânica │
                    └───────┬──────────┘
                            │
                     ┌──────┴───────┐
                     ▼              ▼
                PostgreSQL        Kafka
                                    │
                                    ▼
                              MS Orçamentos
```

O projeto é complementado pelos repositórios de infraestrutura, Lambda e microsserviço de orçamentos.

---

# 📚 Projeto

Este repositório faz parte da solução **Oficina Mecânica – Tech Challenge Fase 3**, desenvolvida pelo **Grupo-SOAT**.

---

# 📄 Licença

Este projeto está licenciado sob a licença **MIT**.

Consulte o arquivo [`LICENSE`](./LICENSE).

# 🔐 Identity Service - Conecta Saúde

Este repositório contém o **Identity Service**, o microsserviço central responsável pela autenticação, autorização e gestão completa dos utilizadores do sistema Conecta Saúde.

## 🎯 Funcionalidades Principais

*   **Gestão de Perfis de Utilizador:** Criação e administração de contas com diferentes níveis de acesso (Pacientes, Médicos, Receção e Administradores).
*   **Segurança e Autenticação:** Implementação de fluxos de segurança robustos para login e validação de identidade[cite: 10].
*   **Recuperação de Credenciais:** APIs dedicadas para redefinição de palavra-passe (*password reset*)[cite: 10].
*   **Catálogo Clínico:** Gestão do cadastro de médicos e das respetivas especialidades clínicas[cite: 10].
*   **Fornecedor de Identidade:** Atua como a fonte de verdade para outros microsserviços (como o `appointment-service` e o `clinical-service`) que precisam de validar dados de utilizadores.

## 🛠️ Tecnologias Utilizadas

*   **Java**
*   **Spring Boot** (Core, Web, Security)
*   **Spring Data JPA** & **PostgreSQL** (Persistência e infraestrutura de base de dados)[cite: 10]
*   **Maven** (Gestão de dependências através do *wrapper* incluído)[cite: 10]
*   **Docker & Docker Compose** (Contentorização e alinhamento de portas locais)[cite: 10]

## 📦 Base de Dados (Database-per-Service)

Seguindo as boas práticas de microsserviços, este serviço possui a sua própria base de dados relacional isolada:
*   **Motor:** PostgreSQL
*   **Nome:** `db_identity`
*   **Porta Local (Docker):** Acesso configurado via `docker-compose.yml`[cite: 10]

## 🚀 Como Executar

### Pré-requisitos
*   É necessário ter a base de dados PostgreSQL em execução. A infraestrutura de base de dados já se encontra configurada[cite: 10].

### Localmente (Dev Mode)
Para iniciar o serviço isoladamente usando o *wrapper* do Maven[cite: 10], executa o seguinte comando na raiz do projeto:

```bash
./mvnw spring-boot:run

Via Docker
Para executar em conjunto com os restantes microsserviços, utiliza o ficheiro docker-compose.yml[cite: 10] no repositório principal do ecossistema.

📚 Documentação Adicional
A documentação adicional do projeto e da API pode ser encontrada na diretoria /docs[cite: 10].

🤝 Autor
Desenvolvido por João como pilar de segurança e identidade do sistema Conecta Saúde.
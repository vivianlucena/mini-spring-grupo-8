
# API de Serviços de Pet Shop — Grupo 8

API REST desenvolvida em **Java 21** e **Spring Boot 3.5.8** para gerenciar serviços oferecidos por um pet shop.

Este projeto representa a migração de uma aplicação originalmente desenvolvida com um mini-framework Java próprio para o ecossistema Spring Boot, preservando as funcionalidades, a arquitetura em camadas e as regras de negócio da implementação original.

## Integrantes

- Alice Flávia Félix Lucena
- Vívian Gabryelle Gomes Lucena Silva
- Luís Henrique dos Anjos Silva
- José Carlos da Costa
- José Kayo Matheus Ferreira

## Sobre o projeto

A aplicação permite cadastrar, listar, consultar, atualizar e excluir serviços oferecidos por um pet shop.

Cada serviço possui os seguintes atributos:

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Long | Identificador gerado automaticamente |
| `nome` | String | Nome do serviço |
| `duracaoMinutos` | Integer | Duração do serviço em minutos |

O identificador é gerado pela aplicação e permanece inalterado durante as atualizações.

Os dados são armazenados em memória, portanto são perdidos quando a aplicação é encerrada ou reiniciada.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.5.8
- Spring Web / Spring MVC
- Spring Validation (Jakarta Bean Validation)
- Apache Tomcat embarcado
- Maven
- Jackson
- JUnit 5
- Spring Boot Test

## Migração do Mini Spring para Spring Boot

O objetivo da migração foi substituir os mecanismos implementados manualmente no mini-framework original pelas soluções oferecidas pelo Spring Boot, preservando o comportamento da API.

| Mini Spring (antes) | Spring Boot (depois) |
|---|---|
| Inicialização manual da aplicação | `SpringApplication.run(...)` |
| Servidor Jetty | Tomcat embarcado |
| Anotações próprias de roteamento | `@RestController`, `@GetMapping`, `@PostMapping`, `@PutMapping` e `@DeleteMapping` |
| Container próprio de injeção de dependências | Gerenciamento de beans pelo Spring |
| Anotações próprias de componentes | `@Service`, `@Repository` e `@RestController` |
| Validação própria | Jakarta Bean Validation |
| Tratamento manual de erros HTTP | `@RestControllerAdvice` e `ResponseStatusException` |
| Interceptação de requisições | `OncePerRequestFilter` |

A aplicação utiliza injeção de dependências por construtor, mantendo o desacoplamento entre as camadas.

O código do mini-framework original foi removido da branch de migração, pois não é mais necessário para executar a aplicação.

### Organização das branches

- `main`: preserva a implementação original com Mini Spring.
- `migrate-to-spring`: contém a implementação migrada para Spring Boot.

Essa separação permite comparar as duas versões e identificar as alterações realizadas durante a migração.

## Arquitetura

O projeto mantém a arquitetura em camadas, separando as responsabilidades da aplicação.

```text
src/
├── main/
│   ├── java/
│   │   └── br/com/minispring/exemplo/
│   │       ├── Application.java
│   │       ├── controller/
│   │       │   ├── InfoController.java
│   │       │   └── ServicoController.java
│   │       ├── exception/
│   │       │   └── GlobalExceptionHandler.java
│   │       ├── filter/
│   │       │   └── RequestIdFilter.java
│   │       ├── model/
│   │       │   ├── Servico.java
│   │       │   └── ServicoInput.java
│   │       ├── repository/
│   │       │   ├── ServicoRepository.java
│   │       │   └── InMemoryServicoRepository.java
│   │       └── service/
│   │           └── ServicoService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/br/com/minispring/
        ├── ApiIntegrationTest.java
        ├── ApplicationContextTest.java
        ├── RouterTest.java
        └── UnexpectedErrorIntegrationTest.java
```

### Responsabilidades das camadas

- **Controller:** recebe requisições HTTP e retorna as respostas da API.
- **Service:** implementa as regras de negócio e coordena as operações.
- **Repository:** abstrai o acesso aos dados e realiza o armazenamento em memória.
- **Model:** representa os serviços e os dados recebidos nas requisições.
- **Exception:** centraliza o tratamento de erros HTTP.
- **Filter:** gera identificadores para rastreamento das requisições.

A classe `Application.java`, anotada com `@SpringBootApplication`, é responsável por inicializar o contexto Spring e o servidor embarcado.

## Como executar

### Pré-requisitos

- JDK 21
- Maven compatível com o projeto
- IntelliJ IDEA ou outra IDE Java (opcional)
- Acesso à internet na primeira execução para baixar as dependências

### Clonar o repositório

```bash
git clone https://github.com/vivianlucena/mini-spring-grupo-8.git
cd mini-spring-grupo-8
git checkout migrate-to-spring
```

### Compilar e executar os testes

Na raiz do projeto, onde está localizado o arquivo `pom.xml`, execute:

```bash
mvn clean test
```

Esse comando compila o projeto e executa os testes automatizados.

No IntelliJ IDEA, também é possível executar os objetivos `clean` e `test` pela janela **Maven > Lifecycle**, sem precisar configurar o Maven no terminal.

### Executar a aplicação

Para iniciar diretamente com Maven:

```bash
mvn spring-boot:run
```

Também é possível executar o método `main` da classe:

```text
br.com.minispring.exemplo.Application
```

### Gerar e executar o JAR

Para empacotar a aplicação:

```bash
mvn clean package
```

Depois, execute:

```bash
java -jar target/mini-spring-2.0.0.jar
```

O arquivo JAR é gerado pelo Maven com o plugin do Spring Boot.

A API estará disponível em:

```text
http://localhost:8080
```

### Verificar a inicialização

Acesse:

```http
GET http://localhost:8080/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

## Endpoints da API

| Método | Endpoint | Descrição | Resposta esperada |
|---|---|---|---|
| GET | `/` | Informações da aplicação | 200 OK |
| GET | `/health` | Verifica o estado da aplicação | 200 OK |
| GET | `/servicos` | Lista os serviços cadastrados | 200 OK |
| GET | `/servicos?nome=banho` | Filtra serviços pelo nome | 200 OK |
| GET | `/servicos/{id}` | Consulta um serviço pelo ID | 200 OK |
| POST | `/servicos` | Cadastra um serviço | 201 Created |
| PUT | `/servicos/{id}` | Atualiza um serviço | 200 OK |
| DELETE | `/servicos/{id}` | Exclui um serviço | 204 No Content |

### 1. Cadastrar um serviço

**Requisição:**

```http
POST /servicos
Content-Type: application/json
```

**Corpo JSON:**

```json
{
  "nome": "Banho básico",
  "duracaoMinutos": 40
}
```

**Exemplo de resposta:**

```json
{
  "id": 1,
  "nome": "Banho básico",
  "duracaoMinutos": 40
}
```

Quando o cadastro é realizado com sucesso, a API retorna `201 Created`, os dados do serviço criado e o cabeçalho `Location` com o endereço do recurso.

### 2. Listar serviços

```http
GET /servicos
```

Retorna a lista de serviços cadastrados, ordenada pelo identificador.

### 3. Filtrar serviços pelo nome

```http
GET /servicos?nome=BANHO
```

O filtro realiza uma busca parcial e não diferencia letras maiúsculas de minúsculas.

Por exemplo, pesquisar `BANHO` encontra serviços como `Banho básico` e `Banho completo`.

### 4. Consultar um serviço pelo ID

```http
GET /servicos/1
```

Retorna o serviço correspondente ao identificador informado.

Caso o serviço não exista, retorna `404 Not Found`.

### 5. Atualizar um serviço

```http
PUT /servicos/1
Content-Type: application/json
```

```json
{
  "nome": "Banho e tosa",
  "duracaoMinutos": 90
}
```

A atualização modifica os dados do serviço, preservando seu identificador original.

### 6. Excluir um serviço

```http
DELETE /servicos/1
```

Quando a exclusão é realizada com sucesso, a API retorna `204 No Content`, sem corpo na resposta.

## Validações e regras de negócio

A aplicação utiliza Jakarta Bean Validation e validações específicas na camada Service.

As principais regras são:

- O nome do serviço é obrigatório.
- O nome não pode ser vazio ou conter apenas espaços.
- A duração deve ser um número inteiro positivo.
- A duração máxima permitida é de 240 minutos.
- O identificador é gerado automaticamente.
- O identificador é preservado durante atualizações.
- O filtro por nome não diferencia letras maiúsculas de minúsculas.
- Serviços inexistentes retornam `404 Not Found`.
- Durações superiores a 240 minutos retornam `422 Unprocessable Entity`.

### Principais códigos HTTP

| Código | Significado |
|---|---|
| 200 | Operação realizada com sucesso |
| 201 | Serviço criado com sucesso |
| 204 | Serviço excluído com sucesso |
| 400 | Requisição ou dados inválidos |
| 404 | Serviço ou rota não encontrado |
| 405 | Método HTTP não permitido |
| 415 | Tipo de conteúdo não suportado |
| 422 | Regra de negócio violada |
| 500 | Erro interno inesperado |

### Tratamento de JSON inválido

A aplicação utiliza `@RestControllerAdvice` para centralizar o tratamento de exceções.

Requisições com JSON malformado, corpo vazio ou o literal JSON `null` são rejeitadas com HTTP `400 Bad Request`.

Exemplo de requisição inválida:

```http
POST /servicos
Content-Type: application/json

null
```

Exemplo de resposta:

```json
{
  "status": 400,
  "message": "Corpo da requisição inválido",
  "requestId": "identificador-da-requisicao"
}
```

O comportamento para o corpo JSON `null` foi confirmado em teste manual com a aplicação em execução.

## Identificação das requisições

O projeto possui um filtro HTTP implementado com `OncePerRequestFilter`.

Para cada requisição, é gerado um identificador UUID, disponibilizado no cabeçalho:

```http
X-Request-Id: identificador-da-requisicao
```

Esse identificador também é incluído nas respostas de erro tratadas pelo `GlobalExceptionHandler`.

Isso facilita a identificação e o rastreamento de requisições.

## Testes automatizados

Os testes utilizam JUnit 5 e os recursos de teste do Spring Boot.

As classes de teste são:

| Classe | Responsabilidade |
|---|---|
| `ApiIntegrationTest` | Testes HTTP do CRUD, validações, erros e concorrência |
| `ApplicationContextTest` | Testes de injeção de dependências e contexto Spring |
| `RouterTest` | Testes de roteamento e métodos HTTP |
| `UnexpectedErrorIntegrationTest` | Testes de falhas inesperadas e tratamento de erros |

### Cenários cobertos

- Inicialização do contexto Spring.
- Injeção de dependências.
- Cadastro e consulta de serviços.
- Listagem e filtro parcial por nome.
- Atualização preservando o identificador.
- Exclusão de serviços.
- Consulta de recursos inexistentes.
- Validação dos dados de entrada.
- Tratamento de JSON inválido.
- Rejeição do JSON literal `null`.
- Tratamento dos códigos HTTP 400, 404, 405, 415, 422 e 500.
- Verificação do cabeçalho `Location`.
- Requisições concorrentes e geração de identificadores únicos.
- Identificação das requisições com `X-Request-Id`.
- Tratamento de falhas inesperadas sem exposição de detalhes internos.

Para executar os testes:

```bash
mvn test
```

Durante o desenvolvimento, a suíte foi executada com sucesso no IntelliJ IDEA após a migração.

Também foram realizados testes manuais com a aplicação em execução, verificando CRUD, filtro por nome, validações e tratamento de erros.

## Exemplos de requisições

O arquivo `requests.http`, localizado na raiz do projeto, contém exemplos para testar os endpoints e os principais cenários de validação.

As requisições podem ser executadas por ferramentas compatíveis com arquivos HTTP, pelo PowerShell ou por clientes REST.

## Utilização de ferramentas de IA

Ferramentas de inteligência artificial foram utilizadas como apoio durante o desenvolvimento, auxiliando na análise, implementação, revisão, migração e documentação do código.

As decisões finais, a integração das alterações e a validação do funcionamento foram realizadas pela equipe.

## Projeto-base

O projeto utiliza como base o repositório Mini Spring, disponibilizado para a atividade:

https://github.com/ferreiralisson/mini-spring

## Repositório do projeto

https://github.com/vivianlucena/mini-spring-grupo-8

**Branch da migração:**

https://github.com/vivianlucena/mini-spring-grupo-8/tree/migrate-to-spring

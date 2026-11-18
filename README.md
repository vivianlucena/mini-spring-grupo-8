
# API de Serviços de Pet Shop — Grupo 8

API REST desenvolvida em **Java 21** e **Spring Boot 3.5.8** para gerenciar serviços oferecidos por um pet shop. Esta versão corresponde à migração do projeto original, que utilizava um mini-framework próprio, para o ecossistema Spring Boot, preservando a arquitetura em camadas, o CRUD e as regras de negócio.

## Integrantes

- Alice Flávia Félix Lucena
- Vívian Gabryelle Gomes Lucena Silva
- Luís Henrique dos Anjos Silva
- José Carlos da Costa
- José Kayo Matheus Ferreira

## Funcionalidades

A aplicação permite cadastrar, listar, consultar, atualizar e excluir serviços. Cada serviço possui os seguintes atributos:

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Long | Identificador gerado automaticamente |
| `nome` | String | Nome do serviço |
| `duracaoMinutos` | Integer | Duração do serviço em minutos |

O identificador é gerado pela aplicação e permanece o mesmo durante uma atualização. O armazenamento é **em memória**, portanto os registros são perdidos quando a aplicação é reiniciada.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3.5.8
- Spring Web / Spring MVC
- Spring Validation (Jakarta Bean Validation)
- Apache Tomcat embarcado
- Maven
- Jackson (serialização e desserialização JSON)
- JUnit 5 e Spring Boot Test

## Migração do Mini Spring para Spring Boot

O projeto foi originalmente construído com um mini-framework próprio. Nesta versão, os recursos implementados manualmente foram substituídos pelos mecanismos equivalentes do Spring Boot:

| Antes — Mini Spring | Depois — Spring Boot |
|---|---|
| Inicialização e servidor configurados no projeto | `SpringApplication.run(...)` e Tomcat embarcado |
| Roteamento HTTP do framework próprio | `@RestController` e mapeamentos Spring MVC |
| Container próprio de injeção de dependências | Injeção por construtor e gerenciamento de beans pelo Spring |
| Validação própria das entradas | Jakarta Bean Validation e validações de negócio na camada Service |
| Tratamento manual de exceções HTTP | `@RestControllerAdvice` e `ResponseStatusException` |
| Identificação de requisições | Filtro integrado ao ciclo HTTP do Spring (`OncePerRequestFilter`) |

O código do mini-framework não é mais necessário para executar a versão migrada. A implementação original foi preservada na branch `main`, enquanto a migração é desenvolvida na branch `migrate-to-spring`.

## Arquitetura

O projeto mantém a divisão por responsabilidades:

```text
src/main/java/br/com/minispring/exemplo/
├── Application.java
├── controller/
│   ├── InfoController.java
│   └── ServicoController.java
├── exception/
│   └── GlobalExceptionHandler.java
├── filter/
│   └── RequestIdFilter.java
├── model/
│   ├── Servico.java
│   └── ServicoInput.java
├── repository/
│   ├── ServicoRepository.java
│   └── InMemoryServicoRepository.java
└── service/
    └── ServicoService.java
```

- **Controller:** recebe requisições HTTP e produz respostas REST.
- **Service:** centraliza as regras de negócio.
- **Repository:** abstrai o acesso aos dados e armazena os serviços em memória.
- **Model:** representa os dados da aplicação e os dados de entrada.
- **Exception:** padroniza o tratamento de erros da API.
- **Filter:** atribui um identificador às requisições HTTP.

A camada Service recebe o Repository por **injeção de dependências via construtor**.

## Como executar

### Pré-requisitos

- JDK 21
- Maven (instalado ou disponível pela IDE)
- Acesso à internet na primeira execução para baixar as dependências

### Executar testes

Na raiz do projeto, onde está `pom.xml`:

```bash
mvn clean test
```

Alternativamente, no IntelliJ IDEA, abra a janela **Maven** e execute **Lifecycle > clean** e **Lifecycle > test**.

### Iniciar a aplicação

Com Maven:

```bash
mvn spring-boot:run
```

Ou abra `src/main/java/br/com/minispring/exemplo/Application.java` no IntelliJ IDEA e execute o método `main`.

Para gerar o JAR executável e iniciá-lo:

```bash
mvn clean package
java -jar target/mini-spring-1.0.0.jar
```

A API fica disponível em `http://localhost:8080`.

Para verificar a inicialização, abra:

```http
GET http://localhost:8080/health
```

Resposta esperada:

```json
{"status":"UP"}
```

## Endpoints da API

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| GET | `/` | Informações da API | 200 OK |
| GET | `/health` | Estado da aplicação | 200 OK |
| GET | `/servicos` | Lista os serviços | 200 OK |
| GET | `/servicos?nome=banho` | Filtra serviços por parte do nome | 200 OK |
| GET | `/servicos/{id}` | Consulta um serviço pelo ID | 200 OK |
| POST | `/servicos` | Cadastra um serviço | 201 Created |
| PUT | `/servicos/{id}` | Atualiza um serviço | 200 OK |
| DELETE | `/servicos/{id}` | Exclui um serviço | 204 No Content |

### Cadastrar um serviço

```http
POST /servicos
Content-Type: application/json
```

```json
{
  "nome": "Banho básico",
  "duracaoMinutos": 40
}
```

Em caso de sucesso, a API retorna **201 Created**, os dados do novo serviço e o cabeçalho `Location` indicando o recurso criado.

### Listar e pesquisar

```http
GET /servicos
GET /servicos?nome=BANHO
GET /servicos/1
```

O filtro encontra correspondências **parciais**, sem diferenciar letras maiúsculas de minúsculas. Por exemplo, `BANHO` encontra `Banho básico`.

### Atualizar um serviço

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

O ID do serviço é preservado.

### Excluir um serviço

```http
DELETE /servicos/1
```

Quando a exclusão é bem-sucedida, a API retorna **204 No Content**, sem corpo.

## Validações e regras de negócio

- O nome é obrigatório e não pode conter apenas espaços.
- A duração deve ser um número inteiro positivo.
- A duração máxima é de **240 minutos**.
- O ID é gerado automaticamente e preservado nas atualizações.
- O filtro por nome ignora diferenças entre maiúsculas e minúsculas.
- Serviços inexistentes retornam **404 Not Found**.

### Principais códigos HTTP

| Código | Significado |
|---|---|
| 200 | Requisição bem-sucedida |
| 201 | Serviço criado |
| 204 | Serviço excluído, sem conteúdo |
| 400 | Dados ou parâmetros inválidos |
| 404 | Serviço ou rota inexistente |
| 405 | Método HTTP não permitido |
| 415 | Tipo de conteúdo não suportado |
| 422 | Regra de negócio violada, como duração acima de 240 minutos |
| 500 | Falha interna inesperada |

Outros códigos HTTP podem ser produzidos pelo servidor, conforme o tipo e o tamanho da requisição.

## Testes

Os testes automatizados utilizam **JUnit 5** e ferramentas de teste do Spring Boot. A migração inclui testes de endpoints REST, regras de validação, tratamento de exceções, inicialização do contexto e injeção de dependências.

Além dos testes automatizados, foram executadas verificações manuais de inicialização, `GET /health`, CRUD completo, filtro parcial sem distinção entre maiúsculas e minúsculas e respostas HTTP **400**, **404** e **422**.

Para executar a suíte:

```bash
mvn test
```

## Exemplos de requisições

O arquivo `requests.http`, na raiz do projeto, contém exemplos de requisições HTTP para testar a API.

## Utilização de ferramentas de IA

Ferramentas de inteligência artificial foram utilizadas como apoio na análise, implementação, revisão e documentação do código. As decisões finais, a integração das alterações e a verificação do funcionamento foram realizadas pela equipe.

## Projeto-base

A implementação original foi desenvolvida a partir do repositório Mini Spring disponibilizado para a atividade:

https://github.com/ferreiralisson/mini-spring

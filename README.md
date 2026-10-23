# API de Serviços de Pet Shop — Grupo 8

API REST desenvolvida em Java 21 para o gerenciamento de serviços oferecidos por um pet shop. O projeto utiliza o mini-framework web fornecido como base para a atividade, com foco em arquitetura em camadas, injeção de dependências, validação de dados e operações CRUD.

## Integrantes

- Alice Flávia Félix Lucena
- Vívian Gabryelle Gomes Lucena Silva
- Luís Henrique dos Anjos Silva
- José Carlos da Costa
- José Kayo Matheus Ferreira

## Sobre o projeto

A aplicação permite cadastrar, consultar, listar, atualizar e excluir serviços de um pet shop.

Cada serviço possui os seguintes atributos:

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Long | Identificador gerado automaticamente |
| `nome` | String | Nome do serviço |
| `duracaoMinutos` | Integer | Duração do serviço em minutos |

O identificador é gerado pela aplicação e não pode ser alterado pelo cliente durante uma atualização.

## Tecnologias utilizadas

- Java 21
- Maven
- Jetty
- Jackson
- JUnit 5
- Mini-framework web fornecido no projeto-base

## Arquitetura

O projeto utiliza uma arquitetura em camadas para separar as responsabilidades da aplicação.

```text
src/main/java/br/com/minispring/
├── framework/
│   ├── annotation/
│   ├── context/
│   ├── validation/
│   └── web/
└── exemplo/
    ├── Application.java
    ├── controller/
    │   ├── InfoController.java
    │   └── ServicoController.java
    ├── model/
    │   ├── Servico.java
    │   └── ServicoInput.java
    ├── repository/
    │   ├── ServicoRepository.java
    │   └── InMemoryServicoRepository.java
    └── service/
        └── ServicoService.java
```

### Responsabilidades das camadas

- **Controller:** recebe as requisições HTTP e define as respostas da API.
- **Service:** concentra as regras de negócio e valida as condições específicas dos serviços.
- **Repository:** define as operações de acesso aos dados e sua implementação em memória.
- **Model:** representa os serviços e os dados recebidos nas requisições.
- **Framework:** fornece os mecanismos de roteamento, injeção de dependências, validação e tratamento de requisições.

A aplicação utiliza injeção de dependências por construtor e uma interface de repositório para separar as regras de negócio do mecanismo de armazenamento.

A pasta `framework` faz parte do projeto-base. As adaptações da atividade estão concentradas na aplicação de exemplo.

## Como executar

### Pré-requisitos

- JDK 21
- Maven compatível com o projeto
- Acesso à internet na primeira compilação para baixar as dependências

### Compilar e executar os testes

Na raiz do projeto, onde está localizado o arquivo `pom.xml`, execute:

```bash
mvn clean verify
```

Esse comando compila a aplicação e executa os testes automatizados.

### Executar a aplicação

Para gerar o JAR e iniciar a aplicação:

```bash
java -jar target/mini-spring-1.0.0.jar
```

A API estará disponível em:

```text
http://127.0.0.1:8080
```

Também é possível executar o método `main` da classe `br.com.minispring.exemplo.Application` pela IDE.

**Observação:** os dados são armazenados em memória e são perdidos quando a aplicação é encerrada ou reiniciada.

## Endpoints da API

A rota-base dos serviços é `/servicos`.

| Método | Endpoint | Descrição | Resposta esperada |
|---|---|---|---|
| GET | `/servicos` | Lista todos os serviços | 200 OK |
| GET | `/servicos?nome=banho` | Filtra serviços pelo nome | 200 OK |
| GET | `/servicos/{id}` | Consulta um serviço pelo ID | 200 OK |
| POST | `/servicos` | Cadastra um serviço | 201 Created |
| PUT | `/servicos/{id}` | Atualiza um serviço | 200 OK |
| DELETE | `/servicos/{id}` | Exclui um serviço | 204 No Content |

### 1. Cadastrar um serviço

**Requisição**

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

Quando o cadastro é realizado com sucesso, a API retorna `201 Created`, os dados do serviço criado e o cabeçalho `Location` com o endereço do recurso.

### 2. Listar serviços

```http
GET /servicos
```

Retorna a lista de serviços cadastrados.

### 3. Filtrar pelo nome

```http
GET /servicos?nome=banho
```

O filtro procura o texto informado no nome dos serviços sem diferenciar letras maiúsculas de minúsculas.

### 4. Consultar por ID

```http
GET /servicos/1
```

Retorna o serviço correspondente ao identificador informado.

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

A atualização modifica os dados do serviço sem alterar seu identificador.

### 6. Excluir um serviço

```http
DELETE /servicos/1
```

Quando a exclusão é realizada com sucesso, a API retorna `204 No Content`, sem corpo na resposta.

## Validações e regras de negócio

A API aplica validações aos dados recebidos e às operações solicitadas.

- O nome do serviço é obrigatório e não pode conter apenas espaços.
- A duração deve ser um número inteiro positivo.
- A duração máxima permitida é de 240 minutos.
- O identificador é gerado automaticamente.
- O identificador existente é preservado durante atualizações.
- O filtro pelo nome não diferencia letras maiúsculas de minúsculas.
- Serviços inexistentes retornam erro de recurso não encontrado.

### Principais códigos HTTP

| Código | Significado |
|---|---|
| 200 | Operação realizada com sucesso |
| 201 | Serviço criado com sucesso |
| 204 | Serviço excluído com sucesso, sem conteúdo na resposta |
| 400 | Requisição ou dados inválidos |
| 404 | Serviço ou rota não encontrado |
| 405 | Método HTTP não permitido para a rota |
| 413 | Corpo da requisição excede o limite permitido |
| 415 | Tipo de conteúdo não suportado |
| 422 | Regra de negócio violada |
| 500 | Erro interno inesperado |

O código `422` é utilizado quando a duração informada ultrapassa o limite de 240 minutos.

## Testes automatizados

O projeto possui testes para verificar o comportamento do framework e da API.

Os cenários incluem:

- Operações CRUD por requisições HTTP.
- Cadastro e consulta de serviços.
- Filtro por nome.
- Atualização preservando o identificador.
- Exclusão e consulta de recursos inexistentes.
- Validação dos dados de entrada.
- Tratamento de erros HTTP.
- Métodos e rotas não permitidos.
- Requisições concorrentes e geração de identificadores únicos.
- Comportamento do container de injeção de dependências.
- Tratamento de falhas inesperadas.

Para executar os testes:

```bash
mvn test
```

Na execução realizada durante o desenvolvimento, os 15 testes passaram.

## Exemplos de requisições

O arquivo `requests.http`, localizado na raiz do projeto, contém exemplos prontos para testar os endpoints e os principais cenários de validação pela IDE.

## Utilização de ferramentas de IA

Ferramentas de inteligência artificial foram utilizadas como apoio durante o desenvolvimento, para auxiliar na análise, implementação, revisão e documentação do código. As decisões finais, a integração das alterações e a validação do funcionamento foram realizadas pela equipe.

## Projeto-base

O projeto utiliza como base o repositório Mini Spring, disponibilizado para a atividade:

https://github.com/ferreiralisson/mini-spring

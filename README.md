# AutoEscola 3ESPG — API REST de agendamento de instruções

| | |
|---|---|
| **Instituição** | FIAP |
| **Curso** | Engenharia de Software |
| **Disciplina** | Arquitetura Orientada a Serviços (SOA) e Web Services |
| **Atividade** | Checkpoint 5 |
| **Professor** | Carlos Eduardo Machado de Oliveira |

## Integrantes do grupo

| Nome | RM |
|---|---|
| Felipe Braunstein e Silva | 554483 |
| Felipe do Nascimento Fernandes | 554598 |
| Henrique Ignacio Bartalo | 555274 |
| Gustavo Henrique Martins | 556956 |

## Descrição

API REST para o agendamento de instruções de uma autoescola. Contém CRUD de **Instrutores**, **Alunos**, **Usuários** e **Instruções** (agendamento, consulta e cancelamento), autenticação JWT, regras de negócio de agendamento e, no Checkpoint 5:

1. **Consumo de API externa** — consulta de endereço pelo CEP na API pública **ViaCEP**.
2. **Documentação automática** com Swagger UI / OpenAPI (springdoc).
3. **Configuração de CORS** restrita a origens configuráveis.
4. **Testes automatizados** para cada entidade (JUnit 5, Mockito, MockMvc, H2).

## Tecnologias

Java 21 · Spring Boot 4.0.5 · Spring Web MVC · Spring Data JPA (Hibernate) · Spring Security + JWT (`java-jwt`) · Flyway · MySQL 8 · springdoc-openapi 3.0.3 · Lombok · JUnit 5 · Mockito · H2 (apenas testes) · Maven

## Estrutura de pastas

```
src/main/java/br/com/fiap3espg/autoescola3espg
├── controller/            Aluno, Instrutor, Usuario, Instrucao, Cep, Login, HealthCheck
├── service/               AlunoService, InstrutorService, UsuarioService, InstrucaoService, AutenticacaoService
├── domain/
│   ├── aluno/ instrutor/ usuario/ instrucao/ endereco/   Entidades, DTOs (records), repositórios
│   └── instrucao/validacao/                              Regras de negócio do agendamento
├── integration/cep/       CepService, CepClientConfig, ConsultaCepResponse, exceções (API externa ViaCEP)
└── infra/
    ├── security/          SecurityConfig (JWT + CORS), SecurityFilter, TokenService, AdminInicializador
    ├── documentation/     SwaggerConfig (OpenAPI + esquema JWT)
    └── exception/         TratadorGlobalErros
src/main/resources         application.properties e migrations Flyway (db/migration)
src/test                   Testes automatizados e application-test.properties (H2)
```

## Requisitos

- **JDK 17 ou superior** (projeto configurado para **JDK 21**)
- **MySQL 8** em execução (porta 3306)
- **IntelliJ IDEA** (ou outra IDE com Maven). O Maven Wrapper (`mvnw`) acompanha o projeto, não é necessário instalar o Maven.
- Acesso à internet (download das dependências e consulta ao ViaCEP)

## Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `JWT_SECRET` | **Sim** | — | Chave secreta para assinar o JWT (use texto longo e aleatório, 32+ caracteres). A aplicação **não inicia** sem ela. |
| `DB_PASSWORD` | Se o MySQL tiver senha | *(vazio)* | Senha do usuário do banco |
| `DB_USER` | Não | `root` | Usuário do MySQL |
| `DB_URL` | Não | `jdbc:mysql://localhost:3306/autoescola3espg?createDatabaseIfNotExist=true` | URL JDBC |
| `ADMIN_PASSWORD` | Recomendada no 1º uso | — | Se definida e a tabela `usuarios` estiver vazia, cria o administrador inicial |
| `ADMIN_LOGIN` | Não | `admin` | Login do administrador inicial |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500,http://127.0.0.1:5500` | Origens permitidas (separadas por vírgula) |
| `VIACEP_URL` | Não | `https://viacep.com.br/ws` | URL base do ViaCEP |

> Nenhuma senha ou segredo fica no código. **Não** versione arquivos `.env`.

## Banco de dados

O banco `autoescola3espg` é criado automaticamente (`createDatabaseIfNotExist=true`) e as tabelas são criadas pelo **Flyway** (`src/main/resources/db/migration`, V1 a V8). Para criar manualmente: `CREATE DATABASE autoescola3espg;`

## Como executar no IntelliJ IDEA

1. Extraia o ZIP e abra a pasta `AutoEscola3ESPG` (`File > Open` e selecione o `pom.xml` → *Open as Project*).
2. `File > Project Structure > Project`: SDK = JDK 21 (ou 17+). Aguarde o Maven importar as dependências (ou clique em *Reload All Maven Projects*).
3. Abra `Run > Edit Configurations…`, selecione/crie a configuração **Spring Boot** (classe `AutoEscola3EspgApplication`) e em **Environment variables** informe, por exemplo:
   `JWT_SECRET=uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres;DB_PASSWORD=sua_senha_mysql;ADMIN_PASSWORD=uma_senha_forte`
4. Execute. A API sobe em **http://localhost:8085**.

Pelo terminal (Windows PowerShell):
```powershell
$env:JWT_SECRET="uma-chave-longa-e-aleatoria-com-mais-de-32-caracteres"
$env:DB_PASSWORD="sua_senha_mysql"
$env:ADMIN_PASSWORD="uma_senha_forte"
.\mvnw.cmd spring-boot:run
```
Linux/macOS: `export ...` e `./mvnw spring-boot:run`.

## Swagger / OpenAPI

- Swagger UI: **http://localhost:8085/swagger-ui.html** (redireciona para `/swagger-ui/index.html`)
- JSON OpenAPI: http://localhost:8085/v3/api-docs · YAML: http://localhost:8085/v3/api-docs.yaml

Para testar rotas protegidas: execute `POST /login`, copie o valor de `tokenJWT`, clique em **Authorize** e cole o token (sem o prefixo `Bearer`).

## Integração com API externa (ViaCEP)

`GET /integracoes/cep/{cep}` (exige JWT) consulta `https://viacep.com.br/ws/{cep}/json/` por meio de `CepService` (Spring `RestClient`, timeouts de 3 s de conexão e 5 s de leitura) e devolve o endereço em JSON.

| Situação | HTTP |
|---|---|
| CEP encontrado | `200` com `cep`, `logradouro`, `complemento`, `bairro`, `localidade`, `uf` |
| CEP com formato inválido (aceita `01001000` ou `01001-000`) | `400` |
| CEP inexistente (ViaCEP responde `erro`) | `404` |
| ViaCEP indisponível, erro 4xx/5xx, timeout ou falha de rede | `502` |

Os testes simulam todas essas respostas com `MockRestServiceServer` (sem acessar a rede).

## CORS

Definido em `SecurityConfig.corsConfigurationSource` (única fonte de CORS, integrada ao Spring Security): métodos `GET, POST, PUT, DELETE, OPTIONS, HEAD`; headers `Authorization, Content-Type, Accept, Origin`; expõe `Location`; `allowCredentials=false` (a autenticação usa header Bearer, não cookies); preflight respondido antes da autenticação. As origens vêm de `CORS_ALLOWED_ORIGINS`: em produção informe somente o domínio real do front-end.

## Testes automatizados

```bash
./mvnw clean test        # Windows: .\mvnw.cmd clean test
./mvnw clean package     # gera o JAR (executa os testes)
```
Os testes usam o perfil `test` (H2 em memória, JWT fictício): **não precisam de MySQL nem de variáveis de ambiente**. No IntelliJ: botão direito em `src/test/java` → *Run 'All Tests'*.

| Camada | Classes |
|---|---|
| Entidades (Aluno, Instrutor, Usuário, Instrução) | `EntidadesTest` |
| Validação de dados (Bean Validation) | `ValidacaoDadosTest` |
| Regras de negócio do agendamento | `ValidadoresAgendamentoTest` |
| Serviços com Mockito (CRUD, agendamento, cancelamento, erros) | `AlunoServiceTest`, `InstrutorServiceTest`, `UsuarioServiceTest`, `InstrucaoServiceTest` |
| Repositórios (JPA/H2) | `AlunoRepositoryTest`, `InstrutorRepositoryTest`, `InstrucaoRepositoryTest` |
| API externa simulada | `CepServiceTest`, `CepControllerTest` |
| Controllers com serviço simulado | `InstrucaoControllerTest` |
| Integração ponta a ponta (CRUDs, agendar/cancelar) | `CrudIntegracaoTest` |
| JWT, perfis, CORS e Swagger | `SegurancaCorsSwaggerTest` |
| Infra | `AdminInicializadorETratadorTest`, `AutoEscola3EspgApplicationTests` |

## Endpoints

Autenticação: `Authorization: Bearer <token>`. **ADM** = perfil ADMIN; **AUT** = qualquer usuário autenticado.

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/login` | Gera o token JWT | público |
| POST | `/alunos` | Cadastra aluno | ADM |
| GET | `/alunos` | Lista alunos ativos (paginado) | ADM, USER |
| GET | `/alunos/{id}` | Detalha aluno | ADM |
| PUT | `/alunos` | Atualiza aluno | ADM |
| DELETE | `/alunos/{id}` | Inativa aluno (exclusão lógica) | ADM |
| POST | `/instrutores` | Cadastra instrutor | ADM |
| GET | `/instrutores` | Lista instrutores ativos (paginado) | ADM, USER |
| GET | `/instrutores/{id}` | Detalha instrutor | ADM |
| PUT | `/instrutores` | Atualiza instrutor | ADM |
| DELETE | `/instrutores/{id}` | Inativa instrutor (exclusão lógica) | ADM |
| POST | `/usuarios` | Cadastra usuário (senha em BCrypt) | ADM |
| GET | `/usuarios` | Lista usuários (paginado) | ADM |
| GET | `/usuarios/{id}` | Detalha usuário | ADM |
| PUT | `/usuarios` | Atualiza login/senha/perfil | ADM |
| DELETE | `/usuarios/{id}` | Exclui usuário | ADM |
| POST | `/instrucoes` | Agenda instrução | AUT |
| GET | `/instrucoes` | Lista instruções (paginado) | AUT |
| GET | `/instrucoes/{id}` | Detalha instrução | AUT |
| DELETE | `/instrucoes/{id}` | Cancela instrução | AUT |
| GET | `/integracoes/cep/{cep}` | Consulta CEP no ViaCEP | AUT |
| GET | `/health-check` | Verificação de integridade | AUT |
| GET | `/swagger-ui.html`, `/v3/api-docs` | Documentação | público |

Exemplo de agendamento (`data_hora` no formato `dd/MM/yyyy - HH:mm`; se `id_instrutor` for omitido, informe `especialidade`: `MOTOS`, `CARROS`, `VANS` ou `CAMINHOES`):
```json
{ "id_aluno": 1, "id_instrutor": 2, "data_hora": "19/10/2026 - 10:00" }
```

### Regras de negócio do agendamento (já existentes)
Horas inteiras · funcionamento de segunda a sábado, das 06:00 às 20:00 (último início) · antecedência mínima de 30 min · uma instrução por aluno por dia · instrutor sem conflito de horário · aluno e instrutor ativos · escolha automática de instrutor disponível por especialidade.

### Limitação conhecida
O documento anexo de regras de negócio dos checkpoints anteriores não foi disponibilizado. O **cancelamento** remove a instrução (liberando o horário); regras adicionais de cancelamento (por exemplo, antecedência mínima ou motivo), se existirem, **não** foram implementadas.

## Publicação no GitHub
Repositório público: **https://github.com/3ESPG/cp5-SOA**

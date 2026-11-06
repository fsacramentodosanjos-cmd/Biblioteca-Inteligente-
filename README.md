# Biblioteca Inteligente

Sistema de gestão de biblioteca em Spring Boot: cadastro de livros e usuários, empréstimos com regras automáticas e uma interface web, além da API REST.

## Como rodar

1. Suba o MySQL (opcional, se você já tem um): `docker compose up -d`
2. Rode a aplicação: `./mvnw spring-boot:run`
3. Abra http://localhost:8080

Para ver a interface com exemplos, rode com dados de demonstração (só inserem se o banco estiver vazio):

```bash
BIBLIOTECA_DADOS_DEMO=true ./mvnw spring-boot:run
```

### Configuração do banco

Por padrão a aplicação usa o MySQL do `docker-compose.yml`. Para outro banco, defina variáveis de ambiente:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/biblioteca?...` |
| `DB_USER` | `biblioteca` |
| `DB_PASSWORD` | `biblioteca` |

Os valores padrão servem só para desenvolvimento. Em produção, defina senhas próprias.

## Regras de empréstimo

Ajustáveis em `application.properties` (prefixo `biblioteca.`):

- prazo padrão de 14 dias (`prazo-padrao-dias`);
- até 3 livros por usuário ao mesmo tempo (`limite-emprestimos`);
- até 2 renovações, cada uma soma o prazo padrão (`max-renovacoes`);
- multa de R$ 0,50 por dia de atraso (`multa-por-dia`);
- quem tem devolução em atraso não pega outro livro;
- livros e usuários com histórico de empréstimos não podem ser excluídos;
- o status "Emprestado" só muda pelo fluxo de empréstimo e devolução.

## Interface web

| Página | O que faz |
|---|---|
| `/` | Resumo do dia, devoluções em atraso e últimos empréstimos |
| `/livros` | Busca por título, autor ou ISBN; cadastro, edição e exclusão |
| `/usuarios` | Busca por nome ou e-mail; cadastro, edição e exclusão |
| `/emprestimos` | Filtros (todos, em aberto, atrasados); devolver e renovar |
| `/emprestimos/novo` | Registrar empréstimo |

Há tema claro e escuro (botão no menu).

## API REST

Erros seguem o formato `ProblemDetail`: 400 (dados inválidos, com os campos em `erros`), 404 (não encontrado) e 409 (regra de negócio).

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/livros?busca=` | Lista ou busca livros |
| GET / PUT / DELETE | `/api/livros/{id}` | Consulta, atualiza, exclui |
| POST | `/api/livros` | Cadastra (sempre nasce disponível) |
| GET | `/api/usuarios?busca=` | Lista ou busca usuários |
| GET | `/api/usuarios/email/{email}` | Busca por e-mail |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Consulta, atualiza, exclui |
| POST | `/api/usuarios` | Cadastra |
| GET | `/api/emprestimos`, `/abertos`, `/atrasados`, `/usuario/{id}` | Consultas |
| POST | `/api/emprestimos` | Registra: `{"usuarioId":1,"livroId":2,"dataDevolucaoPrevista":"2026-11-01"}` (data opcional) |
| PUT | `/api/emprestimos/{id}/devolver` | Registra a devolução |
| PUT | `/api/emprestimos/{id}/renovar` | Renova o prazo |

## Testes

```bash
./mvnw test
```

Os testes usam H2 em memória (perfil `test`), sem precisar do MySQL. As regras de empréstimo e de livros têm testes unitários com Mockito.

## Estrutura

```
controller/   API REST (+ ApiExceptionHandler)
web/          Controllers da interface Thymeleaf
service/      Regras de negócio
repository/   Spring Data JPA
dto/          Objetos de entrada e saída dos empréstimos
model/        Entidades
config/       Propriedades das regras, relógio e carga de demonstração
```

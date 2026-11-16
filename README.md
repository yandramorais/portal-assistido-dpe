# Portal do Assistido — DPE-GO

Sistema em que o cidadão pede atendimento à Defensoria Pública do Estado de Goiás e acompanha o pedido, e em que a equipe faz a triagem num quadro kanban, com devolutiva no portal.

[![CI](https://github.com/yandramorais/portal-assistido-dpe/actions/workflows/ci.yml/badge.svg)](https://github.com/yandramorais/portal-assistido-dpe/actions/workflows/ci.yml)

> Projeto de demonstração desenvolvido para processo seletivo. Não é um sistema oficial da Defensoria Pública do Estado de Goiás; todos os dados são fictícios.

![Quadro de triagem da equipe](docs/img/kanban.png)

## Sumário

- [Telas](#telas)
- [Como rodar](#como-rodar)
- [Como recarregar os dados](#como-recarregar-os-dados)
- [Arquitetura](#arquitetura)
- [Regras de negócio](#regras-de-negócio)
- [Tecnologias](#tecnologias)
- [Como rodar os testes](#como-rodar-os-testes)
- [Suporte N2](docs/SUPORTE-N2.md)
- [Autora](#autora)

## Telas

### Assistido

![Tela de login](docs/img/login.png)
Entrada com e-mail e senha; quem não tem conta segue para o cadastro.

<table>
  <tr>
    <td><img src="docs/img/cadastro1.png" width="100%" alt="Cadastro, passo 1: dados pessoais"></td>
    <td><img src="docs/img/cadastro2.png" width="100%" alt="Cadastro, passo 2: endereço e contato"></td>
    <td><img src="docs/img/cadastro3.png" width="100%" alt="Cadastro, passo 3: renda e situação"></td>
  </tr>
  <tr>
    <td>Passo 1: dados pessoais, com validação de CPF.</td>
    <td>Passo 2: contato, endereço e acesso.</td>
    <td>Passo 3: renda, família e situações de vulnerabilidade.</td>
  </tr>
</table>

![Novo pedido](docs/img/novo_pedido.png)
Novo pedido: área, unidade, assunto, relato, modalidade e checklist de documentos.

![Pedido enviado](docs/img/pedido_enviado.png)
Confirmação com o número de protocolo e os próximos passos.

![Meus pedidos](docs/img/meus_pedidos.png)
Lista dos pedidos do assistido com a situação atual de cada um.

![Detalhe do pedido](docs/img/detalhe_pedido.png)
Detalhe do pedido: linha do tempo, decisão da equipe e conversa.

### Equipe da Defensoria

![Quadro de triagem](docs/img/kanban.png)
Quadro de triagem com filtros por área, unidade e prioridade; prioridade alta tem faixa verde à esquerda.

![Mover card](docs/img/mover_card.png)
Transição de status com os campos exigidos pelo destino, como motivo e devolutiva ao assistido.

![Detalhe no quadro](docs/img/detalhe_kanban.png)
Painel lateral com dados do pedido, histórico de status e nota interna da equipe.

## Como rodar

Só é preciso ter Docker com Docker Compose.

```bash
git clone https://github.com/yandramorais/portal-assistido-dpe.git
cd portal-assistido-dpe
cp .env.example .env
docker compose up --build
```

O back-end sobe com o perfil `dev`, que aplica o schema e carrega os dados fictícios de `docs/seed.sql`.

| Serviço | Endereço |
| --- | --- |
| Portal | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

Todos os usuários de teste usam a senha `Senha@123`.

| Usuário | Perfil | O que mostra |
| --- | --- | --- |
| servidor@dpe.local | Servidor | Quadro de triagem completo, transições e notas internas. |
| defensor@dpe.local | Defensor | As mesmas telas da equipe; é o responsável por parte dos pedidos do seed. |
| maria@exemplo.com | Assistida | Pessoa idosa (prioridade alta) com um pedido aguardando triagem e outro deferido com data e local de atendimento. |
| carlos@exemplo.com | Assistido | Renda acima do critério: pedido indeferido com motivo e outro aguardando triagem. |
| rafael@exemplo.com | Assistido | Uso do nome social em todo o portal e um pedido em análise. |

## Como recarregar os dados

O seed limpa as tabelas e popula tudo de novo, em uma única transação. Usuário e banco são os de `DB_USER` e `DB_NAME` no `.env` (`portal` e `portal` no `.env.example`):

```bash
docker compose exec -T postgres psql -U portal -d portal -1 -v ON_ERROR_STOP=1 -f - < docs/seed.sql
```

## Arquitetura

```mermaid
flowchart LR
    navegador[Navegador] -->|http :5173| web["web<br/>nginx + React"]
    web -->|/api| api["api<br/>Spring Boot :8080"]
    api -->|JDBC| postgres[("postgres<br/>PostgreSQL 16")]
```

- `frontend/`: aplicação React organizada por funcionalidade. Em produção é servida pelo nginx, que repassa `/api` ao back-end.
- `backend/`: API Spring Boot organizada por domínio em `auth`, `assistido`, `solicitacao`, `workflow`, `notificacao`, `unidade` e `shared`.

Fluxo de status do pedido:

```mermaid
stateDiagram-v2
    [*] --> AGUARDANDO_TRIAGEM: assistido envia o pedido
    AGUARDANDO_TRIAGEM --> EM_ANALISE
    EM_ANALISE --> PENDENTE_DOCUMENTACAO: exige devolutiva
    EM_ANALISE --> DEFERIDO: exige data e local
    EM_ANALISE --> ENCAMINHADO: exige destino
    EM_ANALISE --> INDEFERIDO: exige motivo
    PENDENTE_DOCUMENTACAO --> EM_ANALISE
    PENDENTE_DOCUMENTACAO --> INDEFERIDO: exige motivo
    INDEFERIDO --> EM_ANALISE: revisão
    DEFERIDO --> CONCLUIDO
    ENCAMINHADO --> [*]
    CONCLUIDO --> [*]
```

Qualquer transição fora desse mapa é recusada pela API. Cada mudança grava o histórico.

## Regras de negócio

- **Critério de renda.** Renda individual de até 3 salários mínimos gera hipossuficiência presumida; acima disso, o pedido é marcado como "requer análise". O salário mínimo e o número de salários são configuráveis (`dpe.salario-minimo` e `dpe.limite-salarios`). Não há recusa automática: quem decide é a equipe, que pode considerar despesas, dependentes e vulnerabilidades.
- **Prioridade.** O pedido é de prioridade alta quando o assistido tem 60 anos ou mais ou declarou alguma situação de vulnerabilidade, como deficiência ou violência doméstica. No quadro, esses pedidos aparecem com faixa verde à esquerda.
- **Documentos.** O assistido marca quais documentos já tem e pode enviar o pedido sem todos eles. A equipe vê quantos faltam no card e pode mover o pedido para "Pendente de documentação" com uma devolutiva explicando o que falta.
- **Indeferimento e revisão.** Indeferir exige um motivo, que o assistido vê no detalhe do pedido. Se quiser recorrer, ele escreve na conversa do pedido, e a equipe pode devolvê-lo para "Em análise".

## Tecnologias

- **Back-end:** Java 21, Spring Boot 3, Spring Web, Bean Validation, Spring Data JPA, Spring Security com JWT, Liquibase, Lombok, springdoc-openapi e Actuator.
- **Front-end:** React 18, TypeScript, Vite, Tailwind CSS, React Router, TanStack Query, React Hook Form com Zod, dnd-kit e date-fns.
- **Banco de dados:** PostgreSQL 16.
- **Infraestrutura:** Docker Compose e nginx.
- **Testes e CI:** JUnit, Testcontainers, Vitest, Testing Library e GitHub Actions.

## Como rodar os testes

Back-end (testes unitários e de integração, com PostgreSQL via Testcontainers; precisa de Docker):

```bash
cd backend
./mvnw verify
```

Front-end:

```bash
cd frontend
npm ci
npm test
```

O CI no GitHub Actions roda os dois a cada push na `main` e em todo pull request.

## Autora

Yandra de Morais — [github.com/yandramorais](https://github.com/yandramorais)

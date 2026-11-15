# Roteiro de suporte N2: "Não recebi o e-mail"

Roteiro para investigar quando um assistido diz que não recebeu o aviso de mudança no pedido. Os exemplos usam os dados fictícios de `docs/seed.sql`, que já incluem um envio com falha.

## O caso

Francisco de Assis Souza liga para a Central de Informações: o pedido dele sobre uma geladeira com defeito mudou de situação, mas ele diz que não chegou nenhum e-mail.

## 1. Achar o envio no log de e-mails

Entre no portal como `servidor@dpe.local` (senha `Senha@123`) e abra **Notificações** (`/admin/notificacoes`). Em **Status do envio**, escolha **Falha**.

O mesmo dado pela API:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"servidor@dpe.local","senha":"Senha@123"}' | jq -r .token)

curl -s "http://localhost:8080/api/admin/notificacoes?status=FALHA" \
  -H "Authorization: Bearer $TOKEN" | jq '.content[] | {protocolo, destinatario, status, erro, criadoEm}'
```

Resposta esperada com o seed:

```json
{
  "protocolo": "DPE-2026-000004",
  "destinatario": "francisco@exemplo.com",
  "status": "FALHA",
  "erro": "MailSendException: 550 5.1.1 mailbox unavailable",
  "criadoEm": "..."
}
```

Anote o **protocolo**, o **horário** (`criadoEm`) e o **erro**. O ano do protocolo é o ano em que o seed foi carregado.

## 2. Procurar o protocolo nos logs do back-end

```bash
docker compose logs backend | grep DPE-2026-000004
```

Cada envio gera uma linha do `EnvioEmailListener`. Uma falha aparece assim:

```text
2026-11-15T14:02:11.418Z  WARN [3f9c1a7e20b4] [usuario=1] [task-1] b.g.g.d.p.n.s.EnvioEmailListener : E-mail de atualização: protocolo=DPE-2026-000004 status=FALHA erro=MailSendException
```

- O código entre colchetes é o `requestId` da mudança de status que gerou o e-mail. Com ele, `docker compose logs backend | grep 3f9c1a7e20b4` mostra a requisição inteira, incluindo a linha `Transição de status: protocolo=... de=... para=... usuarioId=...` com quem fez a mudança.
- O log traz só a classe do erro e nunca o endereço de e-mail, por causa da LGPD. O detalhe técnico fica na coluna `erro` do log de e-mails.

Os envios do seed foram gravados direto no banco, então para eles o `grep` não acha nada; as linhas só existem para mudanças feitas pelo portal. Para ver uma falha real no log, pare o servidor de e-mail, mova um card no quadro e suba o servidor de novo:

```bash
docker compose stop mailpit
docker compose start mailpit
```

Entre os dois comandos, mova qualquer card no quadro. O envio fica com status **Falha** e erro de conexão, e a linha `status=FALHA` aparece no log com o protocolo do card.

## 3. Identificar a causa

| Erro registrado | O que significa | O que fazer |
| --- | --- | --- |
| `550 5.1.1 mailbox unavailable` | O servidor de e-mail do destinatário diz que a caixa não existe. Em geral, o e-mail foi digitado errado no cadastro ou a conta foi desativada. | Confirmar o e-mail com o assistido e corrigir o cadastro. |
| `Mail server connection failed` ou `Couldn't connect to host` | O portal não conseguiu falar com o servidor de e-mail. O problema é nosso, não do assistido. | Verificar o servidor SMTP (`docker compose ps mailpit`) e escalar para a infraestrutura. |
| Status **Enviado**, mas o assistido não viu | O e-mail saiu do portal. | Pedir que confira a caixa de spam e o endereço cadastrado. |

No caso do Francisco, o erro é `550 5.1.1 mailbox unavailable`: o endereço `francisco@exemplo.com` não existe no provedor. A mudança de status foi gravada normalmente; só o aviso não chegou.

Para conferir a situação atual do pedido, procure o protocolo no campo de busca do quadro de triagem.

## 4. Resposta ao assistido

> Seu Francisco, o seu pedido DPE-2026-000004 foi atualizado normalmente e está registrado no sistema. O e-mail de aviso não chegou porque o endereço que está no seu cadastro, francisco@exemplo.com, não existe mais ou foi digitado errado. Para ver a situação do pedido agora, entre no Portal do Assistido com seu e-mail e senha e abra "Meus pedidos". Se o senhor tiver outro e-mail, me passe que eu peço a correção do cadastro para os próximos avisos chegarem.

## 5. Registro do atendimento

Registre no chamado: protocolo, horário do envio com falha, erro do log de e-mails, `requestId` da mudança (se houver linha no log) e a orientação dada ao assistido.

--liquibase formatted sql
--changeset portal:002-seed context:dev runOnChange:true
-- =====================================================================
-- seed.sql — dados fictícios para avaliação do Portal do Assistido
-- Todos os nomes, CPFs (válidos, mas gerados) e e-mails são fictícios.
-- Senha de todos os usuários: Senha@123
-- Pode ser rodado várias vezes: limpa as tabelas antes de popular.
-- Liquibase: incluído no changelog com context "dev".
-- Manual:    psql -1 -v ON_ERROR_STOP=1 -f seed.sql   (-1 = uma transação só)
-- =====================================================================

TRUNCATE notificacao_email, mensagem, historico_status, solicitacao_documento,
         solicitacao, assistido_vulnerabilidade, assistido, unidade, usuario
         RESTART IDENTITY CASCADE;
ALTER SEQUENCE protocolo_seq RESTART WITH 1;

-- ---------------------------------------------------------------------
-- Unidades
-- ---------------------------------------------------------------------
INSERT INTO unidade (nome, comarca, endereco, telefone) VALUES
 ('Sede Administrativa e Atendimento', 'Goiânia', 'Alameda Cel. Joaquim de Bastos, 282, Setor Marista', '(62) 3602-1224'),
 ('Unidade Aparecida de Goiânia',      'Aparecida de Goiânia', NULL, NULL),
 ('Unidade Anápolis',                  'Anápolis',  NULL, NULL),
 ('Unidade Trindade',                  'Trindade',  NULL, NULL),
 ('Unidade Inhumas',                   'Inhumas',   NULL, NULL),
 ('Unidade Luziânia',                  'Luziânia',  NULL, NULL);

-- ---------------------------------------------------------------------
-- Equipe (acessa o kanban)
-- ---------------------------------------------------------------------
INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES
 ('Carla Mendes (Servidora)',  'servidor@dpe.local', crypt('Senha@123', gen_salt('bf', 10)), 'SERVIDOR'),
 ('Dr. Renato Alves',          'defensor@dpe.local', crypt('Senha@123', gen_salt('bf', 10)), 'DEFENSOR'),
 ('Dra. Patrícia Nogueira',    'defensora@dpe.local', crypt('Senha@123', gen_salt('bf', 10)), 'DEFENSOR');

-- ---------------------------------------------------------------------
-- Assistidos (cada um com login próprio)
-- ---------------------------------------------------------------------
WITH dados (nome, nome_social, cpf, nasc, tel, email, bairro, cidade, renda, renda_fam, membros, imovel, veiculo) AS (
  VALUES
   ('Maria Aparecida da Silva', NULL, '52601815906', DATE '1958-03-12', '62991230001', 'maria@exemplo.com',   'Jardim América',  'Goiânia',              1621.00, 2900.00, 3, FALSE, FALSE),
   ('João Batista Ferreira',    NULL, '08301661305', DATE '1987-07-22', '62991230002', 'joao@exemplo.com',    'Setor Pedro Ludovico', 'Goiânia',          2400.00, 2400.00, 4, FALSE, TRUE),
   ('Ana Clara Rodrigues',      NULL, '18609139034', DATE '1995-11-02', '62991230003', 'ana@exemplo.com',     'Vila Brasília',   'Aparecida de Goiânia', 1800.00, 1800.00, 2, FALSE, FALSE),
   ('Francisco de Assis Souza', NULL, '99603082430', DATE '1950-01-30', '62991230004', 'francisco@exemplo.com','Centro',         'Inhumas',              1621.00, 1621.00, 1, TRUE,  FALSE),
   ('Juliana Pereira Lima',     NULL, '62819482112', DATE '1992-05-17', '62991230005', 'juliana@exemplo.com', 'Jundiaí',         'Anápolis',             0.00,    1200.00, 3, FALSE, FALSE),
   ('Carlos Eduardo Martins',   NULL, '99351819019', DATE '1979-09-09', '62991230006', 'carlos@exemplo.com',  'Setor Leste',     'Trindade',             6200.00, 9800.00, 4, TRUE,  TRUE),
   ('Rafaela Costa',            'Rafael Costa', '93786579741', DATE '1999-12-01', '62991230007', 'rafael@exemplo.com', 'Setor Campinas', 'Goiânia',    2100.00, 2100.00, 1, FALSE, FALSE),
   ('Sebastiana Gomes',         NULL, '54323194897', DATE '1946-06-25', '62991230008', 'sebastiana@exemplo.com','Parque Estrela Dalva','Luziânia',      1621.00, 3242.00, 2, TRUE,  FALSE),
   ('Pedro Henrique Almeida',   NULL, '75749118606', DATE '2001-02-14', '62991230009', 'pedro@exemplo.com',   'Setor Norte Ferroviário','Goiânia',      0.00,    0.00,    1, FALSE, FALSE),
   ('Luciana Barbosa Teixeira', NULL, '25276018987', DATE '1984-08-30', '62991230010', 'luciana@exemplo.com', 'Jardim Nova Era',  'Aparecida de Goiânia', 3200.00, 4100.00, 3, FALSE, TRUE)
), usuarios AS (
  INSERT INTO usuario (nome, email, senha_hash, perfil)
  SELECT COALESCE(nome_social, nome), email, crypt('Senha@123', gen_salt('bf', 10)), 'ASSISTIDO' FROM dados
  RETURNING id, email
)
INSERT INTO assistido (usuario_id, nome, nome_social, cpf, data_nascimento, telefone, email,
                       cep, logradouro, numero, bairro, cidade, uf,
                       renda_individual, renda_familiar, membros_familia, possui_imovel, possui_veiculo)
SELECT u.id, d.nome, d.nome_social, d.cpf, d.nasc, d.tel, d.email,
       '74000000', 'Rua Fictícia', (100 + row_number() OVER ())::text, d.bairro, d.cidade, 'GO',
       d.renda, d.renda_fam, d.membros, d.imovel, d.veiculo
FROM dados d JOIN usuarios u ON u.email = d.email;

INSERT INTO assistido_vulnerabilidade (assistido_id, vulnerabilidade)
SELECT a.id, v.vul
FROM (VALUES ('62819482112','VIOLENCIA_DOMESTICA'),
             ('75749118606','SITUACAO_DE_RUA'),
             ('99603082430','DOENCA_GRAVE'),
             ('25276018987','PESSOA_COM_DEFICIENCIA')) AS v(cpf, vul)
JOIN assistido a ON a.cpf = v.cpf;

-- ---------------------------------------------------------------------
-- Solicitações espalhadas por todas as colunas do kanban
-- (datas relativas a now() para o quadro sempre parecer "vivo")
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS seed_solicitacao;
CREATE TEMP TABLE seed_solicitacao (
  cpf text, comarca text, area text, assunto text, relato text, modalidade text,
  status text, dias_atras int, responsavel text, agendamento_em_dias int,
  local_atendimento text, destino text, motivo text
);

INSERT INTO seed_solicitacao VALUES
 -- Aguardando triagem
 ('52601815906','Goiânia','FAMILIA','Pensão alimentícia','O pai dos meus netos, que estão sob minha guarda, não paga pensão há 8 meses.','PRESENCIAL','AGUARDANDO_TRIAGEM',0,NULL,NULL,NULL,NULL,NULL),
 ('08301661305','Goiânia','CONSUMIDOR','Negativação indevida','Meu nome foi incluído no SPC por uma dívida de telefonia que já quitei. Tenho o comprovante.','VIRTUAL','AGUARDANDO_TRIAGEM',1,NULL,NULL,NULL,NULL,NULL),
 ('75749118606','Goiânia','REGISTRO_CIVIL','Segunda via de certidão de nascimento','Perdi todos os documentos e preciso da certidão para tirar RG e CPF.','PRESENCIAL','AGUARDANDO_TRIAGEM',2,NULL,NULL,NULL,NULL,NULL),
 ('99351819019','Trindade','CIVEL','Usucapião','Moro no mesmo lote há 15 anos sem escritura e quero regularizar.','PRESENCIAL','AGUARDANDO_TRIAGEM',3,NULL,NULL,NULL,NULL,NULL),
 ('18609139034','Aparecida de Goiânia','INFANCIA_JUVENTUDE','Vaga em creche','Solicitei vaga em CMEI para minha filha de 2 anos há 6 meses e não fui chamada.','VIRTUAL','AGUARDANDO_TRIAGEM',4,NULL,NULL,NULL,NULL,NULL),
 -- Em análise
 ('99603082430','Inhumas','SAUDE','Fornecimento de medicamento','O posto de saúde negou o medicamento de uso contínuo receitado pelo cardiologista.','PRESENCIAL','EM_ANALISE',2,'defensor@dpe.local',NULL,NULL,NULL,NULL),
 ('62819482112','Anápolis','DEFESA_MULHER','Medida protetiva','Sofro ameaças do ex-companheiro e preciso de orientação sobre medida protetiva.','VIRTUAL','EM_ANALISE',1,'defensora@dpe.local',NULL,NULL,NULL,NULL),
 ('93786579741','Goiânia','REGISTRO_CIVIL','Alteração de nome e gênero','Quero alterar nome e gênero na certidão de nascimento.','VIRTUAL','EM_ANALISE',5,'servidor@dpe.local',NULL,NULL,NULL,NULL),
 ('25276018987','Aparecida de Goiânia','IDOSO_PCD','Benefício de Prestação Continuada','O INSS negou meu BPC mesmo com laudo de deficiência.','PRESENCIAL','EM_ANALISE',6,'defensor@dpe.local',NULL,NULL,NULL,NULL),
 -- Pendente de documentos
 ('08301661305','Goiânia','FAMILIA','Guarda compartilhada','Quero regularizar a guarda compartilhada do meu filho.','PRESENCIAL','PENDENTE_DOCUMENTACAO',7,'servidor@dpe.local',NULL,NULL,NULL,NULL),
 ('54323194897','Luziânia','MORADIA','Ação de despejo','Recebi notificação de despejo do imóvel onde moro de aluguel.','PRESENCIAL','PENDENTE_DOCUMENTACAO',9,'defensora@dpe.local',NULL,NULL,NULL,NULL),
 ('18609139034','Aparecida de Goiânia','CONSUMIDOR','Cobrança abusiva','A conta de energia veio quatro vezes maior que a média sem explicação.','VIRTUAL','PENDENTE_DOCUMENTACAO',10,'servidor@dpe.local',NULL,NULL,NULL,NULL),
 -- Deferido (agendado)
 ('52601815906','Goiânia','FAMILIA','Curatela','Meu irmão tem Alzheimer avançado e preciso ser curadora para cuidar dos interesses dele.','PRESENCIAL','DEFERIDO',12,'defensor@dpe.local',3,'Sede Goiânia — Alameda Cel. Joaquim de Bastos, 282, sala 4',NULL,NULL),
 ('62819482112','Anápolis','FAMILIA','Divórcio litigioso','Quero me divorciar e dividir os bens.','VIRTUAL','DEFERIDO',8,'defensora@dpe.local',5,'Atendimento virtual — link enviado por mensagem',NULL,NULL),
 ('75749118606','Goiânia','REGISTRO_CIVIL','Registro tardio','Nunca tive registro de nascimento.','PRESENCIAL','DEFERIDO',15,'servidor@dpe.local',1,'Sede Goiânia — Núcleo de Registro Civil',NULL,NULL),
 -- Encaminhado
 ('08301661305','Goiânia','CIVEL','Benefício previdenciário (INSS)','Tive o auxílio-doença cortado pelo INSS.','VIRTUAL','ENCAMINHADO',14,'defensor@dpe.local',NULL,NULL,'Defensoria Pública da União (DPU) — matéria federal',NULL),
 ('99603082430','Inhumas','CONSUMIDOR','Produto com defeito','Geladeira comprada há 2 meses parou de funcionar e a loja não troca.','PRESENCIAL','ENCAMINHADO',18,'servidor@dpe.local',NULL,NULL,'Procon Goiás — tentativa de solução administrativa',NULL),
 -- Indeferido
 ('99351819019','Trindade','CIVEL','Revisão de contrato de financiamento','Quero revisar os juros do financiamento da minha caminhonete.','PRESENCIAL','INDEFERIDO',20,'defensora@dpe.local',NULL,NULL,NULL,'Renda individual e patrimônio acima dos critérios de hipossuficiência, sem situação de vulnerabilidade que justifique flexibilização.'),
 -- Concluído
 ('54323194897','Luziânia','IDOSO_PCD','Atendimento prioritário em banco','O banco se recusou a me atender no caixa prioritário.','PRESENCIAL','CONCLUIDO',30,'defensor@dpe.local',-20,'Unidade Luziânia',NULL,NULL),
 ('25276018987','Aparecida de Goiânia','SAUDE','Cirurgia ortopédica','Aguardo cirurgia no joelho pela regulação há mais de um ano.','PRESENCIAL','CONCLUIDO',45,'defensora@dpe.local',-30,'Unidade Aparecida de Goiânia',NULL,NULL);

INSERT INTO solicitacao (protocolo, assistido_id, unidade_id, responsavel_id, area, assunto, relato, modalidade,
                         status, prioridade, hipossuficiencia, data_agendamento, local_atendimento,
                         encaminhamento_destino, motivo_indeferimento, criado_em, atualizado_em)
SELECT
  'DPE-' || to_char(now(), 'YYYY') || '-' || lpad(nextval('protocolo_seq')::text, 6, '0'),
  a.id, un.id, resp.id, s.area, s.assunto, s.relato, s.modalidade, s.status,
  CASE WHEN age(a.data_nascimento) >= interval '60 years'
         OR EXISTS (SELECT 1 FROM assistido_vulnerabilidade v WHERE v.assistido_id = a.id)
       THEN 'ALTA' ELSE 'NORMAL' END,
  CASE WHEN a.renda_individual <= 3 * 1621.00 THEN 'PRESUMIDA' ELSE 'REQUER_ANALISE' END,
  CASE WHEN s.agendamento_em_dias IS NOT NULL
       THEN date_trunc('day', now()) + make_interval(days => s.agendamento_em_dias) + interval '9 hours' END,
  s.local_atendimento, s.destino, s.motivo,
  now() - make_interval(days => s.dias_atras) - interval '2 hours',
  now() - make_interval(days => GREATEST(s.dias_atras - 1, 0))
FROM seed_solicitacao s
JOIN assistido a     ON a.cpf = s.cpf
JOIN unidade un      ON un.comarca = s.comarca
LEFT JOIN usuario resp ON resp.email = s.responsavel
ORDER BY s.dias_atras DESC;

-- ---------------------------------------------------------------------
-- Checklist de documentos
-- ---------------------------------------------------------------------
INSERT INTO solicitacao_documento (solicitacao_id, tipo, entregue)
SELECT s.id, t.tipo,
       CASE
         WHEN s.status = 'AGUARDANDO_TRIAGEM' THEN t.tipo IN ('DOCUMENTO_IDENTIFICACAO','COMPROVANTE_RESIDENCIA')
         WHEN s.status = 'PENDENTE_DOCUMENTACAO' THEN t.tipo NOT IN ('COMPROVANTE_RENDA','DOCUMENTOS_DO_CASO')
         ELSE TRUE
       END
FROM solicitacao s
CROSS JOIN (VALUES ('DOCUMENTO_IDENTIFICACAO'),('COMPROVANTE_RESIDENCIA'),('COMPROVANTE_RENDA'),
                   ('CERTIDAO_CIVIL'),('DOCUMENTOS_DO_CASO')) AS t(tipo);

-- ---------------------------------------------------------------------
-- Histórico: abertura + caminho até o status atual
-- ---------------------------------------------------------------------
INSERT INTO historico_status (solicitacao_id, status_anterior, status_novo, observacao, autor_id, criado_em)
SELECT s.id, NULL, 'AGUARDANDO_TRIAGEM', 'Solicitação registrada pelo assistido.', a.usuario_id, s.criado_em
FROM solicitacao s JOIN assistido a ON a.id = s.assistido_id;

INSERT INTO historico_status (solicitacao_id, status_anterior, status_novo, observacao, autor_id, criado_em)
SELECT s.id, 'AGUARDANDO_TRIAGEM', 'EM_ANALISE', 'Triagem iniciada.', s.responsavel_id, s.criado_em + interval '1 day'
FROM solicitacao s WHERE s.status <> 'AGUARDANDO_TRIAGEM';

INSERT INTO historico_status (solicitacao_id, status_anterior, status_novo, observacao, autor_id, criado_em)
SELECT s.id, 'EM_ANALISE', s.status,
       CASE s.status
         WHEN 'PENDENTE_DOCUMENTACAO' THEN 'Solicitados comprovante de renda e documentos do caso.'
         WHEN 'DEFERIDO'    THEN 'Atendimento deferido e agendado.'
         WHEN 'ENCAMINHADO' THEN 'Encaminhado para: ' || s.encaminhamento_destino
         WHEN 'INDEFERIDO'  THEN s.motivo_indeferimento
         WHEN 'CONCLUIDO'   THEN 'Atendimento deferido e agendado.'
       END,
       s.responsavel_id, s.criado_em + interval '2 days'
FROM solicitacao s WHERE s.status NOT IN ('AGUARDANDO_TRIAGEM','EM_ANALISE');

INSERT INTO historico_status (solicitacao_id, status_anterior, status_novo, observacao, autor_id, criado_em)
SELECT s.id, 'DEFERIDO', 'CONCLUIDO', 'Atendimento realizado. Ação ajuizada.', s.responsavel_id, s.data_agendamento + interval '2 hours'
FROM solicitacao s WHERE s.status = 'CONCLUIDO';

-- Corrige o status intermediário dos concluídos (passaram por DEFERIDO)
UPDATE historico_status h SET status_novo = 'DEFERIDO'
FROM solicitacao s
WHERE h.solicitacao_id = s.id AND s.status = 'CONCLUIDO'
  AND h.status_anterior = 'EM_ANALISE';

-- ---------------------------------------------------------------------
-- Devolutivas (mensagens visíveis ao assistido) e uma resposta dele
-- ---------------------------------------------------------------------
INSERT INTO mensagem (solicitacao_id, autor_id, conteudo, visivel_assistido, criado_em)
SELECT s.id, s.responsavel_id,
       CASE s.status
         WHEN 'PENDENTE_DOCUMENTACAO' THEN 'Olá! Para seguir com seu pedido, envie o último contracheque (ou carteira de trabalho) e os documentos relacionados ao caso. Se não tiver algum, responda esta mensagem que orientamos você.'
         WHEN 'DEFERIDO'    THEN 'Seu atendimento foi aprovado e agendado. Confira data e local no seu pedido. Leve documento com foto.'
         WHEN 'ENCAMINHADO' THEN 'Seu caso não é de atribuição da Defensoria Estadual e foi encaminhado. Veja o destino e as orientações no seu pedido.'
         WHEN 'INDEFERIDO'  THEN 'Após análise, o atendimento não pôde ser deferido. O motivo está no seu pedido. Você pode pedir a revisão da decisão respondendo esta mensagem.'
         WHEN 'CONCLUIDO'   THEN 'Seu atendimento foi concluído. Obrigado por procurar a Defensoria Pública.'
       END,
       TRUE, s.criado_em + interval '2 days'
FROM solicitacao s WHERE s.status NOT IN ('AGUARDANDO_TRIAGEM','EM_ANALISE');

INSERT INTO mensagem (solicitacao_id, autor_id, conteudo, visivel_assistido, criado_em)
SELECT s.id, s.responsavel_id, 'Nota interna: verificar se já existe processo de alimentos em andamento.', FALSE, s.criado_em + interval '1 day'
FROM solicitacao s WHERE s.status = 'PENDENTE_DOCUMENTACAO' AND s.area = 'FAMILIA';

INSERT INTO mensagem (solicitacao_id, autor_id, conteudo, visivel_assistido, criado_em)
SELECT s.id, a.usuario_id, 'Obrigada! Preciso separar algum documento para a reunião online?', TRUE, s.criado_em + interval '3 days'
FROM solicitacao s JOIN assistido a ON a.id = s.assistido_id
WHERE s.status = 'DEFERIDO' AND s.modalidade = 'VIRTUAL';

-- ---------------------------------------------------------------------
-- Log de e-mails (inclui uma FALHA para demonstrar investigação N2)
-- ---------------------------------------------------------------------
INSERT INTO notificacao_email (solicitacao_id, destinatario, assunto, status, erro, criado_em, enviado_em)
SELECT s.id, a.email,
       'Atualização do seu pedido ' || s.protocolo,
       CASE WHEN s.status = 'ENCAMINHADO' AND s.area = 'CONSUMIDOR' THEN 'FALHA' ELSE 'ENVIADO' END,
       CASE WHEN s.status = 'ENCAMINHADO' AND s.area = 'CONSUMIDOR'
            THEN 'MailSendException: 550 5.1.1 mailbox unavailable' END,
       s.criado_em + interval '2 days',
       CASE WHEN NOT (s.status = 'ENCAMINHADO' AND s.area = 'CONSUMIDOR')
            THEN s.criado_em + interval '2 days' + interval '3 seconds' END
FROM solicitacao s JOIN assistido a ON a.id = s.assistido_id
WHERE s.status NOT IN ('AGUARDANDO_TRIAGEM','EM_ANALISE');


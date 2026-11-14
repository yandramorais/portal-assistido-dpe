--liquibase formatted sql

--changeset portal:001-schema
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuario (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL CHECK (perfil IN ('ASSISTIDO','SERVIDOR','DEFENSOR')),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE unidade (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    comarca   VARCHAR(80)  NOT NULL,
    endereco  VARCHAR(200),
    telefone  VARCHAR(20)
);

CREATE TABLE assistido (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id        BIGINT UNIQUE REFERENCES usuario(id),
    nome              VARCHAR(150) NOT NULL,
    nome_social       VARCHAR(150),
    cpf               CHAR(11)     NOT NULL UNIQUE,
    data_nascimento   DATE         NOT NULL,
    telefone          VARCHAR(20)  NOT NULL,
    email             VARCHAR(150),
    cep               CHAR(8),
    logradouro        VARCHAR(150),
    numero            VARCHAR(10),
    complemento       VARCHAR(80),
    bairro            VARCHAR(80),
    cidade            VARCHAR(80)  NOT NULL,
    uf                CHAR(2)      NOT NULL DEFAULT 'GO',
    renda_individual  NUMERIC(12,2) NOT NULL,
    renda_familiar    NUMERIC(12,2),
    membros_familia   SMALLINT     NOT NULL DEFAULT 1,
    possui_imovel     BOOLEAN      NOT NULL DEFAULT FALSE,
    possui_veiculo    BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE assistido_vulnerabilidade (
    assistido_id    BIGINT      NOT NULL REFERENCES assistido(id) ON DELETE CASCADE,
    vulnerabilidade VARCHAR(40) NOT NULL CHECK (vulnerabilidade IN
        ('PESSOA_COM_DEFICIENCIA','VIOLENCIA_DOMESTICA','DOENCA_GRAVE','SITUACAO_DE_RUA')),
    PRIMARY KEY (assistido_id, vulnerabilidade)
);

CREATE TABLE solicitacao (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    protocolo               VARCHAR(20)  NOT NULL UNIQUE,
    assistido_id            BIGINT       NOT NULL REFERENCES assistido(id),
    unidade_id              BIGINT       NOT NULL REFERENCES unidade(id),
    responsavel_id          BIGINT       REFERENCES usuario(id),
    area                    VARCHAR(40)  NOT NULL,
    assunto                 VARCHAR(150) NOT NULL,
    relato                  TEXT         NOT NULL,
    modalidade              VARCHAR(20)  NOT NULL CHECK (modalidade IN ('PRESENCIAL','VIRTUAL')),
    status                  VARCHAR(30)  NOT NULL DEFAULT 'AGUARDANDO_TRIAGEM' CHECK (status IN
        ('AGUARDANDO_TRIAGEM','EM_ANALISE','PENDENTE_DOCUMENTACAO','DEFERIDO','ENCAMINHADO','INDEFERIDO','CONCLUIDO')),
    prioridade              VARCHAR(10)  NOT NULL DEFAULT 'NORMAL' CHECK (prioridade IN ('NORMAL','ALTA')),
    hipossuficiencia        VARCHAR(20)  NOT NULL CHECK (hipossuficiencia IN ('PRESUMIDA','REQUER_ANALISE')),
    data_agendamento        TIMESTAMPTZ,
    local_atendimento       VARCHAR(200),
    encaminhamento_destino  VARCHAR(150),
    motivo_indeferimento    TEXT,
    versao                  INTEGER      NOT NULL DEFAULT 0,
    criado_em               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em           TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_solicitacao_status_prioridade_criado ON solicitacao (status, prioridade, criado_em);
CREATE INDEX idx_solicitacao_assistido ON solicitacao (assistido_id);
CREATE INDEX idx_solicitacao_area ON solicitacao (area);
CREATE INDEX idx_solicitacao_responsavel ON solicitacao (responsavel_id);

CREATE TABLE solicitacao_documento (
    solicitacao_id BIGINT      NOT NULL REFERENCES solicitacao(id) ON DELETE CASCADE,
    tipo           VARCHAR(40) NOT NULL CHECK (tipo IN
        ('DOCUMENTO_IDENTIFICACAO','COMPROVANTE_RESIDENCIA','COMPROVANTE_RENDA','CERTIDAO_CIVIL','DOCUMENTOS_DO_CASO')),
    entregue       BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (solicitacao_id, tipo)
);

CREATE TABLE historico_status (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitacao_id  BIGINT      NOT NULL REFERENCES solicitacao(id) ON DELETE CASCADE,
    status_anterior VARCHAR(30),
    status_novo     VARCHAR(30) NOT NULL,
    observacao      TEXT,
    autor_id        BIGINT      REFERENCES usuario(id),
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_historico_solicitacao ON historico_status (solicitacao_id, criado_em);

CREATE TABLE mensagem (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitacao_id    BIGINT      NOT NULL REFERENCES solicitacao(id) ON DELETE CASCADE,
    autor_id          BIGINT      NOT NULL REFERENCES usuario(id),
    conteudo          TEXT        NOT NULL,
    visivel_assistido BOOLEAN     NOT NULL DEFAULT TRUE,
    lida_em           TIMESTAMPTZ,
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mensagem_solicitacao ON mensagem (solicitacao_id, criado_em);

CREATE TABLE notificacao_email (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitacao_id  BIGINT       NOT NULL REFERENCES solicitacao(id) ON DELETE CASCADE,
    destinatario    VARCHAR(150) NOT NULL,
    assunto         VARCHAR(200) NOT NULL,
    status          VARCHAR(10)  NOT NULL CHECK (status IN ('PENDENTE','ENVIADO','FALHA')),
    erro            TEXT,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    enviado_em      TIMESTAMPTZ
);
CREATE INDEX idx_notificacao_status ON notificacao_email (status);

CREATE SEQUENCE protocolo_seq START 1;
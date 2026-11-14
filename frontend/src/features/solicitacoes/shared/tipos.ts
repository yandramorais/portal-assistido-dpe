/** Tipos que espelham os DTOs do backend (pacotes solicitacao, unidade e assistido). */

export type AreaAtuacao =
  | 'FAMILIA'
  | 'CIVEL'
  | 'CONSUMIDOR'
  | 'SAUDE'
  | 'MORADIA'
  | 'REGISTRO_CIVIL'
  | 'INFANCIA_JUVENTUDE'
  | 'DEFESA_MULHER'
  | 'IDOSO_PCD';

export type StatusSolicitacao =
  | 'AGUARDANDO_TRIAGEM'
  | 'EM_ANALISE'
  | 'PENDENTE_DOCUMENTACAO'
  | 'DEFERIDO'
  | 'ENCAMINHADO'
  | 'INDEFERIDO'
  | 'CONCLUIDO';

export type Modalidade = 'PRESENCIAL' | 'VIRTUAL';
export type Prioridade = 'NORMAL' | 'ALTA';
export type Hipossuficiencia = 'PRESUMIDA' | 'REQUER_ANALISE';

export type TipoDocumento =
  | 'DOCUMENTO_IDENTIFICACAO'
  | 'COMPROVANTE_RESIDENCIA'
  | 'COMPROVANTE_RENDA'
  | 'CERTIDAO_CIVIL'
  | 'DOCUMENTOS_DO_CASO';

export type Vulnerabilidade =
  'PESSOA_COM_DEFICIENCIA' | 'VIOLENCIA_DOMESTICA' | 'DOENCA_GRAVE' | 'SITUACAO_DE_RUA';

export interface Unidade {
  id: number;
  nome: string;
  comarca: string;
  endereco: string | null;
  telefone: string | null;
}

/** Página do Spring Data (modo VIA_DTO). */
export interface Pagina<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

export interface SolicitacaoResumo {
  protocolo: string;
  assunto: string;
  area: AreaAtuacao;
  areaDescricao: string;
  status: StatusSolicitacao;
  unidade: string;
  dataAgendamento: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface DocumentoItem {
  tipo: TipoDocumento;
  entregue: boolean;
}

export interface HistoricoItem {
  statusAnterior: StatusSolicitacao | null;
  statusNovo: StatusSolicitacao;
  observacao: string | null;
  criadoEm: string;
}

export interface MensagemItem {
  id: number;
  autor: string;
  daEquipe: boolean;
  conteudo: string;
  criadoEm: string;
  lidaEm: string | null;
}

export interface SolicitacaoDetalhe {
  protocolo: string;
  area: AreaAtuacao;
  areaDescricao: string;
  assunto: string;
  relato: string;
  modalidade: Modalidade;
  status: StatusSolicitacao;
  prioridade: Prioridade;
  hipossuficiencia: Hipossuficiencia;
  unidade: Unidade;
  dataAgendamento: string | null;
  localAtendimento: string | null;
  encaminhamentoDestino: string | null;
  motivoIndeferimento: string | null;
  documentos: DocumentoItem[];
  historico: HistoricoItem[];
  mensagens: MensagemItem[];
  criadoEm: string;
  atualizadoEm: string;
}

export interface NovaSolicitacaoRequest {
  unidadeId: number;
  area: AreaAtuacao;
  assunto: string;
  relato: string;
  modalidade: Modalidade;
  documentosEntregues: TipoDocumento[];
}

export interface MensagemResponse {
  id: number;
  autor: string;
  daEquipe: boolean;
  visivelAssistido: boolean;
  conteudo: string;
  criadoEm: string;
  statusSolicitacao: StatusSolicitacao;
}

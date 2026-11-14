import type {
  AreaAtuacao,
  Hipossuficiencia,
  Modalidade,
  Prioridade,
  StatusSolicitacao,
  TipoDocumento,
  Unidade,
  Vulnerabilidade,
} from '../../solicitacoes/shared/tipos';

export interface AssistidoResumo {
  nome: string;
  nomeSocial: string | null;
  cpf: string;
  dataNascimento: string;
  telefone: string;
  email: string | null;
  cidade: string;
  rendaIndividual: number;
  rendaFamiliar: number | null;
  membrosFamilia: number;
  possuiImovel: boolean;
  possuiVeiculo: boolean;
  vulnerabilidades: Vulnerabilidade[];
}

export interface HistoricoEquipe {
  statusAnterior: StatusSolicitacao | null;
  statusNovo: StatusSolicitacao;
  observacao: string | null;
  autor: string | null;
  criadoEm: string;
}

export interface MensagemEquipe {
  id: number;
  autor: string;
  daEquipe: boolean;
  visivelAssistido: boolean;
  conteudo: string;
  criadoEm: string;
  lidaEm: string | null;
}

export interface SolicitacaoAdmin {
  protocolo: string;
  versao: number;
  status: StatusSolicitacao;
  statusDescricao: string;
  proximosStatus: StatusSolicitacao[];
  area: AreaAtuacao;
  areaDescricao: string;
  assunto: string;
  relato: string;
  modalidade: Modalidade;
  prioridade: Prioridade;
  hipossuficiencia: Hipossuficiencia;
  assistido: AssistidoResumo;
  unidade: Unidade;
  responsavel: string | null;
  dataAgendamento: string | null;
  localAtendimento: string | null;
  encaminhamentoDestino: string | null;
  motivoIndeferimento: string | null;
  documentos: Array<{ tipo: TipoDocumento; entregue: boolean }>;
  historico: HistoricoEquipe[];
  mensagens: MensagemEquipe[];
  criadoEm: string;
  atualizadoEm: string;
}

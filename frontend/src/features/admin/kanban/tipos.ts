import type {
  AreaAtuacao,
  Hipossuficiencia,
  Prioridade,
  StatusSolicitacao,
  TipoDocumento,
} from '../../solicitacoes/shared/tipos';

export interface CardKanban {
  protocolo: string;
  nome: string;
  cpf: string | null;
  area: AreaAtuacao;
  areaDescricao: string;
  unidade: string;
  diasEmAberto: number;
  prioridade: Prioridade;
  hipossuficiencia: Hipossuficiencia;
  documentosPendentes: TipoDocumento[];
  responsavel: string | null;
  versao: number;
}

export interface ColunaKanban {
  status: StatusSolicitacao;
  descricao: string;
  total: number;
  cards: CardKanban[];
}

export interface KanbanResponse {
  colunas: ColunaKanban[];
}

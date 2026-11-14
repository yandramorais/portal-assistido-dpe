import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';

export interface AlvoDeTransicao {
  protocolo: string;
  nome: string;
  versao: number;
  status: StatusSolicitacao;
  unidade?: string;
}

export interface CamposDeTransicao {
  dataAgendamento?: string;
  localAtendimento?: string;
  destino?: string;
  motivo?: string;
  devolutiva?: string;
}

export interface PedidoDeTransicao {
  alvo: AlvoDeTransicao;
  destino: StatusSolicitacao;
  campos: CamposDeTransicao;
}

export interface TransicaoEscolhida {
  alvo: AlvoDeTransicao;
  destino: StatusSolicitacao;
}

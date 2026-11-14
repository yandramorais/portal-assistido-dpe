import type { StatusSolicitacao } from '../features/solicitacoes/shared/tipos';

export type CampoDeTransicao =
  'dataAgendamento' | 'localAtendimento' | 'destino' | 'motivo' | 'devolutiva';

export const ORDEM_DOS_STATUS: readonly StatusSolicitacao[] = [
  'AGUARDANDO_TRIAGEM',
  'EM_ANALISE',
  'PENDENTE_DOCUMENTACAO',
  'DEFERIDO',
  'ENCAMINHADO',
  'INDEFERIDO',
  'CONCLUIDO',
];

const TRANSICOES: Record<StatusSolicitacao, readonly StatusSolicitacao[]> = {
  AGUARDANDO_TRIAGEM: ['EM_ANALISE'],
  EM_ANALISE: ['PENDENTE_DOCUMENTACAO', 'DEFERIDO', 'ENCAMINHADO', 'INDEFERIDO'],
  PENDENTE_DOCUMENTACAO: ['EM_ANALISE', 'INDEFERIDO'],
  DEFERIDO: ['CONCLUIDO'],
  ENCAMINHADO: [],
  INDEFERIDO: ['EM_ANALISE'],
  CONCLUIDO: [],
};

export const CAMPOS_EXIGIDOS: Record<StatusSolicitacao, readonly CampoDeTransicao[]> = {
  AGUARDANDO_TRIAGEM: [],
  EM_ANALISE: [],
  PENDENTE_DOCUMENTACAO: ['devolutiva'],
  DEFERIDO: ['dataAgendamento', 'localAtendimento'],
  ENCAMINHADO: ['destino'],
  INDEFERIDO: ['motivo'],
  CONCLUIDO: [],
};

export function destinosPossiveis(origem: StatusSolicitacao): readonly StatusSolicitacao[] {
  return TRANSICOES[origem];
}

export function podeIrPara(origem: StatusSolicitacao, destino: StatusSolicitacao): boolean {
  return TRANSICOES[origem].includes(destino);
}

export function ehFinal(status: StatusSolicitacao): boolean {
  return TRANSICOES[status].length === 0;
}

export function campoExigido(destino: StatusSolicitacao, campo: CampoDeTransicao): boolean {
  return CAMPOS_EXIGIDOS[destino].includes(campo);
}

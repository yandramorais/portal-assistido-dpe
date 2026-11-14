import { describe, expect, it } from 'vitest';

import type { StatusSolicitacao } from '../features/solicitacoes/shared/tipos';
import {
  CAMPOS_EXIGIDOS,
  campoExigido,
  destinosPossiveis,
  ehFinal,
  ORDEM_DOS_STATUS,
  podeIrPara,
} from './transicoes';

const TRANSICOES_VALIDAS = new Set([
  'AGUARDANDO_TRIAGEM -> EM_ANALISE',
  'EM_ANALISE -> PENDENTE_DOCUMENTACAO',
  'EM_ANALISE -> DEFERIDO',
  'EM_ANALISE -> ENCAMINHADO',
  'EM_ANALISE -> INDEFERIDO',
  'PENDENTE_DOCUMENTACAO -> EM_ANALISE',
  'PENDENTE_DOCUMENTACAO -> INDEFERIDO',
  'DEFERIDO -> CONCLUIDO',
  'INDEFERIDO -> EM_ANALISE',
]);

const TODOS: StatusSolicitacao[] = [
  'AGUARDANDO_TRIAGEM',
  'EM_ANALISE',
  'PENDENTE_DOCUMENTACAO',
  'DEFERIDO',
  'ENCAMINHADO',
  'INDEFERIDO',
  'CONCLUIDO',
];

const PARES = TODOS.flatMap((origem) =>
  TODOS.map((destino) => ({
    origem,
    destino,
    permitido: TRANSICOES_VALIDAS.has(`${origem} -> ${destino}`),
  })),
);

describe('mapa de transições (igual ao StatusSolicitacao do back-end)', () => {
  it('cobre os 7 status na ordem do fluxo', () => {
    expect(ORDEM_DOS_STATUS).toEqual(TODOS);
    expect(PARES).toHaveLength(49);
  });

  it.each(PARES)('$origem -> $destino: $permitido', ({ origem, destino, permitido }) => {
    expect(podeIrPara(origem, destino)).toBe(permitido);
  });

  it.each(TODOS)('destinosPossiveis(%s) bate com a tabela', (origem) => {
    const esperados = TODOS.filter((destino) => TRANSICOES_VALIDAS.has(`${origem} -> ${destino}`));
    expect([...destinosPossiveis(origem)].sort()).toEqual(esperados.sort());
  });

  it('só ENCAMINHADO e CONCLUIDO são finais', () => {
    expect(TODOS.filter(ehFinal)).toEqual(['ENCAMINHADO', 'CONCLUIDO']);
  });
});

describe('campos exigidos por destino', () => {
  it('seguem as regras do back-end', () => {
    expect(CAMPOS_EXIGIDOS).toEqual({
      AGUARDANDO_TRIAGEM: [],
      EM_ANALISE: [],
      PENDENTE_DOCUMENTACAO: ['devolutiva'],
      DEFERIDO: ['dataAgendamento', 'localAtendimento'],
      ENCAMINHADO: ['destino'],
      INDEFERIDO: ['motivo'],
      CONCLUIDO: [],
    });
  });

  it('campoExigido responde por destino e campo', () => {
    expect(campoExigido('DEFERIDO', 'localAtendimento')).toBe(true);
    expect(campoExigido('DEFERIDO', 'motivo')).toBe(false);
    expect(campoExigido('EM_ANALISE', 'devolutiva')).toBe(false);
    expect(campoExigido('PENDENTE_DOCUMENTACAO', 'devolutiva')).toBe(true);
  });
});

import { useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';

import type { AreaAtuacao, Prioridade } from '../../solicitacoes/shared/tipos';

export interface FiltrosKanban {
  area: AreaAtuacao | '';
  unidadeId: string;
  prioridade: Prioridade | '';
  busca: string;
}

const CHAVES: ReadonlyArray<keyof FiltrosKanban> = ['area', 'unidadeId', 'prioridade', 'busca'];

export function paraQueryString(filtros: FiltrosKanban): string {
  const parametros = new URLSearchParams();
  CHAVES.forEach((chave) => {
    const valor = filtros[chave].trim();
    if (valor) parametros.set(chave, valor);
  });
  const texto = parametros.toString();
  return texto ? `?${texto}` : '';
}

export function useFiltrosKanban() {
  const [parametros, setParametros] = useSearchParams();

  const filtros = useMemo<FiltrosKanban>(
    () => ({
      area: (parametros.get('area') ?? '') as AreaAtuacao | '',
      unidadeId: parametros.get('unidadeId') ?? '',
      prioridade: (parametros.get('prioridade') ?? '') as Prioridade | '',
      busca: parametros.get('busca') ?? '',
    }),
    [parametros],
  );

  const definir = useCallback(
    (parcial: Partial<FiltrosKanban>) => {
      setParametros(
        (atuais) => {
          const novos = new URLSearchParams(atuais);
          Object.entries(parcial).forEach(([chave, valor]) => {
            if (valor && valor.trim()) novos.set(chave, valor.trim());
            else novos.delete(chave);
          });
          return novos;
        },
        { replace: true },
      );
    },
    [setParametros],
  );

  const limpar = useCallback(() => setParametros({}, { replace: true }), [setParametros]);

  const temFiltro = CHAVES.some((chave) => filtros[chave] !== '');

  return { filtros, definir, limpar, temFiltro };
}

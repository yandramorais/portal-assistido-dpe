import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';

import { Button, Field, Input, Select } from '../../../components/ui';
import { api } from '../../../lib/api';
import { AREAS } from '../../solicitacoes/shared/rotulos';
import type { AreaAtuacao, Prioridade, Unidade } from '../../solicitacoes/shared/tipos';
import { useFiltrosKanban } from './useFiltrosKanban';

const ESPERA_DA_BUSCA_MS = 300;

export function FiltrosKanban() {
  const { filtros, definir, limpar, temFiltro } = useFiltrosKanban();
  const [busca, setBusca] = useState(filtros.busca);

  const unidades = useQuery({
    queryKey: ['unidades'],
    queryFn: () => api.get<Unidade[]>('/unidades'),
    staleTime: 10 * 60_000,
  });

  useEffect(() => {
    if (busca.trim() === filtros.busca) return;
    const temporizador = window.setTimeout(() => definir({ busca }), ESPERA_DA_BUSCA_MS);
    return () => window.clearTimeout(temporizador);
  }, [busca, filtros.busca, definir]);

  const limparTudo = () => {
    setBusca('');
    limpar();
  };

  return (
    <form
      role="search"
      aria-label="Filtros do quadro"
      onSubmit={(evento) => {
        evento.preventDefault();
        definir({ busca });
      }}
      className="mb-4 grid gap-3 rounded-md border border-neutral-200 bg-white p-3 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_1fr_auto] lg:items-end"
    >
      <Field label="Buscar por nome, CPF ou protocolo">
        <Input
          type="search"
          value={busca}
          onChange={(evento) => setBusca(evento.target.value)}
          placeholder="Ex.: Maria, 526.018.159-06 ou DPE-2026-000001"
        />
      </Field>

      <Field label="Área">
        <Select
          value={filtros.area}
          onChange={(evento) => definir({ area: evento.target.value as AreaAtuacao | '' })}
        >
          <option value="">Todas</option>
          {AREAS.map((a) => (
            <option key={a.valor} value={a.valor}>
              {a.rotulo}
            </option>
          ))}
        </Select>
      </Field>

      <Field label="Unidade">
        <Select
          value={filtros.unidadeId}
          onChange={(evento) => definir({ unidadeId: evento.target.value })}
          disabled={!unidades.isSuccess}
        >
          <option value="">{unidades.isError ? 'Não foi possível carregar' : 'Todas'}</option>
          {unidades.data?.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome}
            </option>
          ))}
        </Select>
      </Field>

      <Field label="Prioridade">
        <Select
          value={filtros.prioridade}
          onChange={(evento) => definir({ prioridade: evento.target.value as Prioridade | '' })}
        >
          <option value="">Todas</option>
          <option value="ALTA">Alta</option>
          <option value="NORMAL">Normal</option>
        </Select>
      </Field>

      <div className="flex items-end">
        {temFiltro && (
          <Button variante="secundaria" onClick={limparTudo}>
            Limpar filtros
          </Button>
        )}
      </div>
    </form>
  );
}

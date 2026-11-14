import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useState } from 'react';

import { CabecalhoDePagina } from '../../../components/layout/CabecalhoDePagina';
import { Button, Carregando, EmptyState, EstadoDeErro } from '../../../components/ui';
import { api } from '../../../lib/api';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { PainelDetalhe } from '../detalhe/PainelDetalhe';
import { FiltrosKanban } from '../filtros/FiltrosKanban';
import { paraQueryString, useFiltrosKanban } from '../filtros/useFiltrosKanban';
import { DialogTransicao } from '../transicao/DialogTransicao';
import type { AlvoDeTransicao, TransicaoEscolhida } from '../transicao/tipos';
import { useProtocolosEmTransicao, useTransicao } from '../transicao/useTransicao';
import { QuadroKanban } from './QuadroKanban';
import type { CardKanban, KanbanResponse } from './tipos';

export function TriagemPage() {
  const { filtros, limpar, temFiltro } = useFiltrosKanban();
  const [detalhe, setDetalhe] = useState<string | null>(null);
  const [transicao, setTransicao] = useState<TransicaoEscolhida | null>(null);
  const transicaoMutacao = useTransicao();
  const emTransicao = useProtocolosEmTransicao();

  const quadro = useQuery({
    queryKey: ['admin', 'kanban', filtros],
    queryFn: ({ signal }) =>
      api.get<KanbanResponse>(`/admin/kanban${paraQueryString(filtros)}`, { signal }),
    placeholderData: keepPreviousData,
  });

  const escolherTransicao = (alvo: AlvoDeTransicao, destino: StatusSolicitacao) =>
    setTransicao({ alvo, destino });

  const moverCard = (card: CardKanban, status: StatusSolicitacao, destino: StatusSolicitacao) =>
    escolherTransicao(
      {
        protocolo: card.protocolo,
        nome: card.nome,
        versao: card.versao,
        status,
        unidade: card.unidade,
      },
      destino,
    );

  const totalDePedidos = quadro.data?.colunas.reduce((soma, c) => soma + c.total, 0) ?? 0;

  return (
    <>
      <CabecalhoDePagina
        titulo="Triagem"
        descricao="Arraste o card para a próxima etapa ou use “Mover para…”. Prioridade alta tem faixa verde à esquerda."
        acoes={
          <Button
            variante="secundaria"
            tamanho="sm"
            onClick={() => quadro.refetch()}
            carregando={quadro.isFetching && !quadro.isPending}
          >
            Recarregar quadro
          </Button>
        }
      />

      <FiltrosKanban />

      {quadro.isPending && <Carregando texto="Carregando o quadro…" />}

      {quadro.isError && (
        <EstadoDeErro
          titulo="Não foi possível carregar o quadro."
          erro={quadro.error}
          aoTentarNovamente={() => quadro.refetch()}
        />
      )}

      {quadro.isSuccess && totalDePedidos === 0 && (
        <EmptyState
          titulo={temFiltro ? 'Nenhum pedido com esses filtros' : 'Nenhum pedido no quadro'}
          descricao={
            temFiltro
              ? 'Mude os filtros ou limpe-os para ver todos os pedidos.'
              : 'Quando um assistido fizer um pedido, ele aparece em "Aguardando triagem".'
          }
          acao={
            temFiltro ? (
              <Button variante="secundaria" onClick={limpar}>
                Limpar filtros
              </Button>
            ) : undefined
          }
        />
      )}

      {quadro.isSuccess && totalDePedidos > 0 && (
        <QuadroKanban
          quadro={quadro.data}
          emTransicao={emTransicao}
          aoAbrir={setDetalhe}
          aoMover={moverCard}
        />
      )}

      <PainelDetalhe
        protocolo={detalhe}
        emTransicao={detalhe !== null && emTransicao.includes(detalhe)}
        aoFechar={() => setDetalhe(null)}
        aoMover={escolherTransicao}
      />

      <DialogTransicao
        transicao={transicao}
        aoFechar={() => setTransicao(null)}
        aoConfirmar={(pedido) => {
          setTransicao(null);
          transicaoMutacao.mutate(pedido);
        }}
      />
    </>
  );
}

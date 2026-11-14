import {
  useMutation,
  useMutationState,
  useQueryClient,
  type QueryKey,
} from '@tanstack/react-query';

import { useToast } from '../../../components/ui';
import { api, ApiError, mensagemDoErro } from '../../../lib/api';
import { ROTULO_STATUS } from '../../solicitacoes/shared/rotulos';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import type { SolicitacaoAdmin } from '../detalhe/tipos';
import type { CardKanban, KanbanResponse } from '../kanban/tipos';
import type { PedidoDeTransicao } from './tipos';

const CHAVE_DA_MUTACAO = ['admin', 'transicao'];
export const MENSAGEM_CONFLITO = 'Este pedido foi alterado por outra pessoa. Recarregue o quadro.';

function ordenar(cards: CardKanban[]): CardKanban[] {
  return [...cards].sort(
    (a, b) =>
      Number(b.prioridade === 'ALTA') - Number(a.prioridade === 'ALTA') ||
      b.diasEmAberto - a.diasEmAberto,
  );
}

export function moverCard(
  quadro: KanbanResponse,
  protocolo: string,
  destino: StatusSolicitacao,
): KanbanResponse {
  const card = quadro.colunas.flatMap((c) => c.cards).find((c) => c.protocolo === protocolo);
  if (!card) return quadro;
  return {
    colunas: quadro.colunas.map((coluna) => {
      const semCard = coluna.cards.filter((c) => c.protocolo !== protocolo);
      const cards = coluna.status === destino ? ordenar([...semCard, card]) : semCard;
      return { ...coluna, cards, total: cards.length };
    }),
  };
}

export function useTransicao() {
  const queryClient = useQueryClient();
  const { mostrar } = useToast();

  return useMutation({
    mutationKey: CHAVE_DA_MUTACAO,
    mutationFn: ({ alvo, destino, campos }: PedidoDeTransicao) =>
      api.patch<SolicitacaoAdmin>(`/admin/solicitacoes/${alvo.protocolo}/status`, {
        novoStatus: destino,
        versao: alvo.versao,
        ...campos,
      }),
    onMutate: async ({ alvo, destino }) => {
      await queryClient.cancelQueries({ queryKey: ['admin', 'kanban'] });
      const anteriores = queryClient.getQueriesData<KanbanResponse>({
        queryKey: ['admin', 'kanban'],
      });
      queryClient.setQueriesData<KanbanResponse>({ queryKey: ['admin', 'kanban'] }, (quadro) =>
        quadro ? moverCard(quadro, alvo.protocolo, destino) : quadro,
      );
      return { anteriores };
    },
    onError: (erro, _pedido, contexto) => {
      contexto?.anteriores.forEach(([chave, dados]: [QueryKey, KanbanResponse | undefined]) =>
        queryClient.setQueryData(chave, dados),
      );
      mostrar({
        tipo: 'erro',
        mensagem:
          erro instanceof ApiError && erro.status === 409
            ? MENSAGEM_CONFLITO
            : mensagemDoErro(erro),
      });
    },
    onSuccess: (resposta, { alvo, destino }) => {
      queryClient.setQueryData(['admin', 'solicitacao', alvo.protocolo], resposta);
      mostrar({
        tipo: 'sucesso',
        mensagem: `Pedido ${alvo.protocolo} movido para ${ROTULO_STATUS[destino]}.`,
      });
    },
    onSettled: (_resposta, _erro, { alvo }) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'kanban'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'solicitacao', alvo.protocolo] });
    },
  });
}

export function useProtocolosEmTransicao(): string[] {
  return useMutationState({
    filters: { mutationKey: CHAVE_DA_MUTACAO, status: 'pending' },
    select: (mutacao) => (mutacao.state.variables as PedidoDeTransicao).alvo.protocolo,
  });
}

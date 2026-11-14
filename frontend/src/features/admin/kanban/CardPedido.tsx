import { useDraggable } from '@dnd-kit/core';

import { Badge } from '../../../components/ui';
import { cn } from '../../../lib/cn';
import { destinosPossiveis } from '../../../lib/transicoes';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { MenuMoverPara } from '../transicao/MenuMoverPara';
import type { CardKanban } from './tipos';

export interface DadosDoArraste {
  card: CardKanban;
  status: StatusSolicitacao;
}

function textoDosDias(dias: number): string {
  if (dias <= 0) return 'aberto hoje';
  return dias === 1 ? 'há 1 dia' : `há ${dias} dias`;
}

function textoDosDocumentos(faltam: number): string {
  if (faltam === 0) return 'Documentos completos';
  return faltam === 1 ? 'Falta 1 documento' : `Faltam ${faltam} documentos`;
}

export function ConteudoDoCard({ card }: { card: CardKanban }) {
  const faltam = card.documentosPendentes.length;
  return (
    <>
      <div className="flex items-baseline justify-between gap-2">
        <span className="font-mono text-xs font-medium text-neutral-700">{card.protocolo}</span>
        <span className="text-xs whitespace-nowrap text-neutral-600">
          {textoDosDias(card.diasEmAberto)}
        </span>
      </div>
      <p className="mt-1 text-sm font-semibold text-neutral-900">{card.nome}</p>
      <p className="text-xs text-neutral-700">{card.areaDescricao}</p>
      <div className="mt-2 flex flex-wrap items-center gap-1.5">
        {card.prioridade === 'ALTA' && <Badge tom="primario">Prioridade alta</Badge>}
        {card.hipossuficiencia === 'REQUER_ANALISE' && (
          <Badge tom="contorno">Renda acima do limite</Badge>
        )}
        <span className={cn('text-xs', faltam > 0 ? 'text-neutral-800' : 'text-neutral-500')}>
          {textoDosDocumentos(faltam)}
        </span>
      </div>
    </>
  );
}

interface CardPedidoProps {
  card: CardKanban;
  status: StatusSolicitacao;
  emTransicao: boolean;
  aoAbrir: (protocolo: string) => void;
  aoMover: (card: CardKanban, status: StatusSolicitacao, destino: StatusSolicitacao) => void;
}

export function CardPedido({ card, status, emTransicao, aoAbrir, aoMover }: CardPedidoProps) {
  const { listeners, setNodeRef, isDragging } = useDraggable({
    id: card.protocolo,
    data: { card, status } satisfies DadosDoArraste,
    disabled: emTransicao,
  });

  return (
    <article
      ref={setNodeRef}
      {...listeners}
      onClick={() => aoAbrir(card.protocolo)}
      aria-busy={emTransicao || undefined}
      className={cn(
        'relative cursor-grab rounded-md border border-neutral-200 bg-white py-2.5 pr-3 pl-4 select-none hover:border-neutral-400',
        isDragging && 'opacity-40',
        emTransicao && 'cursor-progress opacity-60',
      )}
    >
      {card.prioridade === 'ALTA' && (
        <span
          aria-hidden="true"
          className="bg-primary-700 absolute inset-y-0 left-0 w-1.5 rounded-l-md"
        />
      )}
      <ConteudoDoCard card={card} />
      <div className="mt-2 flex items-center justify-between gap-2 border-t border-neutral-100 pt-2">
        <button
          type="button"
          onClick={(evento) => {
            evento.stopPropagation();
            aoAbrir(card.protocolo);
          }}
          className="text-primary-700 text-xs font-medium underline-offset-2 hover:underline"
        >
          Ver pedido
          <span className="sr-only">
            {' '}
            {card.protocolo} de {card.nome}
          </span>
        </button>
        <MenuMoverPara
          destinos={destinosPossiveis(status)}
          desabilitado={emTransicao}
          rotuloAcessivel={`Mover o pedido ${card.protocolo} de ${card.nome}`}
          aoEscolher={(destino) => aoMover(card, status, destino)}
        />
      </div>
    </article>
  );
}

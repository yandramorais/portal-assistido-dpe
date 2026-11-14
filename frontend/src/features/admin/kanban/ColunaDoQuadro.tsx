import { useDroppable } from '@dnd-kit/core';
import { useId } from 'react';

import { cn } from '../../../lib/cn';
import { ROTULO_STATUS } from '../../solicitacoes/shared/rotulos';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { CardPedido } from './CardPedido';
import type { CardKanban, ColunaKanban } from './tipos';

export type EstadoDaColuna = 'normal' | 'origem' | 'permitida' | 'bloqueada';

interface ColunaDoQuadroProps {
  coluna: ColunaKanban;
  estado: EstadoDaColuna;
  emTransicao: readonly string[];
  aoAbrir: (protocolo: string) => void;
  aoMover: (card: CardKanban, status: StatusSolicitacao, destino: StatusSolicitacao) => void;
}

export function ColunaDoQuadro({
  coluna,
  estado,
  emTransicao,
  aoAbrir,
  aoMover,
}: ColunaDoQuadroProps) {
  const idTitulo = useId();
  const { setNodeRef, isOver } = useDroppable({
    id: coluna.status,
    disabled: estado === 'bloqueada' || estado === 'origem',
  });

  return (
    <section
      ref={setNodeRef}
      aria-labelledby={idTitulo}
      className={cn(
        'flex max-h-[calc(100vh-17rem)] min-h-48 w-72 shrink-0 flex-col rounded-md border bg-neutral-100 transition-opacity',
        estado === 'normal' && 'border-neutral-200',
        estado === 'origem' && 'border-neutral-300',
        estado === 'permitida' && 'border-primary-600 bg-primary-50 border-2',
        estado === 'permitida' && isOver && 'bg-primary-100',
        estado === 'bloqueada' && 'border-neutral-200 opacity-40',
      )}
    >
      <header className="flex items-center justify-between gap-2 border-b border-neutral-200 px-3 py-2">
        <h2 id={idTitulo} className="text-sm font-semibold text-neutral-900">
          {ROTULO_STATUS[coluna.status]}
        </h2>
        <span
          className="rounded-sm bg-white px-1.5 py-0.5 text-xs font-semibold text-neutral-700"
          aria-label={`${coluna.total} ${coluna.total === 1 ? 'pedido' : 'pedidos'}`}
        >
          {coluna.total}
        </span>
      </header>
      <div className="flex-1 space-y-2 overflow-y-auto p-2">
        {coluna.cards.length === 0 ? (
          <p className="px-1 py-4 text-center text-xs text-neutral-500">Nenhum pedido</p>
        ) : (
          coluna.cards.map((card) => (
            <CardPedido
              key={card.protocolo}
              card={card}
              status={coluna.status}
              emTransicao={emTransicao.includes(card.protocolo)}
              aoAbrir={aoAbrir}
              aoMover={aoMover}
            />
          ))
        )}
      </div>
    </section>
  );
}

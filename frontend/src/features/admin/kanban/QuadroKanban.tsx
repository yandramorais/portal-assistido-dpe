import {
  DndContext,
  DragOverlay,
  MouseSensor,
  TouchSensor,
  useSensor,
  useSensors,
  type Announcements,
  type DragEndEvent,
  type DragStartEvent,
} from '@dnd-kit/core';
import { useState } from 'react';

import { podeIrPara } from '../../../lib/transicoes';
import { ROTULO_STATUS } from '../../solicitacoes/shared/rotulos';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { ConteudoDoCard, type DadosDoArraste } from './CardPedido';
import { ColunaDoQuadro, type EstadoDaColuna } from './ColunaDoQuadro';
import type { CardKanban, KanbanResponse } from './tipos';

interface QuadroKanbanProps {
  quadro: KanbanResponse;
  emTransicao: readonly string[];
  aoAbrir: (protocolo: string) => void;
  aoMover: (card: CardKanban, status: StatusSolicitacao, destino: StatusSolicitacao) => void;
}

function dadosDe(evento: { active: { data: { current?: unknown } } }): DadosDoArraste | undefined {
  return evento.active.data.current as DadosDoArraste | undefined;
}

const anuncios: Announcements = {
  onDragStart: ({ active }) => `Pedido ${active.id} selecionado para mover.`,
  onDragOver: ({ active, over }) =>
    over
      ? `Pedido ${active.id} sobre a coluna ${ROTULO_STATUS[over.id as StatusSolicitacao]}.`
      : `Pedido ${active.id} fora de uma coluna permitida.`,
  onDragEnd: ({ active, over }) =>
    over
      ? `Pedido ${active.id} solto na coluna ${ROTULO_STATUS[over.id as StatusSolicitacao]}. Confirme no diálogo.`
      : `Pedido ${active.id} solto fora de uma coluna permitida. Nada mudou.`,
  onDragCancel: ({ active }) => `Movimento do pedido ${active.id} cancelado.`,
};

export function QuadroKanban({ quadro, emTransicao, aoAbrir, aoMover }: QuadroKanbanProps) {
  const [arrastando, setArrastando] = useState<DadosDoArraste | null>(null);
  const sensores = useSensors(
    useSensor(MouseSensor, { activationConstraint: { distance: 5 } }),
    useSensor(TouchSensor, { activationConstraint: { delay: 250, tolerance: 5 } }),
  );

  const estadoDa = (status: StatusSolicitacao): EstadoDaColuna => {
    if (!arrastando) return 'normal';
    if (status === arrastando.status) return 'origem';
    return podeIrPara(arrastando.status, status) ? 'permitida' : 'bloqueada';
  };

  const aoComecar = (evento: DragStartEvent) => setArrastando(dadosDe(evento) ?? null);

  const aoSoltar = (evento: DragEndEvent) => {
    setArrastando(null);
    const dados = dadosDe(evento);
    const destino = evento.over?.id as StatusSolicitacao | undefined;
    if (!dados || !destino || !podeIrPara(dados.status, destino)) return;
    aoMover(dados.card, dados.status, destino);
  };

  return (
    <DndContext
      sensors={sensores}
      onDragStart={aoComecar}
      onDragEnd={aoSoltar}
      onDragCancel={() => setArrastando(null)}
      accessibility={{
        announcements: anuncios,
        screenReaderInstructions: {
          draggable: 'Para mudar o status pelo teclado, use o botão "Mover para…" do card.',
        },
      }}
    >
      <div
        className="overflow-x-auto pb-3"
        role="region"
        aria-label="Quadro de triagem"
        tabIndex={0}
      >
        <div className="flex gap-3">
          {quadro.colunas.map((coluna) => (
            <ColunaDoQuadro
              key={coluna.status}
              coluna={coluna}
              estado={estadoDa(coluna.status)}
              emTransicao={emTransicao}
              aoAbrir={aoAbrir}
              aoMover={aoMover}
            />
          ))}
        </div>
      </div>
      <DragOverlay dropAnimation={null}>
        {arrastando && (
          <div className="w-68 cursor-grabbing rounded-md border border-neutral-400 bg-white py-2.5 pr-3 pl-4 shadow-sm">
            <ConteudoDoCard card={arrastando.card} />
          </div>
        )}
      </DragOverlay>
    </DndContext>
  );
}

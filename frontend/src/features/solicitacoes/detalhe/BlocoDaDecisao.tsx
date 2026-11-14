import type { ReactNode } from 'react';

import { dataPorExtenso, hora } from '../shared/datas';
import { ROTULO_MODALIDADE } from '../shared/rotulos';
import type { SolicitacaoDetalhe } from '../shared/tipos';

/**
 * Resultado da análise, conforme o status: agendamento (data, hora e local),
 * encaminhamento (destino) ou indeferimento (motivo). Nos demais status não aparece.
 */
export function BlocoDaDecisao({ pedido }: { pedido: SolicitacaoDetalhe }) {
  const agendado =
    (pedido.status === 'DEFERIDO' || pedido.status === 'CONCLUIDO') && pedido.dataAgendamento;

  if (agendado && pedido.dataAgendamento) {
    return (
      <Bloco
        titulo={pedido.status === 'CONCLUIDO' ? 'Atendimento realizado' : 'Atendimento agendado'}
        destaque
      >
        <dl className="grid gap-x-6 gap-y-3 text-sm sm:grid-cols-3">
          <Item rotulo="Data">
            <span className="first-letter:uppercase">{dataPorExtenso(pedido.dataAgendamento)}</span>
          </Item>
          <Item rotulo="Horário">{hora(pedido.dataAgendamento)}</Item>
          <Item rotulo="Modalidade">{ROTULO_MODALIDADE[pedido.modalidade]}</Item>
          <Item rotulo="Local" className="sm:col-span-3">
            {pedido.localAtendimento ?? pedido.unidade.nome}
            {pedido.modalidade === 'PRESENCIAL' && pedido.unidade.endereco && (
              <span className="block text-neutral-600">{pedido.unidade.endereco}</span>
            )}
          </Item>
        </dl>
        {pedido.status === 'DEFERIDO' && (
          <p className="mt-3 text-sm text-neutral-700">
            Leve um documento com foto e os documentos pendentes da lista ao lado. Se não puder
            comparecer, avise pela conversa abaixo.
          </p>
        )}
      </Bloco>
    );
  }

  if (pedido.status === 'ENCAMINHADO' && pedido.encaminhamentoDestino) {
    return (
      <Bloco titulo="Seu caso foi encaminhado">
        <p className="text-sm text-neutral-700">
          Este caso não é atendido pela Defensoria Pública do Estado. Procure:
        </p>
        <p className="mt-1 text-base font-semibold text-neutral-900">
          {pedido.encaminhamentoDestino}
        </p>
      </Bloco>
    );
  }

  if (pedido.status === 'INDEFERIDO' && pedido.motivoIndeferimento) {
    return (
      <Bloco titulo="Pedido não deferido">
        <p className="text-sm font-medium text-neutral-800">Motivo</p>
        <p className="mt-1 text-sm text-neutral-700">{pedido.motivoIndeferimento}</p>
        <p className="mt-3 text-sm text-neutral-700">
          Se quiser pedir a revisão da decisão, escreva na conversa abaixo.
        </p>
      </Bloco>
    );
  }

  return null;
}

function Bloco({
  titulo,
  destaque,
  children,
}: {
  titulo: string;
  destaque?: boolean;
  children: ReactNode;
}) {
  return (
    <section
      aria-label={titulo}
      className={
        'rounded-md border bg-white p-4 ' +
        (destaque ? 'border-primary-300 border-l-4' : 'border-neutral-200')
      }
    >
      <h2 className="mb-3 text-base font-semibold text-neutral-900">{titulo}</h2>
      {children}
    </section>
  );
}

function Item({
  rotulo,
  className,
  children,
}: {
  rotulo: string;
  className?: string;
  children: ReactNode;
}) {
  return (
    <div className={className}>
      <dt className="text-xs text-neutral-600">{rotulo}</dt>
      <dd className="font-medium text-neutral-900">{children}</dd>
    </div>
  );
}

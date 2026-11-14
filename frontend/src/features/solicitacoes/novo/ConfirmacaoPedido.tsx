import { useEffect, useRef } from 'react';

import { useTituloDaPagina } from '../../../components/layout/useTituloDaPagina';
import { LinkBotao } from '../../../components/ui';
import { ROTULO_STATUS } from '../shared/rotulos';
import type { SolicitacaoDetalhe } from '../shared/tipos';

/** Tela mostrada logo após o envio: protocolo em destaque e o que acontece agora. */
export function ConfirmacaoPedido({ pedido }: { pedido: SolicitacaoDetalhe }) {
  useTituloDaPagina('Pedido enviado');
  const titulo = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    titulo.current?.focus();
  }, []);

  return (
    <div className="mx-auto max-w-xl">
      <div className="rounded-md border border-neutral-200 bg-white p-6">
        <h1
          ref={titulo}
          tabIndex={-1}
          className="text-xl font-semibold text-neutral-900 focus:outline-none"
        >
          Pedido enviado
        </h1>
        <p className="mt-1 text-sm text-neutral-600">
          Recebemos seu pedido de atendimento sobre <strong>{pedido.assunto}</strong>.
        </p>

        <div className="border-primary-200 bg-primary-50 mt-5 rounded-md border px-4 py-4">
          <p className="text-primary-800 text-sm">Número do protocolo</p>
          <p className="text-primary-900 mt-1 font-mono text-2xl font-semibold tracking-wide">
            {pedido.protocolo}
          </p>
          <p className="text-primary-800 mt-2 text-xs">
            Anote este número. Ele identifica seu pedido na Central de Informações.
          </p>
        </div>

        <h2 className="mt-6 text-base font-semibold text-neutral-900">Próximo passo</h2>
        <p className="mt-1 text-sm text-neutral-700">
          A equipe da Defensoria vai analisar seu pedido (situação atual:{' '}
          {ROTULO_STATUS[pedido.status].toLowerCase()}). Você recebe um aviso por e-mail a cada
          atualização e pode acompanhar tudo em Meus pedidos. Se faltar algum documento, vamos pedir
          por mensagem neste portal.
        </p>

        <div className="mt-6 flex flex-wrap gap-2">
          <LinkBotao to={`/pedidos/${pedido.protocolo}`}>Acompanhar este pedido</LinkBotao>
          <LinkBotao to="/meus-pedidos" variante="secundaria">
            Ir para Meus pedidos
          </LinkBotao>
        </div>
      </div>
    </div>
  );
}

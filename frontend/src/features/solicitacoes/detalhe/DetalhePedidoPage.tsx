import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';

import { CabecalhoDePagina } from '../../../components/layout/CabecalhoDePagina';
import { Card, Carregando, EmptyState, EstadoDeErro, LinkBotao } from '../../../components/ui';
import { api, ApiError } from '../../../lib/api';
import { dataHora } from '../shared/datas';
import { ROTULO_MODALIDADE } from '../shared/rotulos';
import { StatusBadge } from '../shared/StatusBadge';
import type { SolicitacaoDetalhe } from '../shared/tipos';
import { BlocoDaDecisao } from './BlocoDaDecisao';
import { ChecklistDocumentos } from './ChecklistDocumentos';
import { Conversa, ID_CAMPO_RESPOSTA } from './Conversa';
import { LinhaDoTempo } from './LinhaDoTempo';

export function DetalhePedidoPage() {
  const { protocolo = '' } = useParams();

  const pedido = useQuery({
    queryKey: ['solicitacoes', 'detalhe', protocolo],
    queryFn: ({ signal }) => api.get<SolicitacaoDetalhe>(`/solicitacoes/${protocolo}`, { signal }),
  });

  if (pedido.isPending) {
    return (
      <>
        <CabecalhoDePagina titulo={`Pedido ${protocolo}`} />
        <Carregando texto="Carregando o pedido…" />
      </>
    );
  }

  if (pedido.isError) {
    if (pedido.error instanceof ApiError && pedido.error.status === 404) {
      return (
        <>
          <CabecalhoDePagina titulo="Pedido não encontrado" />
          <EmptyState
            titulo={`Não encontramos o pedido ${protocolo}`}
            descricao="Confira o número do protocolo. Você só consegue ver os pedidos feitos com o seu cadastro."
            acao={<LinkBotao to="/meus-pedidos">Ver meus pedidos</LinkBotao>}
          />
        </>
      );
    }
    return (
      <>
        <CabecalhoDePagina titulo={`Pedido ${protocolo}`} />
        <EstadoDeErro
          titulo="Não foi possível carregar o pedido."
          erro={pedido.error}
          aoTentarNovamente={() => pedido.refetch()}
        />
      </>
    );
  }

  const p = pedido.data;

  return (
    <>
      <CabecalhoDePagina
        titulo={`Pedido ${p.protocolo}`}
        descricao={
          <>
            {p.areaDescricao} · {p.assunto} · aberto em {dataHora(p.criadoEm)}
          </>
        }
        acoes={<StatusBadge status={p.status} />}
      />

      {p.status === 'PENDENTE_DOCUMENTACAO' && (
        <section
          aria-labelledby="aviso-pendencia"
          className="border-danger-200 bg-danger-50 mb-5 rounded-md border px-4 py-3"
        >
          <h2 id="aviso-pendencia" className="text-danger-700 text-sm font-semibold">
            A Defensoria precisa da sua resposta
          </h2>
          <p className="mt-1 text-sm text-neutral-800">
            Faltam documentos ou informações para continuar seu atendimento. Leia a mensagem da
            equipe na conversa e responda. Assim que você responder, o pedido volta para análise.
          </p>
          <a
            href={`#${ID_CAMPO_RESPOSTA}`}
            className="text-danger-700 mt-2 inline-block text-sm font-medium underline underline-offset-2"
          >
            Ir para a resposta
          </a>
        </section>
      )}

      <div className="grid gap-5 lg:grid-cols-3">
        <div className="space-y-5 lg:col-span-2">
          <BlocoDaDecisao pedido={p} />

          <Card titulo="Conversa">
            <Conversa protocolo={p.protocolo} status={p.status} mensagens={p.mensagens} />
          </Card>

          <Card titulo="Seu pedido">
            <dl className="grid gap-x-6 gap-y-3 text-sm sm:grid-cols-2">
              <div>
                <dt className="text-xs text-neutral-600">Unidade</dt>
                <dd className="text-neutral-900">{p.unidade.nome}</dd>
              </div>
              <div>
                <dt className="text-xs text-neutral-600">Atendimento</dt>
                <dd className="text-neutral-900">{ROTULO_MODALIDADE[p.modalidade]}</dd>
              </div>
              <div className="sm:col-span-2">
                <dt className="text-xs text-neutral-600">O que aconteceu</dt>
                <dd className="whitespace-pre-line text-neutral-900">{p.relato}</dd>
              </div>
            </dl>
          </Card>
        </div>

        <div className="space-y-5">
          <Card titulo="Documentos">
            <ChecklistDocumentos documentos={p.documentos} />
          </Card>
          <Card titulo="Andamento">
            <LinhaDoTempo historico={p.historico} />
          </Card>
        </div>
      </div>
    </>
  );
}

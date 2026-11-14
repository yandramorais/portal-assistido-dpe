import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';

import { CabecalhoDePagina } from '../../../components/layout/CabecalhoDePagina';
import { Button, Carregando, EmptyState, EstadoDeErro, LinkBotao } from '../../../components/ui';
import { api } from '../../../lib/api';
import { dataHora, tempoRelativo } from '../shared/datas';
import { StatusBadge } from '../shared/StatusBadge';
import type { Pagina, SolicitacaoResumo } from '../shared/tipos';

const POR_PAGINA = 10;

/** Lista dos pedidos do assistido, mais recentes primeiro. A página atual fica na URL (?pagina=2). */
export function MeusPedidosPage() {
  const [parametros, setParametros] = useSearchParams();
  const pagina = Math.max(1, Number(parametros.get('pagina')) || 1);

  const pedidos = useQuery({
    queryKey: ['solicitacoes', 'minhas', pagina],
    queryFn: ({ signal }) =>
      api.get<Pagina<SolicitacaoResumo>>(
        `/solicitacoes/minhas?page=${pagina - 1}&size=${POR_PAGINA}`,
        { signal },
      ),
    placeholderData: keepPreviousData,
  });

  const irPara = (nova: number) => setParametros(nova > 1 ? { pagina: String(nova) } : {});
  const novoPedido = <LinkBotao to="/pedidos/novo">Fazer um pedido</LinkBotao>;

  return (
    <>
      <CabecalhoDePagina
        titulo="Meus pedidos"
        descricao="Acompanhe a situação dos seus pedidos de atendimento."
        acoes={pedidos.data && pedidos.data.page.totalElements > 0 ? novoPedido : undefined}
      />

      {pedidos.isPending && <Carregando texto="Carregando seus pedidos…" />}

      {pedidos.isError && (
        <EstadoDeErro
          titulo="Não foi possível carregar seus pedidos."
          erro={pedidos.error}
          aoTentarNovamente={() => pedidos.refetch()}
        />
      )}

      {pedidos.isSuccess && pedidos.data.page.totalElements === 0 && (
        <EmptyState
          titulo="Você ainda não fez nenhum pedido"
          descricao="Quando você pedir atendimento à Defensoria, o pedido aparece aqui para você acompanhar."
          acao={novoPedido}
        />
      )}

      {pedidos.isSuccess && pedidos.data.page.totalElements > 0 && (
        <>
          <div className="overflow-x-auto rounded-md border border-neutral-200 bg-white">
            <table className="w-full min-w-[640px] text-left text-sm">
              <caption className="sr-only">
                Seus pedidos, do mais recente para o mais antigo
              </caption>
              <thead className="border-b border-neutral-200 bg-neutral-50 text-xs text-neutral-600">
                <tr>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Protocolo
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Área
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Assunto
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Situação
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Aberto
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {pedidos.data.content.map((p) => (
                  <tr key={p.protocolo} className="hover:bg-neutral-50">
                    <td className="px-4 py-2.5 whitespace-nowrap">
                      <Link
                        to={`/pedidos/${p.protocolo}`}
                        className="text-primary-700 font-mono font-medium underline-offset-2 hover:underline"
                      >
                        {p.protocolo}
                      </Link>
                    </td>
                    <td className="px-4 py-2.5 text-neutral-700">{p.areaDescricao}</td>
                    <td className="px-4 py-2.5 text-neutral-900">{p.assunto}</td>
                    <td className="px-4 py-2.5">
                      <StatusBadge status={p.status} />
                    </td>
                    <td className="px-4 py-2.5 whitespace-nowrap text-neutral-700">
                      <time dateTime={p.criadoEm} title={dataHora(p.criadoEm)}>
                        {tempoRelativo(p.criadoEm)}
                      </time>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pedidos.data.page.totalPages > 1 && (
            <nav
              aria-label="Páginas da lista de pedidos"
              className="mt-3 flex items-center justify-between gap-3 text-sm text-neutral-700"
            >
              <span>
                Página {pagina} de {pedidos.data.page.totalPages} ·{' '}
                {pedidos.data.page.totalElements} pedidos
              </span>
              <div className="flex gap-2">
                <Button
                  variante="secundaria"
                  tamanho="sm"
                  disabled={pagina <= 1 || pedidos.isPlaceholderData}
                  onClick={() => irPara(pagina - 1)}
                >
                  Anterior
                </Button>
                <Button
                  variante="secundaria"
                  tamanho="sm"
                  disabled={pagina >= pedidos.data.page.totalPages || pedidos.isPlaceholderData}
                  onClick={() => irPara(pagina + 1)}
                >
                  Próxima
                </Button>
              </div>
            </nav>
          )}
        </>
      )}
    </>
  );
}

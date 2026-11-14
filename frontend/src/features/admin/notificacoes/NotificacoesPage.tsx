import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Fragment } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

import { CabecalhoDePagina } from '../../../components/layout/CabecalhoDePagina';
import {
  Badge,
  Button,
  Carregando,
  EmptyState,
  EstadoDeErro,
  Field,
  Select,
} from '../../../components/ui';
import { api } from '../../../lib/api';
import { cn } from '../../../lib/cn';
import { dataHora } from '../../solicitacoes/shared/datas';
import type { Pagina } from '../../solicitacoes/shared/tipos';

type StatusNotificacao = 'PENDENTE' | 'ENVIADO' | 'FALHA';

interface Notificacao {
  id: number;
  protocolo: string;
  destinatario: string;
  assunto: string;
  status: StatusNotificacao;
  erro: string | null;
  criadoEm: string;
  enviadoEm: string | null;
}

const POR_PAGINA = 20;

const ROTULO: Record<StatusNotificacao, string> = {
  PENDENTE: 'Pendente',
  ENVIADO: 'Enviado',
  FALHA: 'Falha',
};

const TOM = { PENDENTE: 'contorno', ENVIADO: 'primario', FALHA: 'perigo' } as const;

export function NotificacoesPage() {
  const [parametros, setParametros] = useSearchParams();
  const status = (parametros.get('status') ?? '') as StatusNotificacao | '';
  const pagina = Math.max(1, Number(parametros.get('pagina')) || 1);

  const definir = (novoStatus: string, novaPagina: number) => {
    const novos = new URLSearchParams();
    if (novoStatus) novos.set('status', novoStatus);
    if (novaPagina > 1) novos.set('pagina', String(novaPagina));
    setParametros(novos);
  };

  const log = useQuery({
    queryKey: ['admin', 'notificacoes', status, pagina],
    queryFn: ({ signal }) => {
      const consulta = new URLSearchParams({ page: String(pagina - 1), size: String(POR_PAGINA) });
      if (status) consulta.set('status', status);
      return api.get<Pagina<Notificacao>>(`/admin/notificacoes?${consulta}`, { signal });
    },
    placeholderData: keepPreviousData,
  });

  return (
    <>
      <CabecalhoDePagina
        titulo="Notificações por e-mail"
        descricao="Avisos enviados aos assistidos a cada mudança de status. Envios com falha mostram o erro do servidor de e-mail."
      />

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <Field label="Status do envio" className="w-56">
          <Select value={status} onChange={(evento) => definir(evento.target.value, 1)}>
            <option value="">Todos</option>
            <option value="FALHA">Falha</option>
            <option value="ENVIADO">Enviado</option>
            <option value="PENDENTE">Pendente</option>
          </Select>
        </Field>
        {log.data && (
          <p className="pb-2 text-sm text-neutral-600">
            {log.data.page.totalElements}{' '}
            {log.data.page.totalElements === 1 ? 'registro' : 'registros'}
          </p>
        )}
      </div>

      {log.isPending && <Carregando texto="Carregando o log de e-mails…" />}

      {log.isError && (
        <EstadoDeErro
          titulo="Não foi possível carregar o log de e-mails."
          erro={log.error}
          aoTentarNovamente={() => log.refetch()}
        />
      )}

      {log.isSuccess && log.data.page.totalElements === 0 && (
        <EmptyState
          titulo={status === 'FALHA' ? 'Nenhum envio com falha' : 'Nenhum e-mail registrado'}
          descricao={
            status
              ? 'Escolha "Todos" para ver os demais envios.'
              : 'Os e-mails aparecem aqui quando um pedido muda de status.'
          }
        />
      )}

      {log.isSuccess && log.data.page.totalElements > 0 && (
        <>
          <div className="overflow-x-auto rounded-md border border-neutral-200 bg-white">
            <table className="w-full min-w-[760px] text-left text-sm">
              <caption className="sr-only">
                Log de e-mails, do mais recente para o mais antigo
              </caption>
              <thead className="border-b border-neutral-200 bg-neutral-50 text-xs text-neutral-600">
                <tr>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Data
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Protocolo
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Destinatário
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Assunto
                  </th>
                  <th scope="col" className="px-4 py-2 font-medium">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody>
                {log.data.content.map((n) => {
                  const falha = n.status === 'FALHA';
                  return (
                    <Fragment key={n.id}>
                      <tr className={cn('border-t border-neutral-200', falha && 'bg-danger-50')}>
                        <td className="px-4 py-2 whitespace-nowrap text-neutral-700">
                          <time dateTime={n.criadoEm}>{dataHora(n.criadoEm)}</time>
                        </td>
                        <td className="px-4 py-2 whitespace-nowrap">
                          <Link
                            to={`/admin/triagem?busca=${encodeURIComponent(n.protocolo)}`}
                            className="text-primary-700 font-mono underline-offset-2 hover:underline"
                          >
                            {n.protocolo}
                          </Link>
                        </td>
                        <td className="px-4 py-2 text-neutral-800">{n.destinatario}</td>
                        <td className="px-4 py-2 text-neutral-800">{n.assunto}</td>
                        <td className="px-4 py-2">
                          <Badge tom={TOM[n.status]}>{ROTULO[n.status]}</Badge>
                        </td>
                      </tr>
                      {falha && n.erro && (
                        <tr className="bg-danger-50">
                          <td colSpan={5} className="text-danger-700 px-4 pb-2.5 font-mono text-xs">
                            <span className="font-sans font-semibold">Erro: </span>
                            {n.erro}
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>

          {log.data.page.totalPages > 1 && (
            <nav
              aria-label="Páginas do log de e-mails"
              className="mt-3 flex items-center justify-between gap-3 text-sm text-neutral-700"
            >
              <span>
                Página {pagina} de {log.data.page.totalPages}
              </span>
              <div className="flex gap-2">
                <Button
                  variante="secundaria"
                  tamanho="sm"
                  disabled={pagina <= 1 || log.isPlaceholderData}
                  onClick={() => definir(status, pagina - 1)}
                >
                  Anterior
                </Button>
                <Button
                  variante="secundaria"
                  tamanho="sm"
                  disabled={pagina >= log.data.page.totalPages || log.isPlaceholderData}
                  onClick={() => definir(status, pagina + 1)}
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

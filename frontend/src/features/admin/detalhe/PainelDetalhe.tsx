import { useQuery } from '@tanstack/react-query';
import { differenceInYears, format, parseISO } from 'date-fns';
import type { ReactNode } from 'react';

import { Badge, Carregando, Dialog, EstadoDeErro } from '../../../components/ui';
import { api } from '../../../lib/api';
import { mascararCpf, mascararTelefone } from '../../../lib/mascaras';
import { dataHora } from '../../solicitacoes/shared/datas';
import {
  ROTULO_MODALIDADE,
  ROTULO_STATUS,
  VULNERABILIDADES,
} from '../../solicitacoes/shared/rotulos';
import { StatusBadge } from '../../solicitacoes/shared/StatusBadge';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { MenuMoverPara } from '../transicao/MenuMoverPara';
import type { AlvoDeTransicao } from '../transicao/tipos';
import { ChecklistEditavel } from './ChecklistEditavel';
import { ConversaEquipe } from './ConversaEquipe';
import type { SolicitacaoAdmin } from './tipos';

interface PainelDetalheProps {
  protocolo: string | null;
  emTransicao: boolean;
  aoFechar: () => void;
  aoMover: (alvo: AlvoDeTransicao, destino: StatusSolicitacao) => void;
}

const real = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const ROTULO_VULNERABILIDADE = Object.fromEntries(VULNERABILIDADES.map((v) => [v.valor, v.rotulo]));

export function PainelDetalhe({ protocolo, emTransicao, aoFechar, aoMover }: PainelDetalheProps) {
  const pedido = useQuery({
    queryKey: ['admin', 'solicitacao', protocolo],
    queryFn: ({ signal }) =>
      api.get<SolicitacaoAdmin>(`/admin/solicitacoes/${protocolo}`, { signal }),
    enabled: protocolo !== null,
  });

  return (
    <Dialog
      aberto={protocolo !== null}
      aoFechar={aoFechar}
      posicao="lateral"
      titulo={`Pedido ${protocolo ?? ''}`}
      descricao={
        pedido.data && (
          <span className="flex flex-wrap items-center gap-2">
            <StatusBadge status={pedido.data.status} />
            {pedido.data.areaDescricao} · {pedido.data.assunto}
          </span>
        )
      }
    >
      {pedido.isPending && <Carregando texto="Carregando o pedido…" />}
      {pedido.isError && (
        <EstadoDeErro
          titulo="Não foi possível carregar o pedido."
          erro={pedido.error}
          aoTentarNovamente={() => pedido.refetch()}
        />
      )}
      {pedido.isSuccess && (
        <ConteudoDoPedido
          pedido={pedido.data}
          emTransicao={emTransicao}
          aoMover={(destino) =>
            aoMover(
              {
                protocolo: pedido.data.protocolo,
                nome: pedido.data.assistido.nomeSocial ?? pedido.data.assistido.nome,
                versao: pedido.data.versao,
                status: pedido.data.status,
                unidade: pedido.data.unidade.nome,
              },
              destino,
            )
          }
        />
      )}
    </Dialog>
  );
}

function ConteudoDoPedido({
  pedido,
  emTransicao,
  aoMover,
}: {
  pedido: SolicitacaoAdmin;
  emTransicao: boolean;
  aoMover: (destino: StatusSolicitacao) => void;
}) {
  const a = pedido.assistido;
  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-2 rounded-md border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm">
        <span className="text-neutral-700">
          Responsável:{' '}
          <strong className="text-neutral-900">{pedido.responsavel ?? 'ninguém ainda'}</strong>
        </span>
        {pedido.proximosStatus.length > 0 ? (
          <MenuMoverPara
            destinos={pedido.proximosStatus}
            aoEscolher={aoMover}
            desabilitado={emTransicao}
            rotuloAcessivel={`Mover o pedido ${pedido.protocolo} para outro status`}
          />
        ) : (
          <span className="text-neutral-600">Status final</span>
        )}
      </div>

      <Secao titulo="Assistido">
        <dl className="grid gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
          <Dado rotulo="Nome">{a.nome}</Dado>
          {a.nomeSocial && <Dado rotulo="Nome social">{a.nomeSocial}</Dado>}
          <Dado rotulo="CPF">{mascararCpf(a.cpf)}</Dado>
          <Dado rotulo="Nascimento">
            {format(parseISO(a.dataNascimento), 'dd/MM/yyyy')} (
            {differenceInYears(new Date(), parseISO(a.dataNascimento))} anos)
          </Dado>
          <Dado rotulo="Telefone">{mascararTelefone(a.telefone)}</Dado>
          <Dado rotulo="E-mail">{a.email ?? 'Não informado'}</Dado>
          <Dado rotulo="Cidade">{a.cidade}</Dado>
          <Dado rotulo="Pessoas na família">{a.membrosFamilia}</Dado>
          <Dado rotulo="Renda individual">{real.format(a.rendaIndividual)}</Dado>
          <Dado rotulo="Renda familiar">
            {a.rendaFamiliar === null ? 'Não informada' : real.format(a.rendaFamiliar)}
          </Dado>
          <Dado rotulo="Imóvel próprio">{a.possuiImovel ? 'Sim' : 'Não'}</Dado>
          <Dado rotulo="Veículo">{a.possuiVeiculo ? 'Sim' : 'Não'}</Dado>
          <Dado rotulo="Vulnerabilidades" largo>
            {a.vulnerabilidades.length === 0 ? (
              'Nenhuma declarada'
            ) : (
              <span className="flex flex-wrap gap-1">
                {a.vulnerabilidades.map((v) => (
                  <Badge key={v} tom="neutro">
                    {ROTULO_VULNERABILIDADE[v]}
                  </Badge>
                ))}
              </span>
            )}
          </Dado>
        </dl>
      </Secao>

      <Secao titulo="Pedido">
        <dl className="grid gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
          <Dado rotulo="Aberto em">{dataHora(pedido.criadoEm)}</Dado>
          <Dado rotulo="Unidade">{pedido.unidade.nome}</Dado>
          <Dado rotulo="Atendimento">{ROTULO_MODALIDADE[pedido.modalidade]}</Dado>
          <Dado rotulo="Prioridade">{pedido.prioridade === 'ALTA' ? 'Alta' : 'Normal'}</Dado>
          <Dado rotulo="Hipossuficiência">
            {pedido.hipossuficiencia === 'PRESUMIDA'
              ? 'Presumida'
              : 'Renda acima do limite (analisar)'}
          </Dado>
          {pedido.dataAgendamento && (
            <Dado rotulo="Agendamento">
              {dataHora(pedido.dataAgendamento)} · {pedido.localAtendimento}
            </Dado>
          )}
          {pedido.encaminhamentoDestino && (
            <Dado rotulo="Encaminhado para">{pedido.encaminhamentoDestino}</Dado>
          )}
          {pedido.motivoIndeferimento && (
            <Dado rotulo="Motivo do indeferimento" largo>
              {pedido.motivoIndeferimento}
            </Dado>
          )}
          <Dado rotulo="Relato" largo>
            <span className="whitespace-pre-line">{pedido.relato}</span>
          </Dado>
        </dl>
      </Secao>

      <Secao titulo="Documentos">
        <ChecklistEditavel pedido={pedido} />
      </Secao>

      <Secao titulo="Conversa">
        <ConversaEquipe protocolo={pedido.protocolo} mensagens={pedido.mensagens} />
      </Secao>

      <Secao titulo="Histórico">
        <ol className="space-y-3 text-sm">
          {pedido.historico.map((h) => (
            <li
              key={`${h.statusNovo}-${h.criadoEm}`}
              className="border-l-2 border-neutral-200 pl-3"
            >
              <p className="font-medium text-neutral-900">
                {h.statusAnterior ? `${ROTULO_STATUS[h.statusAnterior]} → ` : ''}
                {ROTULO_STATUS[h.statusNovo]}
              </p>
              <p className="text-xs text-neutral-600">
                {dataHora(h.criadoEm)}
                {h.autor && ` · ${h.autor}`}
              </p>
              {h.observacao && <p className="mt-0.5 text-neutral-700">{h.observacao}</p>}
            </li>
          ))}
        </ol>
      </Secao>
    </div>
  );
}

function Secao({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section>
      <h3 className="mb-2 border-b border-neutral-200 pb-1 text-sm font-semibold text-neutral-900">
        {titulo}
      </h3>
      {children}
    </section>
  );
}

function Dado({
  rotulo,
  largo,
  children,
}: {
  rotulo: string;
  largo?: boolean;
  children: ReactNode;
}) {
  return (
    <div className={largo ? 'sm:col-span-2' : undefined}>
      <dt className="text-xs text-neutral-600">{rotulo}</dt>
      <dd className="text-neutral-900">{children}</dd>
    </div>
  );
}

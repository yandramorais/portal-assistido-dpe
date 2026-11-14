import { useMutation, useQueryClient } from '@tanstack/react-query';

import { useToast } from '../../../components/ui';
import { api, ApiError, mensagemDoErro } from '../../../lib/api';
import { DOCUMENTOS } from '../../solicitacoes/shared/rotulos';
import type { TipoDocumento } from '../../solicitacoes/shared/tipos';
import { MENSAGEM_CONFLITO } from '../transicao/useTransicao';
import type { SolicitacaoAdmin } from './tipos';

export function ChecklistEditavel({ pedido }: { pedido: SolicitacaoAdmin }) {
  const queryClient = useQueryClient();
  const { mostrar } = useToast();

  const atualizacao = useMutation({
    mutationFn: ({ tipo, entregue }: { tipo: TipoDocumento; entregue: boolean }) =>
      api.patch<SolicitacaoAdmin>(`/admin/solicitacoes/${pedido.protocolo}/documentos`, {
        versao: pedido.versao,
        documentos: { [tipo]: entregue },
      }),
    onSuccess: (resposta) => {
      queryClient.setQueryData(['admin', 'solicitacao', pedido.protocolo], resposta);
      queryClient.invalidateQueries({ queryKey: ['admin', 'kanban'] });
    },
    onError: (erro) => {
      mostrar({
        tipo: 'erro',
        mensagem:
          erro instanceof ApiError && erro.status === 409
            ? MENSAGEM_CONFLITO
            : mensagemDoErro(erro),
      });
      queryClient.invalidateQueries({ queryKey: ['admin', 'solicitacao', pedido.protocolo] });
    },
  });

  const entregues = new Map(pedido.documentos.map((d) => [d.tipo, d.entregue]));

  return (
    <fieldset disabled={atualizacao.isPending}>
      <legend className="sr-only">Documentos entregues</legend>
      <ul className="divide-y divide-neutral-200 text-sm">
        {DOCUMENTOS.filter((d) => entregues.has(d.valor)).map((d) => (
          <li key={d.valor}>
            <label className="flex items-center gap-3 py-2">
              <input
                type="checkbox"
                className="accent-primary-700 size-4"
                checked={entregues.get(d.valor) ?? false}
                onChange={(evento) =>
                  atualizacao.mutate({ tipo: d.valor, entregue: evento.target.checked })
                }
              />
              <span className="text-neutral-800">{d.rotulo}</span>
            </label>
          </li>
        ))}
      </ul>
    </fieldset>
  );
}

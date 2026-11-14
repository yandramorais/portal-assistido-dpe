import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button, Field, Textarea, useToast } from '../../../components/ui';
import { api, mensagemDoErro } from '../../../lib/api';
import { cn } from '../../../lib/cn';
import { dataHora } from '../shared/datas';
import type { MensagemItem, MensagemResponse, StatusSolicitacao } from '../shared/tipos';

const MAX_MENSAGEM = 5000;
export const ID_CAMPO_RESPOSTA = 'resposta';

const esquema = z.object({
  conteudo: z
    .string()
    .trim()
    .min(1, 'Escreva sua mensagem.')
    .max(MAX_MENSAGEM, `Use no máximo ${MAX_MENSAGEM.toLocaleString('pt-BR')} caracteres.`),
});

type DadosMensagem = z.infer<typeof esquema>;

interface ConversaProps {
  protocolo: string;
  status: StatusSolicitacao;
  mensagens: MensagemItem[];
}

/** Mensagens da equipe e do assistido, com o campo de resposta. */
export function Conversa({ protocolo, status, mensagens }: ConversaProps) {
  const queryClient = useQueryClient();
  const { mostrar } = useToast();
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<DadosMensagem>({ resolver: zodResolver(esquema), defaultValues: { conteudo: '' } });

  const envio = useMutation({
    mutationFn: (dados: DadosMensagem) =>
      api.post<MensagemResponse>(`/solicitacoes/${protocolo}/mensagens`, {
        conteudo: dados.conteudo,
      }),
    onSuccess: (resposta) => {
      reset();
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] });
      const reaberto =
        status === 'PENDENTE_DOCUMENTACAO' && resposta.statusSolicitacao === 'EM_ANALISE';
      mostrar({
        tipo: 'sucesso',
        mensagem: reaberto
          ? 'Resposta enviada. Seu pedido voltou para análise da equipe.'
          : 'Mensagem enviada.',
      });
    },
  });

  return (
    <div>
      {mensagens.length === 0 ? (
        <p className="text-sm text-neutral-600">
          Nenhuma mensagem ainda. Quando a equipe escrever, a mensagem aparece aqui e você recebe um
          aviso por e-mail.
        </p>
      ) : (
        <ol className="space-y-3">
          {mensagens.map((m) => (
            <li
              key={m.id}
              className={cn(
                'rounded-md border px-4 py-3 text-sm',
                m.daEquipe
                  ? 'border-primary-200 bg-primary-50'
                  : 'border-neutral-200 bg-white sm:ml-8',
              )}
            >
              <p className="flex flex-wrap items-baseline justify-between gap-2 text-xs text-neutral-600">
                <span className="font-semibold text-neutral-800">
                  {m.daEquipe ? `${m.autor} · Defensoria` : 'Você'}
                </span>
                <time dateTime={m.criadoEm}>{dataHora(m.criadoEm)}</time>
              </p>
              <p className="mt-1 whitespace-pre-line text-neutral-900">{m.conteudo}</p>
            </li>
          ))}
        </ol>
      )}

      <form
        noValidate
        onSubmit={handleSubmit((dados) => envio.mutate(dados))}
        className="mt-4 space-y-3 border-t border-neutral-200 pt-4"
      >
        {envio.isError && (
          <p
            role="alert"
            className="border-danger-200 bg-danger-50 text-danger-700 rounded border px-3 py-2 text-sm"
          >
            {mensagemDoErro(envio.error)}
          </p>
        )}
        <Field
          label="Sua resposta"
          erro={errors.conteudo?.message}
          dica={`${watch('conteudo').length.toLocaleString('pt-BR')} de ${MAX_MENSAGEM.toLocaleString('pt-BR')} caracteres.`}
        >
          <Textarea
            id={ID_CAMPO_RESPOSTA}
            rows={4}
            maxLength={MAX_MENSAGEM}
            {...register('conteudo')}
          />
        </Field>
        <div className="flex justify-end">
          <Button type="submit" carregando={envio.isPending}>
            Enviar mensagem
          </Button>
        </div>
      </form>
    </div>
  );
}

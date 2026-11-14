import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Badge, Button, Field, Textarea, useToast } from '../../../components/ui';
import { api, mensagemDoErro } from '../../../lib/api';
import { cn } from '../../../lib/cn';
import { dataHora } from '../../solicitacoes/shared/datas';
import type { MensagemEquipe } from './tipos';

const esquema = z.object({
  conteudo: z
    .string()
    .trim()
    .min(1, 'Escreva a mensagem.')
    .max(5000, 'Use no máximo 5.000 caracteres.'),
  notaInterna: z.boolean(),
});

type DadosMensagem = z.infer<typeof esquema>;

interface ConversaEquipeProps {
  protocolo: string;
  mensagens: MensagemEquipe[];
}

export function ConversaEquipe({ protocolo, mensagens }: ConversaEquipeProps) {
  const queryClient = useQueryClient();
  const { mostrar } = useToast();
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm<DadosMensagem>({
    resolver: zodResolver(esquema),
    defaultValues: { conteudo: '', notaInterna: false },
  });
  const notaInterna = watch('notaInterna');

  const envio = useMutation({
    mutationFn: (dados: DadosMensagem) =>
      api.post(`/solicitacoes/${protocolo}/mensagens`, {
        conteudo: dados.conteudo,
        visivelAssistido: !dados.notaInterna,
      }),
    onSuccess: (_resposta, dados) => {
      reset({ conteudo: '', notaInterna: dados.notaInterna });
      queryClient.invalidateQueries({ queryKey: ['admin', 'solicitacao', protocolo] });
      mostrar({
        tipo: 'sucesso',
        mensagem: dados.notaInterna ? 'Nota interna registrada.' : 'Mensagem enviada ao assistido.',
      });
    },
  });

  return (
    <div>
      {mensagens.length === 0 ? (
        <p className="text-sm text-neutral-600">Nenhuma mensagem neste pedido.</p>
      ) : (
        <ol className="space-y-2">
          {mensagens.map((m) => (
            <li
              key={m.id}
              className={cn(
                'rounded-md border px-3 py-2 text-sm',
                !m.visivelAssistido && 'border-dashed border-neutral-400 bg-neutral-100',
                m.visivelAssistido && m.daEquipe && 'border-primary-200 bg-primary-50',
                m.visivelAssistido && !m.daEquipe && 'border-neutral-200 bg-white',
              )}
            >
              <p className="flex flex-wrap items-center gap-2 text-xs text-neutral-600">
                <span className="font-semibold text-neutral-800">
                  {m.daEquipe ? m.autor : `${m.autor} (assistido)`}
                </span>
                {!m.visivelAssistido && <Badge tom="contorno">Nota interna</Badge>}
                <time dateTime={m.criadoEm} className="ml-auto">
                  {dataHora(m.criadoEm)}
                </time>
              </p>
              <p className="mt-1 whitespace-pre-line text-neutral-900">{m.conteudo}</p>
            </li>
          ))}
        </ol>
      )}

      <form
        noValidate
        onSubmit={handleSubmit((dados) => envio.mutate(dados))}
        className="mt-3 space-y-2"
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
          label={notaInterna ? 'Nota interna' : 'Mensagem ao assistido'}
          erro={errors.conteudo?.message}
          dica={
            notaInterna
              ? 'Só a equipe vê. O assistido não é avisado.'
              : 'O assistido vê no pedido e recebe aviso por e-mail.'
          }
        >
          <Textarea
            rows={3}
            className={cn(notaInterna && 'border-dashed bg-neutral-50')}
            {...register('conteudo')}
          />
        </Field>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <label className="flex items-center gap-2 text-sm text-neutral-800">
            <input
              type="checkbox"
              className="accent-primary-700 size-4"
              {...register('notaInterna')}
            />
            Nota interna (só a equipe vê)
          </label>
          <Button type="submit" tamanho="sm" carregando={envio.isPending}>
            {notaInterna ? 'Salvar nota' : 'Enviar mensagem'}
          </Button>
        </div>
      </form>
    </div>
  );
}

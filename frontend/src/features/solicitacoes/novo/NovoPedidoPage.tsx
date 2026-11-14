import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { CabecalhoDePagina } from '../../../components/layout/CabecalhoDePagina';
import {
  Button,
  Carregando,
  EstadoDeErro,
  Field,
  Input,
  Select,
  Textarea,
} from '../../../components/ui';
import { api, mensagemDoErro } from '../../../lib/api';
import { aplicarErrosDoServidor } from '../../../lib/formulario';
import { AREAS, ASSUNTOS_POR_AREA, DOCUMENTOS, ROTULO_MODALIDADE } from '../shared/rotulos';
import type {
  AreaAtuacao,
  Modalidade,
  NovaSolicitacaoRequest,
  SolicitacaoDetalhe,
  TipoDocumento,
  Unidade,
} from '../shared/tipos';
import { ConfirmacaoPedido } from './ConfirmacaoPedido';

const MAX_RELATO = 5000;

const esquema = z.object({
  unidadeId: z.string().min(1, 'Escolha a unidade onde quer ser atendido.'),
  area: z.string().min(1, 'Escolha a área do seu pedido.'),
  assunto: z
    .string()
    .trim()
    .min(1, 'Informe o assunto em poucas palavras.')
    .max(150, 'Use no máximo 150 caracteres.'),
  relato: z
    .string()
    .trim()
    .min(1, 'Conte o que aconteceu.')
    .max(MAX_RELATO, `Use no máximo ${MAX_RELATO.toLocaleString('pt-BR')} caracteres.`),
  modalidade: z.enum(['PRESENCIAL', 'VIRTUAL'], { message: 'Escolha como prefere ser atendido.' }),
  documentosEntregues: z.array(z.string()),
});

type DadosPedido = z.input<typeof esquema>;
const CAMPOS = ['unidadeId', 'area', 'assunto', 'relato', 'modalidade', 'documentosEntregues'];

export function NovoPedidoPage() {
  const queryClient = useQueryClient();
  const [pedidoCriado, setPedidoCriado] = useState<SolicitacaoDetalhe | null>(null);

  const unidades = useQuery({
    queryKey: ['unidades'],
    queryFn: () => api.get<Unidade[]>('/unidades'),
    staleTime: 10 * 60_000,
  });

  const {
    register,
    handleSubmit,
    setError,
    watch,
    formState: { errors },
  } = useForm<DadosPedido>({
    resolver: zodResolver(esquema),
    defaultValues: {
      unidadeId: '',
      area: '',
      assunto: '',
      relato: '',
      modalidade: undefined,
      documentosEntregues: [],
    },
  });

  const area = watch('area') as AreaAtuacao | '';
  const tamanhoRelato = watch('relato').length;

  const envio = useMutation({
    mutationFn: (dados: DadosPedido) => {
      const corpo: NovaSolicitacaoRequest = {
        unidadeId: Number(dados.unidadeId),
        area: dados.area as AreaAtuacao,
        assunto: dados.assunto.trim(),
        relato: dados.relato.trim(),
        modalidade: dados.modalidade as Modalidade,
        documentosEntregues: dados.documentosEntregues as TipoDocumento[],
      };
      return api.post<SolicitacaoDetalhe>('/solicitacoes', corpo);
    },
    onSuccess: (pedido) => {
      queryClient.invalidateQueries({ queryKey: ['solicitacoes'] });
      setPedidoCriado(pedido);
    },
    onError: (erro) => aplicarErrosDoServidor(erro, setError, CAMPOS),
  });

  if (pedidoCriado) {
    return <ConfirmacaoPedido pedido={pedidoCriado} />;
  }

  return (
    <div className="max-w-3xl">
      <CabecalhoDePagina
        titulo="Novo pedido"
        descricao="Conte qual é o seu problema. A equipe da Defensoria analisa e responde por aqui e por e-mail."
      />

      {unidades.isError && (
        <EstadoDeErro
          titulo="Não foi possível carregar as unidades de atendimento."
          erro={unidades.error}
          aoTentarNovamente={() => unidades.refetch()}
        />
      )}
      {unidades.isPending && <Carregando texto="Carregando o formulário…" />}

      {unidades.isSuccess && (
        <form
          noValidate
          onSubmit={handleSubmit((dados) => envio.mutate(dados))}
          className="space-y-5 rounded-md border border-neutral-200 bg-white p-5"
        >
          {envio.isError &&
            !CAMPOS.some((c) => errors[c as keyof DadosPedido]?.type === 'server') && (
              <p
                role="alert"
                className="border-danger-200 bg-danger-50 text-danger-700 rounded border px-3 py-2 text-sm"
              >
                {mensagemDoErro(envio.error)}
              </p>
            )}

          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Área" erro={errors.area?.message} obrigatorio>
              <Select {...register('area')}>
                <option value="">Selecione</option>
                {AREAS.map((a) => (
                  <option key={a.valor} value={a.valor}>
                    {a.rotulo}
                  </option>
                ))}
              </Select>
            </Field>

            <Field
              label="Assunto"
              erro={errors.assunto?.message}
              dica={
                area
                  ? 'Escolha uma sugestão ou escreva com suas palavras.'
                  : 'Escolha a área para ver sugestões.'
              }
              obrigatorio
            >
              <Input list="sugestoes-de-assunto" autoComplete="off" {...register('assunto')} />
            </Field>
            <datalist id="sugestoes-de-assunto">
              {area &&
                ASSUNTOS_POR_AREA[area].map((assunto) => <option key={assunto} value={assunto} />)}
            </datalist>
          </div>

          <Field
            label="O que aconteceu?"
            erro={errors.relato?.message}
            dica={`${tamanhoRelato.toLocaleString('pt-BR')} de ${MAX_RELATO.toLocaleString('pt-BR')} caracteres. Conte com suas palavras, com datas e nomes se lembrar.`}
            obrigatorio
          >
            <Textarea rows={7} maxLength={MAX_RELATO} {...register('relato')} />
          </Field>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Unidade de atendimento" erro={errors.unidadeId?.message} obrigatorio>
              <Select {...register('unidadeId')}>
                <option value="">Selecione</option>
                {unidades.data.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.nome} ({u.comarca})
                  </option>
                ))}
              </Select>
            </Field>

            <fieldset aria-invalid={errors.modalidade ? true : undefined}>
              <legend className="text-sm font-medium text-neutral-800">
                Como prefere ser atendido?
                <span className="text-danger-600 ml-0.5" aria-hidden="true">
                  *
                </span>
              </legend>
              <div className="mt-2 flex flex-wrap gap-4">
                {(Object.keys(ROTULO_MODALIDADE) as Modalidade[]).map((m) => (
                  <label key={m} className="flex items-center gap-2 text-sm text-neutral-800">
                    <input
                      type="radio"
                      value={m}
                      className="accent-primary-700 size-4"
                      {...register('modalidade')}
                    />
                    {ROTULO_MODALIDADE[m]}
                  </label>
                ))}
              </div>
              {errors.modalidade && (
                <p className="text-danger-700 mt-1 text-xs font-medium">
                  {errors.modalidade.message}
                </p>
              )}
            </fieldset>
          </div>

          <fieldset>
            <legend className="text-sm font-medium text-neutral-800">
              Quais documentos você já tem?
            </legend>
            <p className="border-primary-200 bg-primary-50 text-primary-900 mt-2 rounded border px-3 py-2 text-sm">
              Mesmo sem todos os documentos você pode enviar o pedido.
            </p>
            <div className="mt-3 grid gap-2 sm:grid-cols-2">
              {DOCUMENTOS.map((d) => (
                <label key={d.valor} className="flex items-start gap-2 text-sm text-neutral-800">
                  <input
                    type="checkbox"
                    value={d.valor}
                    className="accent-primary-700 mt-0.5 size-4"
                    {...register('documentosEntregues')}
                  />
                  <span>
                    {d.rotulo}
                    <span className="block text-xs text-neutral-600">{d.dica}</span>
                  </span>
                </label>
              ))}
            </div>
          </fieldset>

          <div className="flex justify-end border-t border-neutral-200 pt-4">
            <Button type="submit" carregando={envio.isPending}>
              Enviar pedido
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}

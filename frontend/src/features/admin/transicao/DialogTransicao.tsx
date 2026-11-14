import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button, Dialog, Field, Input, Textarea } from '../../../components/ui';
import { campoExigido } from '../../../lib/transicoes';
import { ROTULO_STATUS } from '../../solicitacoes/shared/rotulos';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';
import { MODELO_DE_DEVOLUTIVA, SUGESTOES_DE_DESTINO } from './modelos';
import type { CamposDeTransicao, PedidoDeTransicao, TransicaoEscolhida } from './tipos';

interface DialogTransicaoProps {
  transicao: TransicaoEscolhida | null;
  aoFechar: () => void;
  aoConfirmar: (pedido: PedidoDeTransicao) => void;
}

export function DialogTransicao({ transicao, aoFechar, aoConfirmar }: DialogTransicaoProps) {
  return (
    <Dialog
      aberto={transicao !== null}
      aoFechar={aoFechar}
      titulo={
        transicao
          ? `Mover ${transicao.alvo.protocolo} para ${ROTULO_STATUS[transicao.destino]}`
          : ''
      }
      descricao={
        transicao && (
          <>
            {transicao.alvo.nome} · hoje em {ROTULO_STATUS[transicao.alvo.status]}
          </>
        )
      }
      rodape={
        <>
          <Button variante="secundaria" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-transicao">
            Confirmar
          </Button>
        </>
      }
    >
      {transicao && (
        <FormularioTransicao
          key={`${transicao.alvo.protocolo}-${transicao.destino}`}
          transicao={transicao}
          aoConfirmar={aoConfirmar}
        />
      )}
    </Dialog>
  );
}

interface DadosTransicao {
  dataAgendamento: string;
  localAtendimento: string;
  destino: string;
  motivo: string;
  devolutiva: string;
}

function texto(maximo: number, obrigatorio: boolean, mensagem: string) {
  const base = z.string().trim().max(maximo, `Use no máximo ${maximo} caracteres.`);
  return obrigatorio ? base.min(1, mensagem) : base;
}

function esquemaPara(destino: StatusSolicitacao) {
  const exige = (campo: Parameters<typeof campoExigido>[1]) => campoExigido(destino, campo);
  return z.object({
    dataAgendamento: exige('dataAgendamento')
      ? z
          .string()
          .min(1, 'Informe a data e a hora do atendimento.')
          .refine(
            (valor) => new Date(valor).getTime() > Date.now(),
            'A data do atendimento deve ser no futuro.',
          )
      : z.string(),
    localAtendimento: texto(200, exige('localAtendimento'), 'Informe o local do atendimento.'),
    destino: texto(150, exige('destino'), 'Informe para onde o caso foi encaminhado.'),
    motivo: texto(5000, exige('motivo'), 'Informe o motivo do indeferimento.'),
    devolutiva: texto(5000, exige('devolutiva'), 'Escreva a mensagem ao assistido.'),
  });
}

function vazioParaIndefinido(valor: string): string | undefined {
  return valor.trim() === '' ? undefined : valor.trim();
}

function FormularioTransicao({
  transicao,
  aoConfirmar,
}: {
  transicao: TransicaoEscolhida;
  aoConfirmar: (pedido: PedidoDeTransicao) => void;
}) {
  const { destino, alvo } = transicao;
  const esquema = useMemo(() => esquemaPara(destino), [destino]);
  const exige = (campo: Parameters<typeof campoExigido>[1]) => campoExigido(destino, campo);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<DadosTransicao>({
    resolver: zodResolver(esquema),
    defaultValues: {
      dataAgendamento: '',
      localAtendimento: alvo.unidade ?? '',
      destino: '',
      motivo: '',
      devolutiva: MODELO_DE_DEVOLUTIVA[destino],
    },
  });

  const enviar = handleSubmit((dados) => {
    const campos: CamposDeTransicao = {
      dataAgendamento: dados.dataAgendamento
        ? new Date(dados.dataAgendamento).toISOString()
        : undefined,
      localAtendimento: exige('localAtendimento')
        ? vazioParaIndefinido(dados.localAtendimento)
        : undefined,
      destino: vazioParaIndefinido(dados.destino),
      motivo: vazioParaIndefinido(dados.motivo),
      devolutiva: vazioParaIndefinido(dados.devolutiva),
    };
    aoConfirmar({ alvo, destino, campos });
  });

  const devolutivaObrigatoria = exige('devolutiva');

  return (
    <form id="form-transicao" noValidate onSubmit={enviar} className="space-y-4">
      {exige('dataAgendamento') && (
        <Field
          label="Data e hora do atendimento"
          erro={errors.dataAgendamento?.message}
          obrigatorio
        >
          <Input type="datetime-local" {...register('dataAgendamento')} />
        </Field>
      )}
      {exige('localAtendimento') && (
        <Field
          label="Local do atendimento"
          erro={errors.localAtendimento?.message}
          dica="Para atendimento virtual, informe como será (ex.: link enviado por mensagem)."
          obrigatorio
        >
          <Input {...register('localAtendimento')} />
        </Field>
      )}
      {exige('destino') && (
        <>
          <Field label="Encaminhado para" erro={errors.destino?.message} obrigatorio>
            <Input list="sugestoes-de-destino" autoComplete="off" {...register('destino')} />
          </Field>
          <datalist id="sugestoes-de-destino">
            {SUGESTOES_DE_DESTINO.map((s) => (
              <option key={s} value={s} />
            ))}
          </datalist>
        </>
      )}
      {exige('motivo') && (
        <Field
          label="Motivo do indeferimento"
          erro={errors.motivo?.message}
          dica="O assistido vê este motivo no pedido."
          obrigatorio
        >
          <Textarea rows={3} {...register('motivo')} />
        </Field>
      )}

      <Field
        label="Mensagem ao assistido"
        erro={errors.devolutiva?.message}
        obrigatorio={devolutivaObrigatoria}
        dica={
          devolutivaObrigatoria
            ? 'Diga o que falta. O assistido recebe um aviso por e-mail e responde pelo portal.'
            : `Fica visível no pedido. ${watch('devolutiva').trim() ? '' : 'Sem texto, nenhuma mensagem é enviada; o assistido só recebe o aviso da mudança.'}`
        }
      >
        <Textarea rows={5} {...register('devolutiva')} />
      </Field>
    </form>
  );
}

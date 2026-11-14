import { useMutation } from '@tanstack/react-query';
import { useEffect, useRef, useState } from 'react';
import { useForm, type Resolver, type UseFormRegisterReturn } from 'react-hook-form';
import { Link } from 'react-router-dom';

import { useTituloDaPagina } from '../../components/layout/useTituloDaPagina';
import { Button, Field, Input, Select, useToast } from '../../components/ui';
import { api, mensagemDoErro } from '../../lib/api';
import { aplicarErrosDoServidor, comMascara } from '../../lib/formulario';
import { mascararCep, mascararCpf, mascararTelefone } from '../../lib/mascaras';
import type { TokenResponse } from '../auth/types';
import { useAuth } from '../auth/useAuth';
import { VULNERABILIDADES } from '../solicitacoes/shared/rotulos';
import {
  CAMPOS_POR_PASSO,
  RESOLVERS_POR_PASSO,
  paraRegistro,
  UFS,
  VALORES_INICIAIS,
  type DadosCadastro,
} from './esquemaCadastro';
import { ProgressoDosPassos } from './ProgressoDosPassos';

const PASSOS = ['Dados pessoais', 'Endereço', 'Renda e família'] as const;
const TODOS_OS_CAMPOS = CAMPOS_POR_PASSO.flat();

/** Cadastro do assistido em 3 passos. Ao concluir, já entra logado (vai para Meus pedidos). */
export function CadastroPage() {
  useTituloDaPagina('Criar cadastro');
  const { entrar } = useAuth();
  const { mostrar } = useToast();
  const [passo, setPasso] = useState(0);
  const tituloDoPasso = useRef<HTMLHeadingElement>(null);
  const primeiraRenderizacao = useRef(true);

  // Cada passo valida só os próprios campos; o formulário (e os dados) é um só.
  const passoAtual = useRef(0);
  const resolver: Resolver<DadosCadastro> = (valores, contexto, opcoes) =>
    RESOLVERS_POR_PASSO[passoAtual.current](valores, contexto, opcoes);

  const {
    register,
    handleSubmit,
    setError,
    getValues,
    formState: { errors },
  } = useForm<DadosCadastro>({ resolver, defaultValues: VALORES_INICIAIS });

  // Leva o foco ao título do passo para o leitor de tela anunciar a mudança.
  useEffect(() => {
    passoAtual.current = passo;
    if (primeiraRenderizacao.current) {
      primeiraRenderizacao.current = false;
      return;
    }
    tituloDoPasso.current?.focus();
  }, [passo]);

  const cadastro = useMutation({
    mutationFn: (dados: DadosCadastro) =>
      api.post<TokenResponse>('/auth/registro', paraRegistro(dados)),
    onSuccess: (resposta) => {
      mostrar({
        tipo: 'sucesso',
        mensagem: 'Cadastro concluído. Agora você já pode fazer seu pedido.',
      });
      entrar(resposta.token);
    },
    onError: (erro) => {
      const marcados = aplicarErrosDoServidor(erro, setError, TODOS_OS_CAMPOS);
      const passoComErro = CAMPOS_POR_PASSO.findIndex((campos) =>
        campos.some((campo) => marcados.includes(campo)),
      );
      if (passoComErro >= 0) setPasso(passoComErro);
    },
  });

  // Erro que não é de um campo específico (rede, 500…) aparece no topo.
  const erroGeral =
    cadastro.isError &&
    !TODOS_OS_CAMPOS.some((campo) => errors[campo]?.type === 'server') &&
    mensagemDoErro(cadastro.error);

  const avancar = handleSubmit(() => {
    if (passo < PASSOS.length - 1) {
      setPasso(passo + 1);
    } else {
      cadastro.mutate(getValues());
    }
  });

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-xl font-semibold text-neutral-900">Criar cadastro</h1>
      <p className="mt-1 mb-5 text-sm text-neutral-600">
        Com o cadastro você pede atendimento à Defensoria e acompanha seus pedidos. Os campos com *
        são obrigatórios.
      </p>

      <ProgressoDosPassos passos={PASSOS} atual={passo} />

      {erroGeral && (
        <p
          role="alert"
          className="border-danger-200 bg-danger-50 text-danger-700 mb-4 rounded border px-3 py-2 text-sm"
        >
          {erroGeral}
        </p>
      )}

      <form
        noValidate
        onSubmit={avancar}
        className="rounded-md border border-neutral-200 bg-white p-5"
      >
        <h2
          ref={tituloDoPasso}
          tabIndex={-1}
          className="mb-4 text-base font-semibold text-neutral-900 focus:outline-none"
        >
          {passo + 1}. {PASSOS[passo]}
        </h2>

        {passo === 0 && (
          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Nome completo"
              erro={errors.nome?.message}
              obrigatorio
              className="sm:col-span-2"
            >
              <Input autoComplete="name" {...register('nome')} />
            </Field>
            <Field
              label="Nome social"
              erro={errors.nomeSocial?.message}
              dica="Se você usa um nome diferente do registro civil, informe aqui. É assim que vamos chamar você."
              className="sm:col-span-2"
            >
              <Input {...register('nomeSocial')} />
            </Field>
            <Field label="CPF" erro={errors.cpf?.message} obrigatorio>
              <Input
                inputMode="numeric"
                placeholder="000.000.000-00"
                {...comMascara(register('cpf'), mascararCpf)}
              />
            </Field>
            <Field label="Data de nascimento" erro={errors.dataNascimento?.message} obrigatorio>
              <Input type="date" autoComplete="bday" {...register('dataNascimento')} />
            </Field>
            <Field label="Telefone com DDD" erro={errors.telefone?.message} obrigatorio>
              <Input
                type="tel"
                inputMode="numeric"
                autoComplete="tel"
                placeholder="(62) 99999-9999"
                {...comMascara(register('telefone'), mascararTelefone)}
              />
            </Field>
            <Field label="E-mail" erro={errors.email?.message} obrigatorio>
              <Input type="email" autoComplete="email" {...register('email')} />
            </Field>
            <Field
              label="Senha"
              erro={errors.senha?.message}
              dica="Pelo menos 8 caracteres."
              obrigatorio
            >
              <Input type="password" autoComplete="new-password" {...register('senha')} />
            </Field>
            <Field label="Repita a senha" erro={errors.confirmacaoSenha?.message} obrigatorio>
              <Input
                type="password"
                autoComplete="new-password"
                {...register('confirmacaoSenha')}
              />
            </Field>
          </div>
        )}

        {passo === 1 && (
          <div className="grid gap-4 sm:grid-cols-6">
            <Field label="CEP" erro={errors.cep?.message} className="sm:col-span-2">
              <Input
                inputMode="numeric"
                autoComplete="postal-code"
                placeholder="00000-000"
                {...comMascara(register('cep'), mascararCep)}
              />
            </Field>
            <Field
              label="Rua, avenida ou quadra"
              erro={errors.logradouro?.message}
              className="sm:col-span-4"
            >
              <Input autoComplete="address-line1" {...register('logradouro')} />
            </Field>
            <Field label="Número" erro={errors.numero?.message} className="sm:col-span-2">
              <Input {...register('numero')} />
            </Field>
            <Field label="Complemento" erro={errors.complemento?.message} className="sm:col-span-4">
              <Input autoComplete="address-line2" {...register('complemento')} />
            </Field>
            <Field label="Bairro" erro={errors.bairro?.message} className="sm:col-span-6">
              <Input {...register('bairro')} />
            </Field>
            <Field
              label="Cidade"
              erro={errors.cidade?.message}
              obrigatorio
              className="sm:col-span-4"
            >
              <Input autoComplete="address-level2" {...register('cidade')} />
            </Field>
            <Field
              label="Estado (UF)"
              erro={errors.uf?.message}
              obrigatorio
              className="sm:col-span-2"
            >
              <Select autoComplete="address-level1" {...register('uf')}>
                {UFS.map((uf) => (
                  <option key={uf} value={uf}>
                    {uf}
                  </option>
                ))}
              </Select>
            </Field>
          </div>
        )}

        {passo === 2 && (
          <div className="space-y-5">
            <div className="grid gap-4 sm:grid-cols-3">
              <Field
                label="Sua renda mensal (R$)"
                erro={errors.rendaIndividual?.message}
                dica="Salário, benefício ou bicos. Informe 0 se não tiver."
                obrigatorio
              >
                <Input
                  inputMode="decimal"
                  placeholder="1.500,00"
                  {...register('rendaIndividual')}
                />
              </Field>
              <Field
                label="Renda da família (R$)"
                erro={errors.rendaFamiliar?.message}
                dica="Soma de todos que moram com você."
              >
                <Input inputMode="decimal" placeholder="2.500,00" {...register('rendaFamiliar')} />
              </Field>
              <Field label="Pessoas na família" erro={errors.membrosFamilia?.message} obrigatorio>
                <Input type="number" min={1} max={99} {...register('membrosFamilia')} />
              </Field>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <PerguntaSimNao
                legenda="Você tem imóvel próprio?"
                erro={errors.possuiImovel?.message}
                registro={register('possuiImovel')}
              />
              <PerguntaSimNao
                legenda="Você tem veículo?"
                erro={errors.possuiVeiculo?.message}
                registro={register('possuiVeiculo')}
              />
            </div>

            <fieldset>
              <legend className="text-sm font-medium text-neutral-800">
                Alguma destas situações se aplica a você?
              </legend>
              <p className="mt-0.5 text-xs text-neutral-600">
                Marque se for o caso. Essas situações dão prioridade ao seu atendimento e só a
                equipe da Defensoria vê essa informação.
              </p>
              <div className="mt-2 grid gap-2 sm:grid-cols-2">
                {VULNERABILIDADES.map((v) => (
                  <label key={v.valor} className="flex items-center gap-2 text-sm text-neutral-800">
                    <input
                      type="checkbox"
                      value={v.valor}
                      className="accent-primary-700 size-4"
                      {...register('vulnerabilidades')}
                    />
                    {v.rotulo}
                  </label>
                ))}
              </div>
            </fieldset>
          </div>
        )}

        <div className="mt-6 flex flex-wrap items-center justify-between gap-3 border-t border-neutral-200 pt-4">
          {passo > 0 ? (
            <Button
              variante="secundaria"
              onClick={() => setPasso(passo - 1)}
              disabled={cadastro.isPending}
            >
              Voltar
            </Button>
          ) : (
            <span />
          )}
          <Button type="submit" carregando={cadastro.isPending}>
            {passo < PASSOS.length - 1 ? 'Continuar' : 'Criar cadastro'}
          </Button>
        </div>
      </form>

      <p className="mt-4 text-sm text-neutral-700">
        Já tem cadastro?{' '}
        <Link to="/entrar" className="text-primary-700 font-medium underline underline-offset-2">
          Entrar
        </Link>
      </p>
    </div>
  );
}

interface PerguntaSimNaoProps {
  legenda: string;
  erro?: string;
  registro: UseFormRegisterReturn<'possuiImovel' | 'possuiVeiculo'>;
}

function PerguntaSimNao({ legenda, erro, registro }: PerguntaSimNaoProps) {
  return (
    <fieldset aria-invalid={erro ? true : undefined}>
      <legend className="text-sm font-medium text-neutral-800">
        {legenda}
        <span className="text-danger-600 ml-0.5" aria-hidden="true">
          *
        </span>
      </legend>
      <div className="mt-2 flex gap-4">
        {(['sim', 'nao'] as const).map((opcao) => (
          <label key={opcao} className="flex items-center gap-2 text-sm text-neutral-800">
            <input type="radio" value={opcao} className="accent-primary-700 size-4" {...registro} />
            {opcao === 'sim' ? 'Sim' : 'Não'}
          </label>
        ))}
      </div>
      {erro && <p className="text-danger-700 mt-1 text-xs font-medium">{erro}</p>}
    </fieldset>
  );
}

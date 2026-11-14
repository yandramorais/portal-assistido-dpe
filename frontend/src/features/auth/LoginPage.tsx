import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { z } from 'zod';

import { useTituloDaPagina } from '../../components/layout/useTituloDaPagina';
import { Button, Field, Input } from '../../components/ui';
import { api, mensagemDoErro } from '../../lib/api';
import type { TokenResponse } from './types';
import { useAuth } from './useAuth';

const esquema = z.object({
  email: z.string().trim().min(1, 'Informe o e-mail.').email('Informe um e-mail válido.'),
  senha: z.string().min(1, 'Informe a senha.'),
});

type DadosLogin = z.infer<typeof esquema>;

/** Login de assistidos e da equipe. O redirecionamento após entrar fica com SomenteVisitante. */
export function LoginPage() {
  useTituloDaPagina('Entrar');
  const { entrar, sessaoExpirada } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DadosLogin>({
    resolver: zodResolver(esquema),
    defaultValues: { email: '', senha: '' },
  });

  const login = useMutation({
    mutationFn: (dados: DadosLogin) => api.post<TokenResponse>('/auth/login', dados),
    onSuccess: (resposta) => entrar(resposta.token),
  });

  return (
    <div className="mx-auto max-w-sm pt-4 sm:pt-10">
      <h1 className="text-xl font-semibold text-neutral-900">Entrar</h1>
      <p className="mt-1 text-sm text-neutral-600">
        Acesse para pedir atendimento à Defensoria ou acompanhar seus pedidos.
      </p>

      {sessaoExpirada && !login.isError && (
        <p
          role="status"
          className="mt-4 rounded border border-neutral-300 bg-white px-3 py-2 text-sm text-neutral-800"
        >
          Sua sessão expirou. Entre novamente para continuar.
        </p>
      )}

      {login.isError && (
        <p
          role="alert"
          className="border-danger-200 bg-danger-50 text-danger-700 mt-4 rounded border px-3 py-2 text-sm"
        >
          {mensagemDoErro(login.error)}
        </p>
      )}

      <form
        noValidate
        onSubmit={handleSubmit((dados) => login.mutate(dados))}
        className="mt-5 space-y-4 rounded-md border border-neutral-200 bg-white p-5"
      >
        <Field label="E-mail" erro={errors.email?.message} obrigatorio>
          <Input type="email" autoComplete="username" inputMode="email" {...register('email')} />
        </Field>

        <Field label="Senha" erro={errors.senha?.message} obrigatorio>
          <Input type="password" autoComplete="current-password" {...register('senha')} />
        </Field>

        <Button type="submit" carregando={login.isPending} className="w-full">
          Entrar
        </Button>
      </form>

      <p className="mt-4 text-sm text-neutral-700">
        Ainda não tem cadastro?{' '}
        <Link to="/cadastro" className="text-primary-700 font-medium underline underline-offset-2">
          Criar cadastro
        </Link>
      </p>
    </div>
  );
}

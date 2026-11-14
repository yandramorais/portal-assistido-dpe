import type {
  ChangeHandler,
  FieldValues,
  Path,
  UseFormRegisterReturn,
  UseFormSetError,
} from 'react-hook-form';

import { ApiError } from './api';

export function comMascara<N extends string>(
  campo: UseFormRegisterReturn<N>,
  mascara: (valor: string) => string,
): UseFormRegisterReturn<N> {
  const onChange: ChangeHandler = (evento) => {
    evento.target.value = mascara(String(evento.target.value ?? ''));
    return campo.onChange(evento);
  };
  return { ...campo, onChange };
}

export function aplicarErrosDoServidor<T extends FieldValues>(
  erro: unknown,
  setError: UseFormSetError<T>,
  camposDoFormulario: readonly string[],
): string[] {
  if (!(erro instanceof ApiError)) return [];
  const marcados: string[] = [];
  const marcar = (campo: string, mensagem: string) => {
    if (!camposDoFormulario.includes(campo)) return;
    setError(campo as Path<T>, { type: 'server', message: mensagem });
    marcados.push(campo);
  };
  if (erro.status === 409 && erro.campo) marcar(erro.campo, erro.message);
  erro.erros.forEach((e) => marcar(e.campo, e.mensagem));
  return marcados;
}

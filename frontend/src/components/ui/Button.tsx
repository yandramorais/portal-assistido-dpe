import { forwardRef, type ButtonHTMLAttributes } from 'react';

import { classesDeBotao, type TamanhoBotao, type VarianteBotao } from './estilosBotao';
import { Spinner } from './Spinner';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBotao;
  tamanho?: TamanhoBotao;
  /** Mostra o indicador de progresso e bloqueia novos cliques. */
  carregando?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  {
    variante = 'primaria',
    tamanho = 'md',
    carregando = false,
    disabled,
    type = 'button',
    className,
    children,
    ...props
  },
  ref,
) {
  return (
    <button
      ref={ref}
      type={type}
      disabled={disabled || carregando}
      aria-busy={carregando || undefined}
      className={classesDeBotao(variante, tamanho, className)}
      {...props}
    >
      {carregando && <Spinner rotulo="Aguarde" />}
      {children}
    </button>
  );
});

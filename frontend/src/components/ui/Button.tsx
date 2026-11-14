import { forwardRef, type ButtonHTMLAttributes } from 'react';

import { cn } from '../../lib/cn';
import { Spinner } from './Spinner';

type Variante = 'primaria' | 'secundaria' | 'perigo' | 'discreta';
type Tamanho = 'md' | 'sm';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante;
  tamanho?: Tamanho;
  /** Mostra o indicador de progresso e bloqueia novos cliques. */
  carregando?: boolean;
}

const variantes: Record<Variante, string> = {
  primaria: 'bg-primary-700 text-white hover:bg-primary-800 border border-primary-700',
  secundaria: 'bg-white text-neutral-800 hover:bg-neutral-100 border border-neutral-300',
  perigo: 'bg-danger-600 text-white hover:bg-danger-700 border border-danger-600',
  discreta: 'bg-transparent text-primary-700 hover:bg-primary-50 border border-transparent',
};

const tamanhos: Record<Tamanho, string> = {
  md: 'h-9 px-4 text-sm',
  sm: 'h-8 px-3 text-sm',
};

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
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded font-medium transition-colors',
        'disabled:cursor-not-allowed disabled:opacity-60',
        variantes[variante],
        tamanhos[tamanho],
        className,
      )}
      {...props}
    >
      {carregando && <Spinner rotulo="Aguarde" />}
      {children}
    </button>
  );
});

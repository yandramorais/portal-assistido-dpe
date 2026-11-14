import { cn } from '../../lib/cn';

export type VarianteBotao = 'primaria' | 'secundaria' | 'perigo' | 'discreta';
export type TamanhoBotao = 'md' | 'sm';

const variantes: Record<VarianteBotao, string> = {
  primaria: 'bg-primary-700 text-white hover:bg-primary-800 border border-primary-700',
  secundaria: 'bg-white text-neutral-800 hover:bg-neutral-100 border border-neutral-300',
  perigo: 'bg-danger-600 text-white hover:bg-danger-700 border border-danger-600',
  discreta: 'bg-transparent text-primary-700 hover:bg-primary-50 border border-transparent',
};

const tamanhos: Record<TamanhoBotao, string> = {
  md: 'h-9 px-4 text-sm',
  sm: 'h-8 px-3 text-sm',
};

/** Classes do botão, usadas também por links com aparência de botão. */
export function classesDeBotao(
  variante: VarianteBotao = 'primaria',
  tamanho: TamanhoBotao = 'md',
  className?: string,
): string {
  return cn(
    'inline-flex items-center justify-center gap-2 rounded font-medium transition-colors',
    'disabled:cursor-not-allowed disabled:opacity-60',
    variantes[variante],
    tamanhos[tamanho],
    className,
  );
}

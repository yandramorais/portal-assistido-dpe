import type { ReactNode } from 'react';

import { cn } from '../../lib/cn';

interface EmptyStateProps {
  titulo: string;
  descricao?: ReactNode;
  acao?: ReactNode;
  className?: string;
}

export function EmptyState({ titulo, descricao, acao, className }: EmptyStateProps) {
  return (
    <div
      className={cn(
        'rounded-md border border-dashed border-neutral-300 bg-white px-6 py-10 text-center',
        className,
      )}
    >
      <p className="text-sm font-semibold text-neutral-800">{titulo}</p>
      {descricao && <p className="mx-auto mt-1 max-w-md text-sm text-neutral-600">{descricao}</p>}
      {acao && <div className="mt-4 flex justify-center">{acao}</div>}
    </div>
  );
}

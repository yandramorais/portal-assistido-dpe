import type { ReactNode } from 'react';

import { cn } from '../../lib/cn';

interface CardProps {
  titulo?: ReactNode;
  acoes?: ReactNode;
  className?: string;
  children: ReactNode;
}

export function Card({ titulo, acoes, className, children }: CardProps) {
  return (
    <section className={cn('rounded-md border border-neutral-200 bg-white', className)}>
      {(titulo || acoes) && (
        <header className="flex items-center justify-between gap-3 border-b border-neutral-200 px-4 py-3">
          {titulo && <h2 className="text-base font-semibold text-neutral-900">{titulo}</h2>}
          {acoes && <div className="flex items-center gap-2">{acoes}</div>}
        </header>
      )}
      <div className="p-4">{children}</div>
    </section>
  );
}

import type { ReactNode } from 'react';

import { cn } from '../../lib/cn';

type Tom = 'neutro' | 'primario' | 'perigo' | 'contorno';

const tons: Record<Tom, string> = {
  neutro: 'bg-neutral-100 text-neutral-800 border-neutral-200',
  primario: 'bg-primary-50 text-primary-800 border-primary-200',
  perigo: 'bg-danger-50 text-danger-700 border-danger-200',
  contorno: 'bg-white text-neutral-700 border-neutral-300',
};

interface BadgeProps {
  tom?: Tom;
  className?: string;
  children: ReactNode;
}

export function Badge({ tom = 'neutro', className, children }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-sm border px-1.5 py-0.5 text-xs font-medium whitespace-nowrap',
        tons[tom],
        className,
      )}
    >
      {children}
    </span>
  );
}

import { cn } from '../../lib/cn';

interface ProgressoDosPassosProps {
  passos: readonly string[];
  /** Índice do passo atual (0, 1, 2…). */
  atual: number;
}

/** Barra de progresso do cadastro: passos numerados e "Passo X de N" para leitores de tela. */
export function ProgressoDosPassos({ passos, atual }: ProgressoDosPassosProps) {
  const percentual = ((atual + 1) / passos.length) * 100;
  return (
    <div className="mb-6">
      <p className="sr-only">
        Passo {atual + 1} de {passos.length}: {passos[atual]}
      </p>
      <div className="h-1.5 w-full overflow-hidden rounded-sm bg-neutral-200" aria-hidden="true">
        <div
          className="bg-primary-700 h-full transition-[width]"
          style={{ width: `${percentual}%` }}
        />
      </div>
      <ol className="mt-3 grid gap-2 text-sm sm:grid-cols-3" aria-hidden="true">
        {passos.map((passo, indice) => (
          <li
            key={passo}
            className={cn(
              'flex items-center gap-2',
              indice === atual ? 'font-semibold text-neutral-900' : 'text-neutral-600',
            )}
          >
            <span
              className={cn(
                'flex size-6 shrink-0 items-center justify-center rounded-sm border text-xs',
                indice < atual && 'border-primary-700 bg-primary-700 text-white',
                indice === atual && 'border-primary-700 text-primary-800',
                indice > atual && 'border-neutral-300 text-neutral-600',
              )}
            >
              {indice + 1}
            </span>
            {passo}
          </li>
        ))}
      </ol>
    </div>
  );
}

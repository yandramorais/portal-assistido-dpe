import { Spinner } from './Spinner';

export function Carregando({ texto = 'Carregando…' }: { texto?: string }) {
  return (
    <div className="flex items-center gap-2 py-8 text-sm text-neutral-600">
      <Spinner rotulo={texto} />
      <span aria-hidden="true">{texto}</span>
    </div>
  );
}

import type { ReactNode } from 'react';

import { useTituloDaPagina } from './useTituloDaPagina';

interface CabecalhoDePaginaProps {
  titulo: string;
  descricao?: ReactNode;
  /** Ações à direita do título (ex.: botão "Novo pedido"). */
  acoes?: ReactNode;
}

/** h1 da página + título da aba do navegador. */
export function CabecalhoDePagina({ titulo, descricao, acoes }: CabecalhoDePaginaProps) {
  useTituloDaPagina(titulo);
  return (
    <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-xl font-semibold text-neutral-900">{titulo}</h1>
        {descricao && <p className="mt-1 text-sm text-neutral-600">{descricao}</p>}
      </div>
      {acoes && <div className="flex gap-2">{acoes}</div>}
    </div>
  );
}

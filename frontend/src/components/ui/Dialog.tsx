import { useEffect, useId, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

import { cn } from '../../lib/cn';

interface DialogProps {
  aberto: boolean;
  aoFechar: () => void;
  titulo: string;
  descricao?: ReactNode;
  rodape?: ReactNode;
  posicao?: 'centro' | 'lateral';
  className?: string;
  children?: ReactNode;
}

const FOCAVEIS =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), ' +
  'textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

const pilhaDeDialogos: string[] = [];

export function Dialog({
  aberto,
  aoFechar,
  titulo,
  descricao,
  rodape,
  posicao = 'centro',
  className,
  children,
}: DialogProps) {
  const painel = useRef<HTMLDivElement>(null);
  const id = useId();
  const idTitulo = `${id}-titulo`;
  const idDescricao = `${id}-descricao`;
  const aoFecharRef = useRef(aoFechar);
  useEffect(() => {
    aoFecharRef.current = aoFechar;
  }, [aoFechar]);

  useEffect(() => {
    if (!aberto) return;
    pilhaDeDialogos.push(id);
    const anterior = document.activeElement as HTMLElement | null;
    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const elemento = painel.current;
    const primeiro = elemento?.querySelector<HTMLElement>(FOCAVEIS);
    (primeiro ?? elemento)?.focus();

    function aoTeclar(evento: KeyboardEvent) {
      if (pilhaDeDialogos[pilhaDeDialogos.length - 1] !== id) return;
      if (evento.key === 'Escape') {
        evento.preventDefault();
        aoFecharRef.current();
        return;
      }
      if (evento.key !== 'Tab' || !elemento) return;
      const focaveis = Array.from(elemento.querySelectorAll<HTMLElement>(FOCAVEIS));
      if (focaveis.length === 0) {
        evento.preventDefault();
        return;
      }
      const inicio = focaveis[0];
      const fim = focaveis[focaveis.length - 1];
      if (evento.shiftKey && document.activeElement === inicio) {
        evento.preventDefault();
        fim.focus();
      } else if (!evento.shiftKey && document.activeElement === fim) {
        evento.preventDefault();
        inicio.focus();
      }
    }

    document.addEventListener('keydown', aoTeclar);
    return () => {
      document.removeEventListener('keydown', aoTeclar);
      const indice = pilhaDeDialogos.lastIndexOf(id);
      if (indice >= 0) pilhaDeDialogos.splice(indice, 1);
      document.body.style.overflow = overflowAnterior;
      anterior?.focus();
    };
  }, [aberto, id]);

  if (!aberto) return null;

  const lateral = posicao === 'lateral';

  return createPortal(
    <div
      className={cn(
        'fixed inset-0 z-50 flex',
        lateral ? 'justify-end' : 'items-start justify-center p-4 sm:items-center',
      )}
    >
      <div className="fixed inset-0 bg-neutral-900/50" aria-hidden="true" onClick={aoFechar} />
      <div
        ref={painel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        aria-describedby={descricao ? idDescricao : undefined}
        tabIndex={-1}
        className={cn(
          'relative flex flex-col bg-white shadow-sm',
          lateral
            ? 'h-full w-full max-w-2xl border-l border-neutral-200'
            : 'max-h-[calc(100vh-2rem)] w-full max-w-lg rounded-md border border-neutral-200',
          className,
        )}
      >
        <div className="flex shrink-0 items-start justify-between gap-4 border-b border-neutral-200 px-5 py-4">
          <div>
            <h2 id={idTitulo} className="text-base font-semibold text-neutral-900">
              {titulo}
            </h2>
            {descricao && (
              <div id={idDescricao} className="mt-1 text-sm text-neutral-600">
                {descricao}
              </div>
            )}
          </div>
          <button
            type="button"
            onClick={aoFechar}
            className="rounded px-2 py-1 text-sm text-neutral-600 hover:bg-neutral-100"
          >
            Fechar
          </button>
        </div>
        {children && <div className="min-h-0 flex-1 overflow-y-auto px-5 py-4">{children}</div>}
        {rodape && (
          <div className="flex shrink-0 justify-end gap-2 border-t border-neutral-200 px-5 py-3">
            {rodape}
          </div>
        )}
      </div>
    </div>,
    document.body,
  );
}

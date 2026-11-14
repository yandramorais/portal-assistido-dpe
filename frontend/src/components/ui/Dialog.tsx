import { useEffect, useId, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

import { cn } from '../../lib/cn';

interface DialogProps {
  aberto: boolean;
  aoFechar: () => void;
  titulo: string;
  descricao?: ReactNode;
  /** Botões de ação, alinhados à direita. */
  rodape?: ReactNode;
  className?: string;
  children?: ReactNode;
}

const FOCAVEIS =
  'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), ' +
  'textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

/**
 * Diálogo modal acessível: foco preso dentro dele (Tab/Shift+Tab), fecha com Esc ou clique
 * no fundo, devolve o foco a quem o abriu e trava a rolagem da página.
 */
export function Dialog({
  aberto,
  aoFechar,
  titulo,
  descricao,
  rodape,
  className,
  children,
}: DialogProps) {
  const painel = useRef<HTMLDivElement>(null);
  const idTitulo = useId();
  const idDescricao = useId();
  // Mantém a função atual sem reexecutar o efeito de foco a cada render do pai.
  const aoFecharRef = useRef(aoFechar);
  useEffect(() => {
    aoFecharRef.current = aoFechar;
  }, [aoFechar]);

  useEffect(() => {
    if (!aberto) return;
    const anterior = document.activeElement as HTMLElement | null;
    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const elemento = painel.current;
    const primeiro = elemento?.querySelector<HTMLElement>(FOCAVEIS);
    (primeiro ?? elemento)?.focus();

    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        evento.stopPropagation();
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
      document.body.style.overflow = overflowAnterior;
      anterior?.focus();
    };
  }, [aberto]);

  if (!aberto) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-start justify-center p-4 sm:items-center">
      <div className="fixed inset-0 bg-neutral-900/50" aria-hidden="true" onClick={aoFechar} />
      <div
        ref={painel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        aria-describedby={descricao ? idDescricao : undefined}
        tabIndex={-1}
        className={cn(
          'relative w-full max-w-lg rounded-md border border-neutral-200 bg-white shadow-sm',
          className,
        )}
      >
        <div className="flex items-start justify-between gap-4 border-b border-neutral-200 px-5 py-4">
          <div>
            <h2 id={idTitulo} className="text-base font-semibold text-neutral-900">
              {titulo}
            </h2>
            {descricao && (
              <p id={idDescricao} className="mt-1 text-sm text-neutral-600">
                {descricao}
              </p>
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
        {children && <div className="px-5 py-4">{children}</div>}
        {rodape && (
          <div className="flex justify-end gap-2 border-t border-neutral-200 px-5 py-3">
            {rodape}
          </div>
        )}
      </div>
    </div>,
    document.body,
  );
}

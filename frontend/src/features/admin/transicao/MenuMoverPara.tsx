import { useEffect, useId, useRef, useState, type KeyboardEvent } from 'react';

import { cn } from '../../../lib/cn';
import { ROTULO_STATUS } from '../../solicitacoes/shared/rotulos';
import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';

interface MenuMoverParaProps {
  destinos: readonly StatusSolicitacao[];
  aoEscolher: (destino: StatusSolicitacao) => void;
  desabilitado?: boolean;
  rotuloAcessivel?: string;
  className?: string;
}

export function MenuMoverPara({
  destinos,
  aoEscolher,
  desabilitado,
  rotuloAcessivel,
  className,
}: MenuMoverParaProps) {
  const [aberto, setAberto] = useState(false);
  const idMenu = useId();
  const botao = useRef<HTMLButtonElement>(null);
  const menu = useRef<HTMLUListElement>(null);

  useEffect(() => {
    if (!aberto) return;
    menu.current?.querySelector<HTMLButtonElement>('[role="menuitem"]')?.focus();
    const aoClicarFora = (evento: MouseEvent) => {
      const alvo = evento.target as Node;
      if (!menu.current?.contains(alvo) && !botao.current?.contains(alvo)) setAberto(false);
    };
    document.addEventListener('mousedown', aoClicarFora);
    return () => document.removeEventListener('mousedown', aoClicarFora);
  }, [aberto]);

  const fechar = () => {
    setAberto(false);
    botao.current?.focus();
  };

  const navegar = (evento: KeyboardEvent<HTMLUListElement>) => {
    const itens = Array.from(
      menu.current?.querySelectorAll<HTMLButtonElement>('[role="menuitem"]') ?? [],
    );
    const atual = itens.indexOf(document.activeElement as HTMLButtonElement);
    if (evento.key === 'ArrowDown') {
      evento.preventDefault();
      itens[(atual + 1) % itens.length]?.focus();
    } else if (evento.key === 'ArrowUp') {
      evento.preventDefault();
      itens[(atual - 1 + itens.length) % itens.length]?.focus();
    } else if (evento.key === 'Home') {
      evento.preventDefault();
      itens[0]?.focus();
    } else if (evento.key === 'End') {
      evento.preventDefault();
      itens[itens.length - 1]?.focus();
    } else if (evento.key === 'Escape' || evento.key === 'Tab') {
      evento.preventDefault();
      evento.stopPropagation();
      fechar();
    }
  };

  if (destinos.length === 0) return null;

  return (
    <div
      className={cn('relative', className)}
      onClick={(evento) => evento.stopPropagation()}
      onPointerDown={(evento) => evento.stopPropagation()}
      onMouseDown={(evento) => evento.stopPropagation()}
      onTouchStart={(evento) => evento.stopPropagation()}
    >
      <button
        ref={botao}
        type="button"
        aria-haspopup="menu"
        aria-expanded={aberto}
        aria-controls={aberto ? idMenu : undefined}
        aria-label={rotuloAcessivel}
        disabled={desabilitado}
        onClick={() => setAberto((valor) => !valor)}
        onKeyDown={(evento) => {
          if (evento.key === 'ArrowDown') {
            evento.preventDefault();
            setAberto(true);
          }
        }}
        className="rounded border border-neutral-300 bg-white px-2 py-0.5 text-xs text-neutral-700 hover:bg-neutral-100 disabled:opacity-50"
      >
        Mover para…
      </button>
      {aberto && (
        <ul
          ref={menu}
          id={idMenu}
          role="menu"
          aria-label="Mover para"
          onKeyDown={navegar}
          className="absolute right-0 z-20 mt-1 min-w-48 rounded-md border border-neutral-200 bg-white py-1 shadow-sm"
        >
          {destinos.map((destino) => (
            <li key={destino} role="none">
              <button
                type="button"
                role="menuitem"
                tabIndex={-1}
                onClick={() => {
                  setAberto(false);
                  aoEscolher(destino);
                }}
                className="block w-full px-3 py-1.5 text-left text-sm text-neutral-800 hover:bg-neutral-100 focus:bg-neutral-100 focus:outline-none"
              >
                {ROTULO_STATUS[destino]}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

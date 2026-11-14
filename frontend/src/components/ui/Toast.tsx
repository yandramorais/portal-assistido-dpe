import { useCallback, useMemo, useRef, useState, type ReactNode } from 'react';

import { cn } from '../../lib/cn';
import { ToastContext, type NovoToast, type TipoToast } from './toast-context';

interface ToastAtivo {
  id: number;
  tipo: TipoToast;
  mensagem: string;
}

const DURACAO_MS = 5000;

const estilos: Record<TipoToast, string> = {
  sucesso: 'border-primary-200 bg-primary-50 text-primary-900',
  erro: 'border-danger-200 bg-danger-50 text-danger-700',
  info: 'border-neutral-200 bg-white text-neutral-800',
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastAtivo[]>([]);
  const proximoId = useRef(1);

  const fechar = useCallback((id: number) => {
    setToasts((atuais) => atuais.filter((t) => t.id !== id));
  }, []);

  const mostrar = useCallback(
    ({ tipo = 'info', mensagem }: NovoToast) => {
      const id = proximoId.current++;
      setToasts((atuais) => [...atuais, { id, tipo, mensagem }]);
      window.setTimeout(() => fechar(id), DURACAO_MS);
    },
    [fechar],
  );

  const valor = useMemo(() => ({ mostrar }), [mostrar]);

  return (
    <ToastContext.Provider value={valor}>
      {children}
      <div className="pointer-events-none fixed right-4 bottom-4 z-[60] flex w-full max-w-sm flex-col gap-2">
        <div aria-live="polite" className="flex flex-col gap-2">
          {toasts
            .filter((t) => t.tipo !== 'erro')
            .map((t) => (
              <ItemToast key={t.id} toast={t} aoFechar={fechar} />
            ))}
        </div>
        <div aria-live="assertive" className="flex flex-col gap-2">
          {toasts
            .filter((t) => t.tipo === 'erro')
            .map((t) => (
              <ItemToast key={t.id} toast={t} aoFechar={fechar} />
            ))}
        </div>
      </div>
    </ToastContext.Provider>
  );
}

function ItemToast({ toast, aoFechar }: { toast: ToastAtivo; aoFechar: (id: number) => void }) {
  return (
    <div
      role={toast.tipo === 'erro' ? 'alert' : 'status'}
      className={cn(
        'pointer-events-auto flex items-start justify-between gap-3 rounded-md border px-4 py-3 text-sm shadow-sm',
        estilos[toast.tipo],
      )}
    >
      <p>{toast.mensagem}</p>
      <button
        type="button"
        onClick={() => aoFechar(toast.id)}
        className="shrink-0 rounded px-1 text-xs font-medium underline-offset-2 hover:underline"
      >
        Fechar
      </button>
    </div>
  );
}

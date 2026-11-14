import { createContext } from 'react';

export type TipoToast = 'sucesso' | 'erro' | 'info';

export interface NovoToast {
  tipo?: TipoToast;
  mensagem: string;
}

export interface ToastContexto {
  mostrar: (toast: NovoToast) => void;
}

export const ToastContext = createContext<ToastContexto | null>(null);

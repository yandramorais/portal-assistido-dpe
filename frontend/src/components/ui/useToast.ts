import { useContext } from 'react';

import { ToastContext, type ToastContexto } from './toast-context';

/** Avisos rápidos: useToast().mostrar({ tipo: 'sucesso', mensagem: 'Pedido enviado.' }). */
export function useToast(): ToastContexto {
  const contexto = useContext(ToastContext);
  if (!contexto) {
    throw new Error('useToast precisa estar dentro de <ToastProvider>.');
  }
  return contexto;
}

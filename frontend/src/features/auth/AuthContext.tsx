import { useCallback, useEffect, useMemo, useSyncExternalStore, type ReactNode } from 'react';

import { expirado, lerClaims, usuarioDasClaims } from '../../lib/jwt';
import { queryClient } from '../../lib/queryClient';
import { sessao } from '../../lib/sessao';
import { AuthContext, type AuthContexto } from './auth-context';

export function AuthProvider({ children }: { children: ReactNode }) {
  const token = useSyncExternalStore(sessao.inscrever, sessao.obterToken);
  const claims = useMemo(() => (token ? lerClaims(token) : null), [token]);

  const sair = useCallback(() => {
    sessao.encerrar('saiu');
  }, []);

  const entrar = useCallback((novoToken: string) => {
    const novasClaims = lerClaims(novoToken);
    if (!novasClaims || expirado(novasClaims)) {
      throw new Error('Token recebido é inválido ou já expirou.');
    }
    sessao.iniciar(novoToken);
  }, []);

  useEffect(() => {
    if (!token) queryClient.clear();
  }, [token]);

  useEffect(() => {
    if (!claims) return;
    const restante = claims.exp * 1000 - Date.now();
    if (restante <= 0) {
      sessao.encerrar('expirou');
      return;
    }
    const temporizador = window.setTimeout(() => sessao.encerrar('expirou'), restante);
    return () => window.clearTimeout(temporizador);
  }, [claims]);

  const valor = useMemo<AuthContexto>(
    () => ({
      usuario: claims && !expirado(claims) ? usuarioDasClaims(claims) : null,
      sessaoExpirada: !token && sessao.motivoDoUltimoEncerramento() === 'expirou',
      entrar,
      sair,
    }),
    [claims, token, entrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

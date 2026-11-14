import { Navigate, Outlet, useLocation } from 'react-router-dom';

import { caminhoPermitido, paginaInicial } from '../features/auth/perfis';
import { useAuth } from '../features/auth/useAuth';

interface EstadoDeOrigem {
  de?: string;
}

/**
 * Telas de entrar e cadastro. Quem já está logado (inclusive logo após o login) segue para
 * a página de onde veio, se ela for da sua área, ou para a página inicial do perfil.
 */
export function SomenteVisitante() {
  const { usuario } = useAuth();
  const { state } = useLocation() as { state: EstadoDeOrigem | null };

  if (usuario) {
    const origem = state?.de;
    const destino =
      origem && caminhoPermitido(origem, usuario.perfil) ? origem : paginaInicial(usuario.perfil);
    return <Navigate to={destino} replace />;
  }
  return <Outlet />;
}

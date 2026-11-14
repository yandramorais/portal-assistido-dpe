import { Navigate, Outlet, useLocation } from 'react-router-dom';

import { paginaInicial } from '../features/auth/perfis';
import type { Perfil } from '../features/auth/types';
import { useAuth } from '../features/auth/useAuth';

interface RequireAuthProps {
  /** Perfis que podem ver as rotas filhas. Sem a lista, basta estar logado. */
  perfis?: readonly Perfil[];
}

/**
 * Sem login: manda para /entrar lembrando de onde veio.
 * Perfil errado: manda para a página inicial do próprio perfil.
 */
export function RequireAuth({ perfis }: RequireAuthProps) {
  const { usuario } = useAuth();
  const local = useLocation();

  if (!usuario) {
    return <Navigate to="/entrar" replace state={{ de: local.pathname + local.search }} />;
  }
  if (perfis && !perfis.includes(usuario.perfil)) {
    return <Navigate to={paginaInicial(usuario.perfil)} replace />;
  }
  return <Outlet />;
}

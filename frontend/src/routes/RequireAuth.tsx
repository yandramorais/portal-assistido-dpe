import { Navigate, Outlet, useLocation } from 'react-router-dom';

import { paginaInicial } from '../features/auth/perfis';
import type { Perfil } from '../features/auth/types';
import { useAuth } from '../features/auth/useAuth';

interface RequireAuthProps {
  perfis?: readonly Perfil[];
}

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

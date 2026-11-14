import { Navigate, Outlet, useLocation } from 'react-router-dom';

import { caminhoPermitido, paginaInicial } from '../features/auth/perfis';
import { useAuth } from '../features/auth/useAuth';

interface EstadoDeOrigem {
  de?: string;
}

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

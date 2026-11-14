import { Navigate } from 'react-router-dom';

import { paginaInicial } from '../features/auth/perfis';
import { useAuth } from '../features/auth/useAuth';

/** "/" leva cada um para o seu lugar: assistido aos pedidos, equipe à triagem, visitante ao login. */
export function RedirecionarInicio() {
  const { usuario } = useAuth();
  return <Navigate to={usuario ? paginaInicial(usuario.perfil) : '/entrar'} replace />;
}

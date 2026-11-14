import { createBrowserRouter } from 'react-router-dom';

import { AppLayout } from '../components/layout/AppLayout';
import { LoginPage } from '../features/auth/LoginPage';
import { PERFIS_ASSISTIDO, PERFIS_EQUIPE } from '../features/auth/perfis';
import { NaoEncontrada } from './NaoEncontrada';
import { PaginaProvisoria } from './PaginaProvisoria';
import { RedirecionarInicio } from './RedirecionarInicio';
import { RequireAuth } from './RequireAuth';
import { SomenteVisitante } from './SomenteVisitante';

export const router = createBrowserRouter([
  { path: '/', element: <RedirecionarInicio /> },
  {
    element: <AppLayout />,
    children: [
      {
        element: <SomenteVisitante />,
        children: [
          { path: '/entrar', element: <LoginPage /> },
          { path: '/cadastro', element: <PaginaProvisoria titulo="Criar cadastro" /> },
        ],
      },
      {
        element: <RequireAuth perfis={PERFIS_ASSISTIDO} />,
        children: [
          { path: '/meus-pedidos', element: <PaginaProvisoria titulo="Meus pedidos" /> },
          { path: '/pedidos/novo', element: <PaginaProvisoria titulo="Novo pedido" /> },
          { path: '/pedidos/:protocolo', element: <PaginaProvisoria titulo="Pedido" /> },
        ],
      },
      {
        element: <RequireAuth perfis={PERFIS_EQUIPE} />,
        children: [
          { path: '/admin/triagem', element: <PaginaProvisoria titulo="Triagem" /> },
          {
            path: '/admin/notificacoes',
            element: <PaginaProvisoria titulo="Notificações por e-mail" />,
          },
        ],
      },
      { path: '*', element: <NaoEncontrada /> },
    ],
  },
]);

import { createBrowserRouter } from 'react-router-dom';

import { AppLayout } from '../components/layout/AppLayout';
import { CadastroPage } from '../features/assistido/CadastroPage';
import { LoginPage } from '../features/auth/LoginPage';
import { PERFIS_ASSISTIDO, PERFIS_EQUIPE } from '../features/auth/perfis';
import { DetalhePedidoPage } from '../features/solicitacoes/detalhe/DetalhePedidoPage';
import { MeusPedidosPage } from '../features/solicitacoes/lista/MeusPedidosPage';
import { NovoPedidoPage } from '../features/solicitacoes/novo/NovoPedidoPage';
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
          { path: '/cadastro', element: <CadastroPage /> },
        ],
      },
      {
        element: <RequireAuth perfis={PERFIS_ASSISTIDO} />,
        children: [
          { path: '/meus-pedidos', element: <MeusPedidosPage /> },
          { path: '/pedidos/novo', element: <NovoPedidoPage /> },
          { path: '/pedidos/:protocolo', element: <DetalhePedidoPage /> },
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

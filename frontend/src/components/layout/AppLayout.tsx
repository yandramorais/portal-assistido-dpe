import { Outlet } from 'react-router-dom';

import { Cabecalho } from './Cabecalho';
import { Rodape } from './Rodape';

export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#conteudo"
        className="text-primary-800 sr-only z-50 rounded bg-white px-3 py-2 text-sm font-medium focus:not-sr-only focus:absolute focus:top-2 focus:left-2"
      >
        Pular para o conteúdo
      </a>
      <Cabecalho />
      <main
        id="conteudo"
        tabIndex={-1}
        className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 focus:outline-none"
      >
        <Outlet />
      </main>
      <Rodape />
    </div>
  );
}

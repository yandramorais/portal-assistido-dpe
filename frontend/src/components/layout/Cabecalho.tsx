import { Link, NavLink } from 'react-router-dom';

import { ehEquipe, paginaInicial, ROTULO_PERFIL } from '../../features/auth/perfis';
import type { UsuarioLogado } from '../../features/auth/types';
import { useAuth } from '../../features/auth/useAuth';
import { cn } from '../../lib/cn';

interface ItemMenu {
  para: string;
  rotulo: string;
}

const MENU_ASSISTIDO: ItemMenu[] = [
  { para: '/meus-pedidos', rotulo: 'Meus pedidos' },
  { para: '/pedidos/novo', rotulo: 'Novo pedido' },
];

const MENU_EQUIPE: ItemMenu[] = [
  { para: '/admin/triagem', rotulo: 'Triagem' },
  { para: '/admin/notificacoes', rotulo: 'Notificações' },
];

function menuDo(usuario: UsuarioLogado): ItemMenu[] {
  return ehEquipe(usuario.perfil) ? MENU_EQUIPE : MENU_ASSISTIDO;
}

export function Cabecalho() {
  const { usuario, sair } = useAuth();

  return (
    <header>
      <div className="border-b border-neutral-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-2 px-4 py-2">
          <Link
            to={usuario ? paginaInicial(usuario.perfil) : '/entrar'}
            className="flex items-center gap-3 rounded"
          >
            <img
              src="/dpe-logo.svg"
              alt="Defensoria Pública do Estado de Goiás"
              width={120}
              height={56}
              className="h-14 w-auto"
            />
            <span aria-hidden="true" className="h-8 border-l border-neutral-300" />
            <span className="text-primary-800 text-sm font-semibold sm:text-base">
              Portal do Assistido
            </span>
          </Link>

          {usuario && (
            <div className="flex items-center gap-3 text-sm">
              <span className="text-neutral-600">
                <span className="font-medium text-neutral-900">{usuario.nome}</span>
                <span className="sr-only">, perfil </span>
                <span aria-hidden="true"> · </span>
                {ROTULO_PERFIL[usuario.perfil]}
              </span>
              <button
                type="button"
                onClick={sair}
                className="rounded border border-neutral-300 px-3 py-1 text-sm text-neutral-800 hover:bg-neutral-100"
              >
                Sair
              </button>
            </div>
          )}
        </div>
      </div>

      {usuario && (
        <nav aria-label="Menu principal" className="border-primary-700 bg-primary-700 border-t">
          <ul className="mx-auto flex max-w-6xl gap-1 px-4">
            {menuDo(usuario).map((item) => (
              <li key={item.para}>
                <NavLink
                  to={item.para}
                  end
                  className={({ isActive }) =>
                    cn(
                      'block border-b-2 px-3 py-2 text-sm text-white',
                      isActive
                        ? 'border-white font-semibold'
                        : 'text-primary-50 hover:border-primary-300 border-transparent',
                    )
                  }
                >
                  {item.rotulo}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
      )}
    </header>
  );
}

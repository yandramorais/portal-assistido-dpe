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
    <header className="bg-primary-800 text-white">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-2 px-4 py-3">
        <Link
          to={usuario ? paginaInicial(usuario.perfil) : '/entrar'}
          className="text-sm font-semibold tracking-tight text-white sm:text-base"
        >
          Defensoria Pública do Estado de Goiás{' '}
          <span className="text-primary-100 font-normal">—</span>{' '}
          <span className="font-normal">Portal do Assistido</span>
        </Link>

        {usuario && (
          <div className="flex items-center gap-3 text-sm">
            <span className="text-primary-100">
              <span className="font-medium text-white">{usuario.nome}</span>
              <span className="sr-only">, perfil </span>
              <span aria-hidden="true"> · </span>
              {ROTULO_PERFIL[usuario.perfil]}
            </span>
            <button
              type="button"
              onClick={sair}
              className="border-primary-300/60 hover:bg-primary-700 rounded border px-3 py-1 text-sm text-white"
            >
              Sair
            </button>
          </div>
        )}
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

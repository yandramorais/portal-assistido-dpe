import { createContext } from 'react';

import type { UsuarioLogado } from './types';

export interface AuthContexto {
  usuario: UsuarioLogado | null;
  sessaoExpirada: boolean;
  entrar: (token: string) => void;
  sair: () => void;
}

export const AuthContext = createContext<AuthContexto | null>(null);

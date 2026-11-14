import { createContext } from 'react';

import type { UsuarioLogado } from './types';

export interface AuthContexto {
  /** Null quando ninguém está logado. */
  usuario: UsuarioLogado | null;
  /** Verdadeiro se a última sessão terminou por expiração (token vencido ou 401). */
  sessaoExpirada: boolean;
  /** Guarda o token recebido no login/cadastro. */
  entrar: (token: string) => void;
  sair: () => void;
}

export const AuthContext = createContext<AuthContexto | null>(null);

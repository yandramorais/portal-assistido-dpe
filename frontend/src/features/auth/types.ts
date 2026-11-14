export type Perfil = 'ASSISTIDO' | 'SERVIDOR' | 'DEFENSOR';

/** Usuário logado, montado a partir das claims do JWT. */
export interface UsuarioLogado {
  id: number;
  nome: string;
  perfil: Perfil;
}

/** Resposta de POST /api/auth/login e /api/auth/registro. */
export interface TokenResponse {
  token: string;
  tipo: 'Bearer';
  expiraEm: string;
  usuario: UsuarioLogado;
}

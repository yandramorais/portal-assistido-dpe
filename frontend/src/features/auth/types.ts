export type Perfil = 'ASSISTIDO' | 'SERVIDOR' | 'DEFENSOR';

export interface UsuarioLogado {
  id: number;
  nome: string;
  perfil: Perfil;
}

export interface TokenResponse {
  token: string;
  tipo: 'Bearer';
  expiraEm: string;
  usuario: UsuarioLogado;
}

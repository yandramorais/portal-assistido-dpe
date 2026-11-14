import type { Perfil } from './types';

export const PERFIS_EQUIPE: readonly Perfil[] = ['SERVIDOR', 'DEFENSOR'];
export const PERFIS_ASSISTIDO: readonly Perfil[] = ['ASSISTIDO'];

export function ehEquipe(perfil: Perfil): boolean {
  return PERFIS_EQUIPE.includes(perfil);
}

/** Para onde cada perfil vai ao entrar ou ao tentar abrir uma área que não é sua. */
export function paginaInicial(perfil: Perfil): string {
  return ehEquipe(perfil) ? '/admin/triagem' : '/meus-pedidos';
}

export const ROTULO_PERFIL: Record<Perfil, string> = {
  ASSISTIDO: 'Assistido',
  SERVIDOR: 'Servidor',
  DEFENSOR: 'Defensor',
};

/** O caminho pertence à área deste perfil? Usado para voltar à página de origem após o login. */
export function caminhoPermitido(caminho: string, perfil: Perfil): boolean {
  if (caminho.startsWith('/admin')) return ehEquipe(perfil);
  if (caminho.startsWith('/meus-pedidos') || caminho.startsWith('/pedidos'))
    return !ehEquipe(perfil);
  return false;
}

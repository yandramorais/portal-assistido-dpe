import type { Perfil, UsuarioLogado } from '../features/auth/types';

/** Claims emitidas pelo backend (JwtService). */
export interface ClaimsPortal {
  sub: string;
  nome: string;
  perfil: Perfil;
  /** Expiração em segundos desde 1970 (padrão JWT). */
  exp: number;
  iat?: number;
}

const PERFIS: readonly Perfil[] = ['ASSISTIDO', 'SERVIDOR', 'DEFENSOR'];

/**
 * Lê o payload do JWT. Não confere a assinatura: quem valida o token é o backend.
 * Devolve null se o token estiver mal formado ou sem as claims esperadas.
 */
export function lerClaims(token: string): ClaimsPortal | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')), (c) =>
      c.charCodeAt(0),
    );
    // TextDecoder para os acentos dos nomes (o payload é UTF-8).
    const claims = JSON.parse(new TextDecoder().decode(bytes)) as Partial<ClaimsPortal>;
    if (
      typeof claims.sub !== 'string' ||
      typeof claims.nome !== 'string' ||
      typeof claims.exp !== 'number' ||
      !PERFIS.includes(claims.perfil as Perfil)
    ) {
      return null;
    }
    return claims as ClaimsPortal;
  } catch {
    return null;
  }
}

export function expirado(claims: ClaimsPortal, agoraMs: number = Date.now()): boolean {
  return claims.exp * 1000 <= agoraMs;
}

export function usuarioDasClaims(claims: ClaimsPortal): UsuarioLogado {
  return { id: Number(claims.sub), nome: claims.nome, perfil: claims.perfil };
}

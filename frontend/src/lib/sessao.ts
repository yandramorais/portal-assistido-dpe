import { expirado, lerClaims } from './jwt';

/**
 * Guarda o token da sessão: em memória e no sessionStorage (some ao fechar a aba).
 * Fica fora do React para o api.ts ler o token sem depender da ordem de montagem
 * dos componentes; o AuthContext assina as mudanças.
 */

export type MotivoEncerramento = 'saiu' | 'expirou';

type Ouvinte = () => void;

const CHAVE = 'portal-assistido.token';

function carregarSalvo(): string | null {
  try {
    const salvo = sessionStorage.getItem(CHAVE);
    if (!salvo) return null;
    const claims = lerClaims(salvo);
    if (!claims || expirado(claims)) {
      sessionStorage.removeItem(CHAVE);
      return null;
    }
    return salvo;
  } catch {
    // sessionStorage indisponível (modo privado restrito etc.): segue só em memória.
    return null;
  }
}

let token: string | null = carregarSalvo();
let ultimoMotivo: MotivoEncerramento | null = null;
const ouvintes = new Set<Ouvinte>();

function avisar() {
  ouvintes.forEach((ouvinte) => ouvinte());
}

export const sessao = {
  obterToken(): string | null {
    return token;
  },

  /** Motivo do último encerramento, para a tela de login explicar ("sua sessão expirou"). */
  motivoDoUltimoEncerramento(): MotivoEncerramento | null {
    return ultimoMotivo;
  },

  iniciar(novoToken: string) {
    token = novoToken;
    ultimoMotivo = null;
    try {
      sessionStorage.setItem(CHAVE, novoToken);
    } catch {
      // Sem storage: a sessão vale só enquanto a página estiver aberta.
    }
    avisar();
  },

  encerrar(motivo: MotivoEncerramento) {
    if (token === null) return;
    token = null;
    ultimoMotivo = motivo;
    try {
      sessionStorage.removeItem(CHAVE);
    } catch {
      // Nada a limpar.
    }
    avisar();
  },

  inscrever(ouvinte: Ouvinte): () => void {
    ouvintes.add(ouvinte);
    return () => ouvintes.delete(ouvinte);
  },
};

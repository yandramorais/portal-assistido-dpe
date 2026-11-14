import { expirado, lerClaims } from './jwt';

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

  motivoDoUltimoEncerramento(): MotivoEncerramento | null {
    return ultimoMotivo;
  },

  iniciar(novoToken: string) {
    token = novoToken;
    ultimoMotivo = null;
    try {
      sessionStorage.setItem(CHAVE, novoToken);
    } catch {}
    avisar();
  },

  encerrar(motivo: MotivoEncerramento) {
    if (token === null) return;
    token = null;
    ultimoMotivo = motivo;
    try {
      sessionStorage.removeItem(CHAVE);
    } catch {}
    avisar();
  },

  inscrever(ouvinte: Ouvinte): () => void {
    ouvintes.add(ouvinte);
    return () => ouvintes.delete(ouvinte);
  },
};

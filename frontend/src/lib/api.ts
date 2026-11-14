import { sessao } from './sessao';

/**
 * Único ponto de chamadas HTTP do front. Envia o token, converte o ProblemDetail
 * (RFC 9457) do backend em ApiError e encerra a sessão quando o token deixa de valer (401).
 */

const BASE = '/api';

export interface ErroCampo {
  campo: string;
  mensagem: string;
}

interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  requestId?: string;
  erros?: ErroCampo[];
  campos?: string[];
}

export class ApiError extends Error {
  readonly status: number;
  readonly titulo: string;
  /** Código da requisição para o usuário informar ao suporte. */
  readonly requestId?: string;
  /** Erros de validação por campo (400). */
  readonly erros: ErroCampo[];
  /** Campos que faltam numa mudança de status (422). */
  readonly campos: string[];

  constructor(dados: {
    status: number;
    titulo: string;
    detalhe: string;
    requestId?: string;
    erros?: ErroCampo[];
    campos?: string[];
  }) {
    super(dados.detalhe);
    this.name = 'ApiError';
    this.status = dados.status;
    this.titulo = dados.titulo;
    this.requestId = dados.requestId;
    this.erros = dados.erros ?? [];
    this.campos = dados.campos ?? [];
  }

  /** Mensagem do campo vinda do servidor, para mostrar junto ao campo no formulário. */
  erroDoCampo(campo: string): string | undefined {
    return this.erros.find((e) => e.campo === campo)?.mensagem;
  }
}

const MENSAGEM_POR_STATUS: Record<number, string> = {
  400: 'Verifique os dados informados.',
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para acessar este recurso.',
  404: 'Não encontrado.',
  409: 'Os dados foram alterados por outra pessoa. Recarregue a página.',
  422: 'Não foi possível concluir a operação.',
  500: 'Erro no servidor. Tente novamente em instantes.',
  502: 'O servidor está indisponível no momento. Tente novamente em instantes.',
  503: 'O servidor está indisponível no momento. Tente novamente em instantes.',
};

async function paraApiError(resposta: Response): Promise<ApiError> {
  let corpo: ProblemDetail = {};
  try {
    corpo = (await resposta.json()) as ProblemDetail;
  } catch {
    // Corpo vazio ou não JSON (ex.: erro do proxy).
  }
  return new ApiError({
    status: resposta.status,
    titulo: corpo.title ?? 'Erro',
    detalhe:
      corpo.detail ??
      MENSAGEM_POR_STATUS[resposta.status] ??
      'Não foi possível concluir a operação.',
    requestId: corpo.requestId ?? resposta.headers.get('X-Request-Id') ?? undefined,
    erros: corpo.erros,
    campos: corpo.campos,
  });
}

interface Opcoes {
  signal?: AbortSignal;
}

async function requisicao<T>(
  metodo: string,
  caminho: string,
  corpo?: unknown,
  opcoes: Opcoes = {},
): Promise<T> {
  const token = sessao.obterToken();
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (corpo !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let resposta: Response;
  try {
    resposta = await fetch(`${BASE}${caminho}`, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
      signal: opcoes.signal,
    });
  } catch (erro) {
    if (erro instanceof DOMException && erro.name === 'AbortError') throw erro;
    throw new ApiError({
      status: 0,
      titulo: 'Sem conexão',
      detalhe: 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.',
    });
  }

  // 401 com token = token vencido ou inválido: encerra a sessão (as rotas levam ao /entrar).
  // Sem token (ex.: senha errada no login) o 401 é só um erro comum.
  if (resposta.status === 401 && token) {
    sessao.encerrar('expirou');
  }
  if (!resposta.ok) {
    throw await paraApiError(resposta);
  }
  if (resposta.status === 204) {
    return undefined as T;
  }
  const texto = await resposta.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}

export const api = {
  get: <T>(caminho: string, opcoes?: Opcoes) => requisicao<T>('GET', caminho, undefined, opcoes),
  post: <T>(caminho: string, corpo?: unknown, opcoes?: Opcoes) =>
    requisicao<T>('POST', caminho, corpo, opcoes),
  put: <T>(caminho: string, corpo?: unknown, opcoes?: Opcoes) =>
    requisicao<T>('PUT', caminho, corpo, opcoes),
  patch: <T>(caminho: string, corpo?: unknown, opcoes?: Opcoes) =>
    requisicao<T>('PATCH', caminho, corpo, opcoes),
  delete: <T>(caminho: string, opcoes?: Opcoes) =>
    requisicao<T>('DELETE', caminho, undefined, opcoes),
};

/**
 * Texto para mostrar ao usuário. Inclui o código da requisição quando houver, para ele
 * informar à Central de Informações.
 */
export function mensagemDoErro(erro: unknown): string {
  if (erro instanceof ApiError) {
    if (erro.requestId && !erro.message.includes(erro.requestId)) {
      return `${erro.message} Código para o suporte: ${erro.requestId}.`;
    }
    return erro.message;
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

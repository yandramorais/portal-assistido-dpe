import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ToastProvider } from '../../../components/ui';
import { NovoPedidoPage } from './NovoPedidoPage';

const UNIDADES = [
  {
    id: 1,
    nome: 'Sede Administrativa e Atendimento',
    comarca: 'Goiânia',
    endereco: null,
    telefone: null,
  },
  { id: 3, nome: 'Unidade Anápolis', comarca: 'Anápolis', endereco: null, telefone: null },
];

function resposta(corpo: unknown, status = 200) {
  return new Response(JSON.stringify(corpo), {
    status,
    headers: { 'Content-Type': 'application/json', 'X-Request-Id': 'teste-123' },
  });
}

let respostaDoEnvio: () => Response;
const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
  if (url === '/api/unidades') return resposta(UNIDADES);
  if (url === '/api/solicitacoes' && init?.method === 'POST') return respostaDoEnvio();
  return resposta({ title: 'Não encontrado', status: 404 }, 404);
});

function renderizar() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <MemoryRouter>
          <NovoPedidoPage />
        </MemoryRouter>
      </ToastProvider>
    </QueryClientProvider>,
  );
}

function chamadasDeEnvio() {
  return fetchMock.mock.calls.filter(([, init]) => init?.method === 'POST');
}

async function esperarFormulario() {
  return screen.findByRole('combobox', { name: /^Área/ });
}

describe('Novo pedido', () => {
  beforeEach(() => {
    fetchMock.mockClear();
    vi.stubGlobal('fetch', fetchMock);
    respostaDoEnvio = () =>
      resposta(
        {
          protocolo: 'DPE-2026-000099',
          assunto: 'Pensão alimentícia',
          status: 'AGUARDANDO_TRIAGEM',
        },
        201,
      );
  });

  it('mostra as mensagens de validação em português e não chama a API', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await esperarFormulario();

    await usuario.click(screen.getByRole('button', { name: 'Enviar pedido' }));

    expect(await screen.findByText('Escolha a área do seu pedido.')).toBeInTheDocument();
    expect(screen.getByText('Informe o assunto em poucas palavras.')).toBeInTheDocument();
    expect(screen.getByText('Conte o que aconteceu.')).toBeInTheDocument();
    expect(screen.getByText('Escolha a unidade onde quer ser atendido.')).toBeInTheDocument();
    expect(screen.getByText('Escolha como prefere ser atendido.')).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: /^Área/ })).toHaveAttribute('aria-invalid', 'true');
    expect(chamadasDeEnvio()).toHaveLength(0);
  });

  it('sugere assuntos conforme a área e conta os caracteres do relato', async () => {
    const usuario = userEvent.setup();
    const { container } = renderizar();
    await esperarFormulario();

    const sugestoes = () =>
      Array.from(container.querySelectorAll('#sugestoes-de-assunto option')).map((o) =>
        o.getAttribute('value'),
      );
    expect(sugestoes()).toEqual([]);

    await usuario.selectOptions(screen.getByRole('combobox', { name: /^Área/ }), 'FAMILIA');
    expect(sugestoes()).toContain('Pensão alimentícia');

    await usuario.selectOptions(screen.getByRole('combobox', { name: /^Área/ }), 'SAUDE');
    expect(sugestoes()).toContain('Fornecimento de medicamento');
    expect(sugestoes()).not.toContain('Pensão alimentícia');

    await usuario.type(screen.getByLabelText(/^O que aconteceu/), 'Remédio em falta');
    expect(screen.getByText(/^16 de 5\.000 caracteres/)).toBeInTheDocument();
  });

  it('envia o pedido com os dados certos e mostra o protocolo', async () => {
    const usuario = userEvent.setup();
    renderizar();
    await esperarFormulario();

    await usuario.selectOptions(screen.getByRole('combobox', { name: /^Área/ }), 'FAMILIA');
    await usuario.type(screen.getByLabelText(/^Assunto/), 'Pensão alimentícia');
    await usuario.type(
      screen.getByLabelText(/^O que aconteceu/),
      'O pai das crianças parou de pagar a pensão.',
    );
    await usuario.selectOptions(
      screen.getByRole('combobox', { name: /^Unidade de atendimento/ }),
      '3',
    );
    await usuario.click(screen.getByRole('radio', { name: /Virtual/ }));
    await usuario.click(screen.getByRole('checkbox', { name: /Documento de identificação/ }));
    await usuario.click(screen.getByRole('checkbox', { name: /Certidão de nascimento/ }));
    expect(
      screen.getByText('Mesmo sem todos os documentos você pode enviar o pedido.'),
    ).toBeInTheDocument();

    await usuario.click(screen.getByRole('button', { name: 'Enviar pedido' }));

    expect(await screen.findByRole('heading', { name: 'Pedido enviado' })).toBeInTheDocument();
    expect(screen.getByText('DPE-2026-000099')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Acompanhar este pedido' })).toHaveAttribute(
      'href',
      '/pedidos/DPE-2026-000099',
    );

    const [[url, init]] = chamadasDeEnvio();
    expect(url).toBe('/api/solicitacoes');
    expect(JSON.parse(init!.body as string)).toEqual({
      unidadeId: 3,
      area: 'FAMILIA',
      assunto: 'Pensão alimentícia',
      relato: 'O pai das crianças parou de pagar a pensão.',
      modalidade: 'VIRTUAL',
      documentosEntregues: ['DOCUMENTO_IDENTIFICACAO', 'CERTIDAO_CIVIL'],
    });
  });

  it('mostra no campo o erro de validação devolvido pela API', async () => {
    respostaDoEnvio = () =>
      resposta(
        {
          title: 'Dados inválidos',
          status: 400,
          detail: 'Verifique os campos informados.',
          requestId: 'abc123',
          erros: [{ campo: 'assunto', mensagem: 'tamanho deve ser entre 0 e 150' }],
        },
        400,
      );
    const usuario = userEvent.setup();
    renderizar();
    await esperarFormulario();

    await usuario.selectOptions(screen.getByRole('combobox', { name: /^Área/ }), 'SAUDE');
    await usuario.type(screen.getByLabelText(/^Assunto/), 'Fornecimento de medicamento');
    await usuario.type(screen.getByLabelText(/^O que aconteceu/), 'Falta remédio no posto.');
    await usuario.selectOptions(
      screen.getByRole('combobox', { name: /^Unidade de atendimento/ }),
      '1',
    );
    await usuario.click(screen.getByRole('radio', { name: /Presencial/ }));
    await usuario.click(screen.getByRole('button', { name: 'Enviar pedido' }));

    const campo = screen.getByLabelText(/^Assunto/);
    await waitFor(() => expect(campo).toHaveAttribute('aria-invalid', 'true'));
    expect(screen.getByText('tamanho deve ser entre 0 e 150')).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Pedido enviado' })).not.toBeInTheDocument();
    expect(within(document.body).queryByText(/Código para o suporte/)).not.toBeInTheDocument();
  });

  it('mostra erro com o código para o suporte se as unidades não carregarem', async () => {
    fetchMock.mockImplementationOnce(async () =>
      resposta(
        { title: 'Erro interno', status: 500, detail: 'Falhou.', requestId: 'req-500' },
        500,
      ),
    );
    renderizar();

    const alerta = await screen.findByRole('alert');
    expect(alerta).toHaveTextContent('Não foi possível carregar as unidades de atendimento.');
    expect(alerta).toHaveTextContent('Código para o suporte: req-500.');
    expect(within(alerta).getByRole('button', { name: 'Tentar novamente' })).toBeInTheDocument();
  });
});

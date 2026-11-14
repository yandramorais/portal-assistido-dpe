import type {
  AreaAtuacao,
  Modalidade,
  StatusSolicitacao,
  TipoDocumento,
  Vulnerabilidade,
} from './tipos';

/** Mesma ordem e descrições do enum AreaAtuacao do backend. */
export const AREAS: ReadonlyArray<{ valor: AreaAtuacao; rotulo: string }> = [
  { valor: 'FAMILIA', rotulo: 'Família' },
  { valor: 'CIVEL', rotulo: 'Cível' },
  { valor: 'CONSUMIDOR', rotulo: 'Consumidor' },
  { valor: 'SAUDE', rotulo: 'Saúde' },
  { valor: 'MORADIA', rotulo: 'Moradia' },
  { valor: 'REGISTRO_CIVIL', rotulo: 'Registro civil' },
  { valor: 'INFANCIA_JUVENTUDE', rotulo: 'Infância e juventude' },
  { valor: 'DEFESA_MULHER', rotulo: 'Defesa da mulher' },
  { valor: 'IDOSO_PCD', rotulo: 'Pessoa idosa e pessoa com deficiência' },
];

export const ROTULO_AREA: Record<AreaAtuacao, string> = Object.fromEntries(
  AREAS.map((a) => [a.valor, a.rotulo]),
) as Record<AreaAtuacao, string>;

/** Sugestões de assunto por área (o assistido pode escrever outro). */
export const ASSUNTOS_POR_AREA: Record<AreaAtuacao, readonly string[]> = {
  FAMILIA: [
    'Pensão alimentícia',
    'Revisão de pensão',
    'Guarda compartilhada',
    'Divórcio litigioso',
    'Reconhecimento de paternidade',
    'Curatela',
  ],
  CIVEL: [
    'Usucapião',
    'Benefício previdenciário (INSS)',
    'Revisão de contrato de financiamento',
    'Cobrança de dívida',
  ],
  CONSUMIDOR: [
    'Negativação indevida',
    'Cobrança abusiva',
    'Produto com defeito',
    'Serviço não prestado',
  ],
  SAUDE: [
    'Fornecimento de medicamento',
    'Cirurgia ortopédica',
    'Vaga em UTI',
    'Consulta ou exame pelo SUS',
  ],
  MORADIA: ['Ação de despejo', 'Regularização de imóvel'],
  REGISTRO_CIVIL: [
    'Segunda via de certidão de nascimento',
    'Registro tardio',
    'Alteração de nome e gênero',
  ],
  INFANCIA_JUVENTUDE: ['Vaga em creche', 'Matrícula escolar'],
  DEFESA_MULHER: ['Medida protetiva', 'Violência doméstica'],
  IDOSO_PCD: [
    'Benefício de Prestação Continuada',
    'Atendimento prioritário em banco',
    'Acessibilidade',
  ],
};

export const ROTULO_STATUS: Record<StatusSolicitacao, string> = {
  AGUARDANDO_TRIAGEM: 'Aguardando triagem',
  EM_ANALISE: 'Em análise',
  PENDENTE_DOCUMENTACAO: 'Pendente de documentação',
  DEFERIDO: 'Deferido',
  ENCAMINHADO: 'Encaminhado',
  INDEFERIDO: 'Indeferido',
  CONCLUIDO: 'Concluído',
};

export const ROTULO_MODALIDADE: Record<Modalidade, string> = {
  PRESENCIAL: 'Presencial',
  VIRTUAL: 'Virtual (por vídeo)',
};

export const DOCUMENTOS: ReadonlyArray<{ valor: TipoDocumento; rotulo: string; dica: string }> = [
  {
    valor: 'DOCUMENTO_IDENTIFICACAO',
    rotulo: 'Documento de identificação com foto',
    dica: 'RG, CNH ou carteira de trabalho',
  },
  {
    valor: 'COMPROVANTE_RESIDENCIA',
    rotulo: 'Comprovante de residência',
    dica: 'Conta de água, luz ou telefone recente',
  },
  {
    valor: 'COMPROVANTE_RENDA',
    rotulo: 'Comprovante de renda',
    dica: 'Contracheque, extrato do benefício ou declaração',
  },
  {
    valor: 'CERTIDAO_CIVIL',
    rotulo: 'Certidão de nascimento ou casamento',
    dica: 'Sua ou das pessoas envolvidas no caso',
  },
  {
    valor: 'DOCUMENTOS_DO_CASO',
    rotulo: 'Documentos do caso',
    dica: 'Contratos, receitas, notificações, conversas',
  },
];

export const ROTULO_DOCUMENTO: Record<TipoDocumento, string> = Object.fromEntries(
  DOCUMENTOS.map((d) => [d.valor, d.rotulo]),
) as Record<TipoDocumento, string>;

export const VULNERABILIDADES: ReadonlyArray<{ valor: Vulnerabilidade; rotulo: string }> = [
  { valor: 'PESSOA_COM_DEFICIENCIA', rotulo: 'Pessoa com deficiência' },
  { valor: 'VIOLENCIA_DOMESTICA', rotulo: 'Situação de violência doméstica' },
  { valor: 'DOENCA_GRAVE', rotulo: 'Doença grave' },
  { valor: 'SITUACAO_DE_RUA', rotulo: 'Situação de rua' },
];

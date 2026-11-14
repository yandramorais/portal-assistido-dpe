import type { StatusSolicitacao } from '../../solicitacoes/shared/tipos';

export const MODELO_DE_DEVOLUTIVA: Record<StatusSolicitacao, string> = {
  AGUARDANDO_TRIAGEM: '',
  EM_ANALISE: '',
  PENDENTE_DOCUMENTACAO:
    'Olá! Para seguir com seu pedido, precisamos do comprovante de renda (contracheque ou extrato do benefício) e dos documentos relacionados ao caso. Se não tiver algum deles, responda esta mensagem explicando.',
  DEFERIDO:
    'Seu atendimento foi aprovado e agendado. Confira a data e o local no seu pedido. Leve um documento com foto.',
  ENCAMINHADO:
    'Seu caso não é de atribuição da Defensoria Pública do Estado e foi encaminhado. Veja o destino e as orientações no seu pedido.',
  INDEFERIDO:
    'Após análise, o atendimento não pôde ser deferido. O motivo está no seu pedido. Você pode pedir a revisão da decisão respondendo esta mensagem.',
  CONCLUIDO: 'Seu atendimento foi concluído. Obrigado por procurar a Defensoria Pública.',
};

export const SUGESTOES_DE_DESTINO = [
  'Defensoria Pública da União (DPU)',
  'Procon Goiás',
  'Juizado Especial Cível',
  'Ministério Público de Goiás',
  'Instituto Nacional do Seguro Social (INSS)',
  'Conselho Tutelar',
  'Centro de Referência de Assistência Social (CRAS)',
] as const;

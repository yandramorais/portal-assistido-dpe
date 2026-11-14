import { Badge } from '../../../components/ui';
import type { TomBadge as Tom } from '../../../components/ui/Badge';
import { ROTULO_STATUS } from './rotulos';
import type { StatusSolicitacao } from './tipos';

/**
 * Um único tom de alerta (perigo) e só para o que pede ação do assistido.
 * Verde para andamento e resultado favorável; cinza para o resto.
 */
const TOM_POR_STATUS: Record<StatusSolicitacao, Tom> = {
  AGUARDANDO_TRIAGEM: 'contorno',
  EM_ANALISE: 'primario',
  PENDENTE_DOCUMENTACAO: 'perigo',
  DEFERIDO: 'destaque',
  ENCAMINHADO: 'neutro',
  INDEFERIDO: 'neutro',
  CONCLUIDO: 'primario',
};

export function StatusBadge({ status }: { status: StatusSolicitacao }) {
  return <Badge tom={TOM_POR_STATUS[status]}>{ROTULO_STATUS[status]}</Badge>;
}

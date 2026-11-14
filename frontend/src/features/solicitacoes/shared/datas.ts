import { format, formatDistanceToNow } from 'date-fns';
import { ptBR } from 'date-fns/locale';

/** "há 3 dias" */
export function tempoRelativo(iso: string): string {
  return formatDistanceToNow(new Date(iso), { addSuffix: true, locale: ptBR });
}

/** "12/11/2026 às 14:30" */
export function dataHora(iso: string): string {
  return format(new Date(iso), "dd/MM/yyyy 'às' HH:mm", { locale: ptBR });
}

/** "quinta-feira, 20 de novembro de 2026" */
export function dataPorExtenso(iso: string): string {
  return format(new Date(iso), "EEEE, d 'de' MMMM 'de' yyyy", { locale: ptBR });
}

/** "09:00" */
export function hora(iso: string): string {
  return format(new Date(iso), 'HH:mm', { locale: ptBR });
}

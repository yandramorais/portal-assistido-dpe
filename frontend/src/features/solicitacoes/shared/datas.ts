import { format, formatDistanceToNow } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export function tempoRelativo(iso: string): string {
  return formatDistanceToNow(new Date(iso), { addSuffix: true, locale: ptBR });
}

export function dataHora(iso: string): string {
  return format(new Date(iso), "dd/MM/yyyy 'às' HH:mm", { locale: ptBR });
}

export function dataPorExtenso(iso: string): string {
  return format(new Date(iso), "EEEE, d 'de' MMMM 'de' yyyy", { locale: ptBR });
}

export function hora(iso: string): string {
  return format(new Date(iso), 'HH:mm', { locale: ptBR });
}

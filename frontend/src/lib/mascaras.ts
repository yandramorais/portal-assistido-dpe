/** Máscaras de digitação (sem biblioteca). Sempre envie ao backend só os dígitos. */

export function apenasDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

/** 000.000.000-00 */
export function mascararCpf(valor: string): string {
  const d = apenasDigitos(valor).slice(0, 11);
  return d
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2');
}

/** (62) 99999-9999 ou (62) 3602-1224 */
export function mascararTelefone(valor: string): string {
  const d = apenasDigitos(valor).slice(0, 11);
  if (d.length <= 2) return d.length ? `(${d}` : '';
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}

/** 00000-000 */
export function mascararCep(valor: string): string {
  const d = apenasDigitos(valor).slice(0, 8);
  return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
}

const VALOR_MONETARIO = /^(\d{1,3}(\.\d{3})+|\d+)(,\d{1,2})?$/;

/** Aceita "1800", "1800,5", "1.800,50". */
export function valorMonetarioValido(valor: string): boolean {
  return VALOR_MONETARIO.test(valor.trim());
}

/** "1.800,50" -> 1800.5 */
export function paraNumero(valor: string): number {
  return Number(valor.trim().replace(/\./g, '').replace(',', '.'));
}

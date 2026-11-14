import { apenasDigitos } from './mascaras';

/**
 * Mesmo cálculo do backend (CpfValidator): 11 dígitos, não todos iguais e os dois
 * dígitos verificadores corretos. Aceita com ou sem máscara.
 */
export function cpfValido(valor: string): boolean {
  const cpf = apenasDigitos(valor);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
  return (
    digitoVerificador(cpf, 9) === Number(cpf[9]) && digitoVerificador(cpf, 10) === Number(cpf[10])
  );
}

function digitoVerificador(cpf: string, quantidade: number): number {
  let soma = 0;
  for (let i = 0; i < quantidade; i++) {
    soma += Number(cpf[i]) * (quantidade + 1 - i);
  }
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

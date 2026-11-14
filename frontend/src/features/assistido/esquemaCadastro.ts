import { zodResolver } from '@hookform/resolvers/zod';
import type { Resolver } from 'react-hook-form';
import { z } from 'zod';

import { cpfValido } from '../../lib/cpf';
import { apenasDigitos, paraNumero, valorMonetarioValido } from '../../lib/mascaras';
import type { Vulnerabilidade } from '../solicitacoes/shared/tipos';

/**
 * Validação do cadastro, espelhando o RegistroRequest do backend (tamanhos, CPF com dígito
 * verificador, data no passado, senha de 8 a 72). Um esquema por passo, para validar só o
 * que está na tela; os dados dos outros passos continuam no mesmo formulário.
 */

const ate = (maximo: number) =>
  z.string().trim().max(maximo, `Use no máximo ${maximo} caracteres.`);

function hojeIso(): string {
  const agora = new Date();
  return new Date(agora.getTime() - agora.getTimezoneOffset() * 60_000).toISOString().slice(0, 10);
}

export const esquemaPasso1 = z
  .object({
    nome: z
      .string()
      .trim()
      .min(1, 'Informe seu nome completo.')
      .max(150, 'Use no máximo 150 caracteres.'),
    nomeSocial: ate(150),
    cpf: z.string().min(1, 'Informe o CPF.').refine(cpfValido, 'CPF inválido. Confira os números.'),
    dataNascimento: z
      .string()
      .min(1, 'Informe a data de nascimento.')
      .refine((data) => data < hojeIso(), 'A data de nascimento deve ser anterior a hoje.'),
    telefone: z
      .string()
      .min(1, 'Informe um telefone para contato.')
      .refine((t) => [10, 11].includes(apenasDigitos(t).length), 'Informe o telefone com DDD.'),
    email: z
      .string()
      .trim()
      .min(1, 'Informe o e-mail.')
      .email('Informe um e-mail válido.')
      .max(150, 'Use no máximo 150 caracteres.'),
    senha: z
      .string()
      .min(8, 'A senha deve ter pelo menos 8 caracteres.')
      .max(72, 'A senha deve ter no máximo 72 caracteres.'),
    confirmacaoSenha: z.string().min(1, 'Repita a senha.'),
  })
  .refine((d) => d.senha === d.confirmacaoSenha, {
    path: ['confirmacaoSenha'],
    message: 'As senhas não conferem.',
  });

export const esquemaPasso2 = z.object({
  cep: z
    .string()
    .refine((c) => c === '' || apenasDigitos(c).length === 8, 'Informe o CEP com 8 números.'),
  logradouro: ate(150),
  numero: ate(10),
  complemento: ate(80),
  bairro: ate(80),
  cidade: z.string().trim().min(1, 'Informe a cidade.').max(80, 'Use no máximo 80 caracteres.'),
  uf: z.string().regex(/^[A-Z]{2}$/, 'Selecione o estado.'),
});

const valor = (obrigatorio: boolean) =>
  z
    .string()
    .refine((v) => !obrigatorio || v.trim() !== '', 'Informe o valor (0 se não tiver renda).')
    .refine(
      (v) => v.trim() === '' || valorMonetarioValido(v),
      'Valor inválido. Use o formato 1.500,00.',
    )
    .refine((v) => v.trim() === '' || paraNumero(v) < 1e10, 'Valor alto demais.');

export const esquemaPasso3 = z.object({
  rendaIndividual: valor(true),
  rendaFamiliar: valor(false),
  membrosFamilia: z
    .string()
    .refine(
      (n) => /^\d+$/.test(n) && Number(n) >= 1 && Number(n) <= 99,
      'Informe de 1 a 99 pessoas.',
    ),
  possuiImovel: z.enum(['sim', 'nao'], { message: 'Responda sim ou não.' }),
  possuiVeiculo: z.enum(['sim', 'nao'], { message: 'Responda sim ou não.' }),
  vulnerabilidades: z.array(z.string()),
});

/**
 * Um resolver por passo, todos com o tipo do formulário inteiro: cada um só valida (e só
 * aponta erros) nos campos do seu passo.
 */
export const RESOLVERS_POR_PASSO: ReadonlyArray<Resolver<DadosCadastro>> = [
  zodResolver(esquemaPasso1) as unknown as Resolver<DadosCadastro>,
  zodResolver(esquemaPasso2) as unknown as Resolver<DadosCadastro>,
  zodResolver(esquemaPasso3) as unknown as Resolver<DadosCadastro>,
];

export interface DadosCadastro {
  nome: string;
  nomeSocial: string;
  cpf: string;
  dataNascimento: string;
  telefone: string;
  email: string;
  senha: string;
  confirmacaoSenha: string;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  rendaIndividual: string;
  rendaFamiliar: string;
  membrosFamilia: string;
  possuiImovel: 'sim' | 'nao' | '';
  possuiVeiculo: 'sim' | 'nao' | '';
  vulnerabilidades: Vulnerabilidade[];
}

export const CAMPOS_POR_PASSO: ReadonlyArray<ReadonlyArray<keyof DadosCadastro>> = [
  ['nome', 'nomeSocial', 'cpf', 'dataNascimento', 'telefone', 'email', 'senha', 'confirmacaoSenha'],
  ['cep', 'logradouro', 'numero', 'complemento', 'bairro', 'cidade', 'uf'],
  [
    'rendaIndividual',
    'rendaFamiliar',
    'membrosFamilia',
    'possuiImovel',
    'possuiVeiculo',
    'vulnerabilidades',
  ],
];

export const VALORES_INICIAIS: DadosCadastro = {
  nome: '',
  nomeSocial: '',
  cpf: '',
  dataNascimento: '',
  telefone: '',
  email: '',
  senha: '',
  confirmacaoSenha: '',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  uf: 'GO',
  rendaIndividual: '',
  rendaFamiliar: '',
  membrosFamilia: '1',
  possuiImovel: '',
  possuiVeiculo: '',
  vulnerabilidades: [],
};

const vazioParaNulo = (valor: string) => (valor.trim() === '' ? null : valor.trim());

/** Converte o formulário no corpo de POST /api/auth/registro (só dígitos em CPF, telefone e CEP). */
export function paraRegistro(d: DadosCadastro) {
  return {
    nome: d.nome.trim(),
    nomeSocial: vazioParaNulo(d.nomeSocial),
    cpf: apenasDigitos(d.cpf),
    dataNascimento: d.dataNascimento,
    telefone: apenasDigitos(d.telefone),
    email: d.email.trim(),
    senha: d.senha,
    cep: vazioParaNulo(apenasDigitos(d.cep)),
    logradouro: vazioParaNulo(d.logradouro),
    numero: vazioParaNulo(d.numero),
    complemento: vazioParaNulo(d.complemento),
    bairro: vazioParaNulo(d.bairro),
    cidade: d.cidade.trim(),
    uf: d.uf,
    rendaIndividual: paraNumero(d.rendaIndividual),
    rendaFamiliar: d.rendaFamiliar.trim() === '' ? null : paraNumero(d.rendaFamiliar),
    membrosFamilia: Number(d.membrosFamilia),
    possuiImovel: d.possuiImovel === 'sim',
    possuiVeiculo: d.possuiVeiculo === 'sim',
    vulnerabilidades: d.vulnerabilidades,
  };
}

export const UFS = [
  'AC',
  'AL',
  'AP',
  'AM',
  'BA',
  'CE',
  'DF',
  'ES',
  'GO',
  'MA',
  'MT',
  'MS',
  'MG',
  'PA',
  'PB',
  'PR',
  'PE',
  'PI',
  'RJ',
  'RN',
  'RS',
  'RO',
  'RR',
  'SC',
  'SP',
  'SE',
  'TO',
] as const;

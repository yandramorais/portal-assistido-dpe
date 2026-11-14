import { Link, type LinkProps } from 'react-router-dom';

import { classesDeBotao, type TamanhoBotao, type VarianteBotao } from './estilosBotao';

interface LinkBotaoProps extends LinkProps {
  variante?: VarianteBotao;
  tamanho?: TamanhoBotao;
}

export function LinkBotao({
  variante = 'primaria',
  tamanho = 'md',
  className,
  ...props
}: LinkBotaoProps) {
  return <Link className={classesDeBotao(variante, tamanho, className)} {...props} />;
}

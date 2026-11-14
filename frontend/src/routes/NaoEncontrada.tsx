import { Link } from 'react-router-dom';

import { CabecalhoDePagina } from '../components/layout/CabecalhoDePagina';

export function NaoEncontrada() {
  return (
    <>
      <CabecalhoDePagina
        titulo="Página não encontrada"
        descricao="O endereço digitado não existe ou mudou."
      />
      <Link to="/" className="text-primary-700 text-sm font-medium underline underline-offset-2">
        Voltar para o início
      </Link>
    </>
  );
}

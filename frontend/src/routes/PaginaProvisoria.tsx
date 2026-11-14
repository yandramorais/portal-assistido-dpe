import { useParams } from 'react-router-dom';

import { CabecalhoDePagina } from '../components/layout/CabecalhoDePagina';

interface PaginaProvisoriaProps {
  titulo: string;
}

/** Página ainda não construída: só o título. Troque pela página real quando ela existir. */
export function PaginaProvisoria({ titulo }: PaginaProvisoriaProps) {
  const { protocolo } = useParams();
  return <CabecalhoDePagina titulo={protocolo ? `${titulo} ${protocolo}` : titulo} />;
}

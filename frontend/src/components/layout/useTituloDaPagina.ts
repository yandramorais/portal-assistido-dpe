import { useEffect } from 'react';

const SUFIXO = 'Portal do Assistido — DPE-GO';

/** Título da aba do navegador, lido também por leitores de tela ao trocar de página. */
export function useTituloDaPagina(titulo: string) {
  useEffect(() => {
    document.title = `${titulo} | ${SUFIXO}`;
  }, [titulo]);
}

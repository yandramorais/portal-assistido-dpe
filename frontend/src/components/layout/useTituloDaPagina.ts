import { useEffect } from 'react';

const SUFIXO = 'Portal do Assistido — DPE-GO';

export function useTituloDaPagina(titulo: string) {
  useEffect(() => {
    document.title = `${titulo} | ${SUFIXO}`;
  }, [titulo]);
}

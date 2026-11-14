import { mensagemDoErro } from '../../lib/api';
import { Button } from './Button';

interface EstadoDeErroProps {
  titulo?: string;
  erro: unknown;
  aoTentarNovamente?: () => void;
}

export function EstadoDeErro({
  titulo = 'Não foi possível carregar as informações.',
  erro,
  aoTentarNovamente,
}: EstadoDeErroProps) {
  return (
    <div
      role="alert"
      className="border-danger-200 bg-danger-50 rounded-md border px-4 py-4 text-sm"
    >
      <p className="text-danger-700 font-semibold">{titulo}</p>
      <p className="mt-1 text-neutral-800">{mensagemDoErro(erro)}</p>
      {aoTentarNovamente && (
        <Button variante="secundaria" tamanho="sm" className="mt-3" onClick={aoTentarNovamente}>
          Tentar novamente
        </Button>
      )}
    </div>
  );
}

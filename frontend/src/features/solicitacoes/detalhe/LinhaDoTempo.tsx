import { dataHora } from '../shared/datas';
import { ROTULO_STATUS } from '../shared/rotulos';
import type { HistoricoItem } from '../shared/tipos';

/** Histórico do pedido, do mais antigo ao mais recente. */
export function LinhaDoTempo({ historico }: { historico: HistoricoItem[] }) {
  if (historico.length === 0) {
    return <p className="text-sm text-neutral-600">Ainda não há movimentações.</p>;
  }
  return (
    <ol className="relative space-y-4 border-l border-neutral-200 pl-5">
      {historico.map((item, indice) => {
        const atual = indice === historico.length - 1;
        return (
          <li key={`${item.statusNovo}-${item.criadoEm}`} className="relative">
            <span
              aria-hidden="true"
              className={
                'absolute top-1.5 -left-[25px] size-2.5 rounded-sm border ' +
                (atual ? 'border-primary-700 bg-primary-700' : 'border-neutral-400 bg-white')
              }
            />
            <p className="text-sm font-medium text-neutral-900">
              {ROTULO_STATUS[item.statusNovo]}
              {atual && <span className="sr-only"> (situação atual)</span>}
            </p>
            <p className="text-xs text-neutral-600">
              <time dateTime={item.criadoEm}>{dataHora(item.criadoEm)}</time>
            </p>
            {item.observacao && <p className="mt-1 text-sm text-neutral-700">{item.observacao}</p>}
          </li>
        );
      })}
    </ol>
  );
}

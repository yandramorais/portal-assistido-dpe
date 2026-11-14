import { Badge } from '../../../components/ui';
import { ROTULO_DOCUMENTO } from '../shared/rotulos';
import type { DocumentoItem } from '../shared/tipos';

export function ChecklistDocumentos({ documentos }: { documentos: DocumentoItem[] }) {
  if (documentos.length === 0) {
    return <p className="text-sm text-neutral-600">Nenhum documento listado.</p>;
  }
  const pendentes = documentos.filter((d) => !d.entregue).length;
  return (
    <>
      <p className="mb-2 text-sm text-neutral-700">
        {pendentes === 0
          ? 'Todos os documentos foram entregues.'
          : `${pendentes} ${pendentes === 1 ? 'documento pendente' : 'documentos pendentes'}.`}
      </p>
      <ul className="divide-y divide-neutral-200 text-sm">
        {documentos.map((d) => (
          <li key={d.tipo} className="flex items-center justify-between gap-3 py-2">
            <span className="text-neutral-800">{ROTULO_DOCUMENTO[d.tipo]}</span>
            {d.entregue ? (
              <Badge tom="primario">Entregue</Badge>
            ) : (
              <Badge tom="contorno">Pendente</Badge>
            )}
          </li>
        ))}
      </ul>
    </>
  );
}

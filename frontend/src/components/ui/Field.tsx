import { cloneElement, isValidElement, useId, type ReactElement, type ReactNode } from 'react';

import { cn } from '../../lib/cn';

interface FieldProps {
  label: string;
  erro?: string;
  dica?: ReactNode;
  obrigatorio?: boolean;
  className?: string;
  children: ReactElement<{
    id?: string;
    'aria-invalid'?: boolean;
    'aria-describedby'?: string;
    'aria-required'?: boolean;
  }>;
}

export function Field({ label, erro, dica, obrigatorio, className, children }: FieldProps) {
  const base = useId();
  const id = children.props.id ?? `${base}-campo`;
  const idDica = dica ? `${base}-dica` : undefined;
  const idErro = erro ? `${base}-erro` : undefined;
  const descritoPor = [idDica, idErro].filter(Boolean).join(' ') || undefined;

  const campo = isValidElement(children)
    ? cloneElement(children, {
        id,
        'aria-invalid': erro ? true : undefined,
        'aria-describedby': descritoPor,
        'aria-required': obrigatorio || undefined,
      })
    : children;

  return (
    <div className={cn('space-y-1', className)}>
      <label htmlFor={id} className="block text-sm font-medium text-neutral-800">
        {label}
        {obrigatorio && (
          <span className="text-danger-600 ml-0.5" aria-hidden="true">
            *
          </span>
        )}
      </label>
      {campo}
      {dica && (
        <p id={idDica} className="text-xs text-neutral-600">
          {dica}
        </p>
      )}
      {erro && (
        <p id={idErro} className="text-danger-700 text-xs font-medium">
          {erro}
        </p>
      )}
    </div>
  );
}

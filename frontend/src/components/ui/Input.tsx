import { forwardRef, type InputHTMLAttributes } from 'react';

import { cn } from '../../lib/cn';

export const classesDeCampo =
  'block w-full rounded border border-neutral-300 bg-white px-3 text-sm text-neutral-900 ' +
  'placeholder:text-neutral-500 focus:border-primary-600 disabled:bg-neutral-100 disabled:text-neutral-500 ' +
  'aria-[invalid=true]:border-danger-600';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function Input({ className, type = 'text', ...props }, ref) {
    return (
      <input ref={ref} type={type} className={cn(classesDeCampo, 'h-9', className)} {...props} />
    );
  },
);

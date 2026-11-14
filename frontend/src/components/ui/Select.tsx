import { forwardRef, type SelectHTMLAttributes } from 'react';

import { cn } from '../../lib/cn';
import { classesDeCampo } from './Input';

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(
  function Select({ className, children, ...props }, ref) {
    return (
      <select ref={ref} className={cn(classesDeCampo, 'h-9 pr-8', className)} {...props}>
        {children}
      </select>
    );
  },
);

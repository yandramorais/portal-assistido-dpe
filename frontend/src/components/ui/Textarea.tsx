import { forwardRef, type TextareaHTMLAttributes } from 'react';

import { cn } from '../../lib/cn';
import { classesDeCampo } from './Input';

export const Textarea = forwardRef<
  HTMLTextAreaElement,
  TextareaHTMLAttributes<HTMLTextAreaElement>
>(function Textarea({ className, rows = 4, ...props }, ref) {
  return (
    <textarea ref={ref} rows={rows} className={cn(classesDeCampo, 'py-2', className)} {...props} />
  );
});

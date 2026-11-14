import { QueryClient } from '@tanstack/react-query';

import { ApiError } from './api';

function deveTentarDeNovo(tentativas: number, erro: unknown): boolean {
  if (erro instanceof ApiError && erro.status >= 400 && erro.status < 500) return false;
  return tentativas < 1;
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: deveTentarDeNovo,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
});

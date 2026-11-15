export function Rodape() {
  return (
    <footer className="border-t border-neutral-200 bg-white">
      <div className="mx-auto flex max-w-6xl flex-col gap-1 px-4 py-4 text-sm text-neutral-700 sm:flex-row sm:items-center sm:justify-between">
        <p className="flex items-center gap-2">
          <img src="/favicon.png" alt="" width={24} height={24} className="h-6 w-6" />
          Defensoria Pública do Estado de Goiás
        </p>
        <p>
          Central de Informações:{' '}
          <a
            href="tel:+556236021224"
            className="text-primary-700 font-medium underline-offset-2 hover:underline"
          >
            (62) 3602-1224
          </a>{' '}
          · Atendimento das 8h às 18h
        </p>
      </div>
    </footer>
  );
}

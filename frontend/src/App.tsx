export default function App() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="bg-primaria-700 text-white">
        <div className="mx-auto max-w-5xl px-4 py-3">
          <p className="text-primaria-100 text-sm">Defensoria Pública do Estado de Goiás</p>
          <p className="text-lg font-semibold">Portal do Assistido</p>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8">
        <h1 className="text-2xl font-semibold">Portal do Assistido</h1>
        <p className="mt-2 max-w-2xl text-gray-700">
          Solicite atendimento à Defensoria Pública e acompanhe seu pedido.
        </p>
      </main>

      <footer className="border-t border-gray-200 bg-white">
        <div className="mx-auto max-w-5xl px-4 py-3 text-sm text-gray-600">
          Defensoria Pública do Estado de Goiás
        </div>
      </footer>
    </div>
  );
}

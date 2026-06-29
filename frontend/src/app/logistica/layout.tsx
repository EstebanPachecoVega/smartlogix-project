import SidebarLogistica from '@/components/logistica/Sidebar';

export default function LogisticaLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-0 flex-1">

      {/*
        Spacer invisible solo en desktop.
        El sidebar real es fixed (fuera del flujo), este div "reserva"
        exactamente los mismos 256px (w-64) para que el <main> no los ocupe.
        En mobile no existe (hidden lg:block) porque el drawer está sobre el contenido.
      */}
      <div className="hidden lg:block lg:w-64 lg:shrink-0" aria-hidden="true" />

      {/* SidebarLogistica renderiza el fixed aside — no ocupa espacio en el flujo */}
      <SidebarLogistica />

      {/*
        min-w-0 es imprescindible: sin él, un hijo flex puede ignorar su propio
        shrink y desbordarse. w-0 + flex-1 garantiza que el main ocupe
        exactamente el espacio que queda después del spacer.
      */}
      <main className="flex-1 min-w-0 p-4 sm:p-6">
        {children}
      </main>

    </div>
  );
}
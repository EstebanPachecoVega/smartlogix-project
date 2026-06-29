import type { Metadata } from 'next';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import { FileText } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Términos y condiciones',
  description: 'Términos y condiciones de uso del sitio SmartLogix. Información sobre precios, disponibilidad y responsabilidades.',
};

export default function TerminosPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Términos y condiciones' }]} />
      <article>
        <div className="flex items-center gap-3 mb-6">
          <FileText className="h-6 w-6 text-primary" />
          <h1 className="text-2xl font-bold">Términos y condiciones</h1>
        </div>
        <div className="prose prose-gray max-w-none space-y-4">
          <p>
            Al acceder y utilizar este sitio web, aceptas cumplir con los siguientes términos y condiciones. 
            Si no estás de acuerdo con alguna parte de estos términos, no debes usar nuestros servicios.
          </p>
          <h2 className="text-lg font-semibold mt-6">Uso del sitio</h2>
          <p>
            Este sitio web se proporciona únicamente para tu uso personal y no comercial. No debes reproducir, 
            distribuir o modificar ningún contenido sin nuestra autorización expresa.
          </p>
          <h2 className="text-lg font-semibold mt-6">Precios y disponibilidad</h2>
          <p>
            Todos los precios están expresados en pesos chilenos (CLP) e incluyen IVA. Nos reservamos el derecho 
            de modificar precios y disponibilidad de productos sin previo aviso.
          </p>
          <h2 className="text-lg font-semibold mt-6">Responsabilidad</h2>
          <p>
            SmartLogix no se hace responsable por daños directos o indirectos derivados del uso indebido de los 
            productos adquiridos en nuestro sitio web.
          </p>
        </div>
      </article>
    </div>
  );
}

'use client';

import Breadcrumbs from '@/components/ui/breadcrumbs';
import { Info } from 'lucide-react';

export default function QuienesSomosPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Quiénes somos' }]} />
      <div className="flex items-center gap-3 mb-6">
        <Info className="h-6 w-6 text-blue-600" />
        <h1 className="text-2xl font-bold">Quiénes somos</h1>
      </div>
      <div className="prose prose-gray max-w-none space-y-4">
        <p>
          En SmartLogix somos una empresa dedicada a la venta de productos tecnológicos, ofreciendo la mejor calidad y los precios más competitivos del mercado.
        </p>
        <p>
          Nuestra misión es facilitar el acceso a la tecnología de punta, brindando una experiencia de compra rápida, segura y confiable. Trabajamos con proveedores oficiales y marcas reconocidas para garantizar la satisfacción de nuestros clientes.
        </p>
        <p>
          Contamos con un equipo altamente capacitado para asesorarte en cada compra y resolver cualquier duda que puedas tener. Nuestro compromiso es entregar productos originales con garantía y un servicio postventa excepcional.
        </p>
      </div>
    </div>
  );
}

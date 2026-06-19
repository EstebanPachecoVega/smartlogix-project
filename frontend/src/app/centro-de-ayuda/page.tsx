import type { Metadata } from 'next';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import { HelpCircle } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Centro de ayuda',
  description: 'Centro de ayuda de SmartLogix. Resuelve tus dudas sobre compras, envíos, devoluciones y más.',
};

export default function CentroAyudaPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Centro de ayuda' }]} />
      <article>
        <div className="flex items-center gap-3 mb-6">
          <HelpCircle className="h-6 w-6 text-blue-600" />
          <h1 className="text-2xl font-bold">Centro de ayuda</h1>
        </div>
        <div className="space-y-6">
          <div className="bg-white rounded-lg border p-5">
            <h2 className="font-semibold text-lg mb-2">¿Cómo realizo una compra?</h2>
            <p className="text-gray-600 text-sm">
              Solo debes navegar por nuestro catálogo, agregar los productos al carrito y seguir los pasos del checkout. 
              No es necesario registrarse para comprar, aunque recomendamos crear una cuenta para dar seguimiento a tus pedidos.
            </p>
          </div>
          <div className="bg-white rounded-lg border p-5">
            <h2 className="font-semibold text-lg mb-2">¿Cuánto tardan los envíos?</h2>
            <p className="text-gray-600 text-sm">
              Los tiempos de entrega varían según tu ubicación. Generalmente, los despachos se realizan dentro de 
              3 a 7 días hábiles para la Región Metropolitana y hasta 10 días hábiles para regiones.
            </p>
          </div>
          <div className="bg-white rounded-lg border p-5">
            <h2 className="font-semibold text-lg mb-2">¿Puedo cambiar o cancelar mi pedido?</h2>
            <p className="text-gray-600 text-sm">
              Sí, puedes cancelar o modificar tu pedido dentro de las primeras 2 horas después de realizada la compra. 
              Para ello, contáctanos a través de nuestros canales de atención.
            </p>
          </div>
        </div>
      </article>
    </div>
  );
}

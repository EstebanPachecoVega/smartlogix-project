import type { Metadata } from 'next';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import { RotateCcw } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Devolución y Reembolso',
  description: 'Política de devolución y reembolso de SmartLogix. Conoce los plazos, condiciones y proceso para solicitar una devolución.',
};

export default function DevolucionPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Devolución y Reembolso' }]} />
      <article>
        <div className="flex items-center gap-3 mb-6">
          <RotateCcw className="h-6 w-6 text-blue-600" />
          <h1 className="text-2xl font-bold">Devolución y Reembolso</h1>
        </div>
        <div className="prose prose-gray max-w-none space-y-4">
          <p>
            En SmartLogix queremos que estés completamente satisfecho con tu compra. Si por algún motivo 
            necesitas devolver un producto, aquí te explicamos cómo hacerlo.
          </p>
          <h2 className="text-lg font-semibold mt-6">Plazo de devolución</h2>
          <p>
            Dispones de 10 días hábiles desde la recepción del producto para solicitar una devolución, 
            de acuerdo a la Ley del Consumidor chilena.
          </p>
          <h2 className="text-lg font-semibold mt-6">Condiciones</h2>
          <p>
            El producto debe estar en su embalaje original, sin uso y con todos sus accesorios. 
            No aceptamos devoluciones de productos que hayan sido dañados por mal uso.
          </p>
          <h2 className="text-lg font-semibold mt-6">Proceso de reembolso</h2>
          <p>
            Una vez recibido y verificado el producto, procesaremos el reembolso en un plazo de 
            5 a 10 días hábiles, a través del mismo medio de pago utilizado en la compra.
          </p>
        </div>
      </article>
    </div>
  );
}

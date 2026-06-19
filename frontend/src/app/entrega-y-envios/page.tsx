import type { Metadata } from 'next';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import { Truck } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Entrega y Envíos',
  description: 'Información de envíos y entregas de SmartLogix. Cobertura nacional, tiempos de entrega y costos de despacho a todo Chile.',
};

export default function EntregaEnviosPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Entrega y Envíos' }]} />
      <article>
        <div className="flex items-center gap-3 mb-6">
          <Truck className="h-6 w-6 text-blue-600" />
          <h1 className="text-2xl font-bold">Entrega y Envíos</h1>
        </div>
        <div className="prose prose-gray max-w-none space-y-4">
          <p>
            Realizamos envíos a todo Chile. A continuación, te detallamos nuestras zonas de cobertura 
            y tiempos estimados de entrega.
          </p>
          <h2 className="text-lg font-semibold mt-6">Cobertura</h2>
          <p>
            Entregamos en todas las regiones de Chile, desde Arica hasta Punta Arenas. 
            También ofrecemos retiro en tienda sin costo adicional.
          </p>
          <h2 className="text-lg font-semibold mt-6">Tiempos de entrega</h2>
          <ul className="list-disc list-inside space-y-1">
            <li>Región Metropolitana: 2 a 4 días hábiles</li>
            <li>Zona Centro (V a VIII): 4 a 6 días hábiles</li>
            <li>Zona Norte (I a IV): 6 a 8 días hábiles</li>
            <li>Zona Sur (IX a XII): 7 a 10 días hábiles</li>
          </ul>
          <h2 className="text-lg font-semibold mt-6">Valor del envío</h2>
          <p>
            El costo de envío se calcula al momento del checkout según el destino y peso del pedido. 
            Los envíos sobre $50.000 tienen despacho gratuito a todo Chile.
          </p>
        </div>
      </article>
    </div>
  );
}

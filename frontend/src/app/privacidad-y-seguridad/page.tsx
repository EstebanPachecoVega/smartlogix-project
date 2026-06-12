'use client';

import Breadcrumbs from '@/components/ui/breadcrumbs';
import { ShieldCheck } from 'lucide-react';

export default function PrivacidadPage() {
  return (
    <div className="max-w-3xl mx-auto">
      <Breadcrumbs items={[{ label: 'Inicio', href: '/' }, { label: 'Privacidad y Seguridad' }]} />
      <div className="flex items-center gap-3 mb-6">
        <ShieldCheck className="h-6 w-6 text-blue-600" />
        <h1 className="text-2xl font-bold">Privacidad y Seguridad</h1>
      </div>
      <div className="prose prose-gray max-w-none space-y-4">
        <p>
          En SmartLogix valoramos tu privacidad. Esta política describe cómo recopilamos, usamos y protegemos 
          tu información personal.
        </p>
        <h2 className="text-lg font-semibold mt-6">Información que recopilamos</h2>
        <p>
          Recopilamos la información que nos proporcionas al registrarte o realizar una compra, incluyendo 
          nombre, correo electrónico, dirección y datos de pago.
        </p>
        <h2 className="text-lg font-semibold mt-6">Uso de la información</h2>
        <p>
          Utilizamos tu información para procesar pedidos, mejorar nuestros servicios y enviar comunicaciones 
          relacionadas con tus compras. No compartimos tus datos con terceros sin tu consentimiento.
        </p>
        <h2 className="text-lg font-semibold mt-6">Seguridad</h2>
        <p>
          Implementamos medidas de seguridad técnicas y organizativas para proteger tu información contra 
          acceso no autorizado, pérdida o alteración.
        </p>
      </div>
    </div>
  );
}

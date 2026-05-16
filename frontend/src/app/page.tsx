'use client';
import Link from 'next/link';
import { useUIStore } from '@/stores/uiStore';

export default function HomePage() {
  const { loading } = useUIStore();
  return (
    <div className="text-center">
      <h1 className="text-3xl font-bold mb-4">Bienvenido a SmartLogix</h1>
      <p className="mb-4">Gestiona pedidos y envíos de forma inteligente.</p>
      <div className="space-x-4">
        <Link href="/pedidos/nuevo" className="bg-blue-600 text-white px-4 py-2 rounded">
          Nuevo Pedido
        </Link>
        <Link href="/pedidos" className="bg-gray-600 text-white px-4 py-2 rounded">
          Ver Pedidos
        </Link>
      </div>
      {loading && <p className="mt-4">Cargando...</p>}
    </div>
  );
}
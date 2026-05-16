import Link from 'next/link';

export default function Home() {
  return (
    <div className="text-center">
      <h1 className="text-3xl font-bold">Bienvenido a SmartLogix</h1>
      <p className="mt-2">Sistema inteligente de gestión logística</p>
      <div className="mt-4 space-x-4">
        <Link href="/pedidos/nuevo" className="bg-blue-500 text-white px-4 py-2 rounded">
          Crear Pedido
        </Link>
        <Link href="/pedidos" className="bg-gray-500 text-white px-4 py-2 rounded">
          Ver Pedidos
        </Link>
      </div>
    </div>
  );
}
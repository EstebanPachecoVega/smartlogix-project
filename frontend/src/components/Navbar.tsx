'use client';
import Link from 'next/link';

export default function Navbar() {
  return (
    <nav className="bg-blue-600 text-white p-4">
      <div className="container mx-auto flex gap-4">
        <Link href="/" className="font-bold">SmartLogix</Link>
        <Link href="/pedidos" className="hover:underline">Pedidos</Link>
        <Link href="/pedidos/nuevo" className="hover:underline">Nuevo Pedido</Link>
      </div>
    </nav>
  );
}
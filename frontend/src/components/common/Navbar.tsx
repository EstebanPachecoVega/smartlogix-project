import Link from 'next/link';

export default function Navbar() {
  return (
    <nav className="bg-gray-800 text-white p-4">
      <div className="container mx-auto flex space-x-4">
        <Link href="/">Inicio</Link>
        <Link href="/pedidos/nuevo">Nuevo Pedido</Link>
        <Link href="/pedidos">Mis Pedidos</Link>
        <Link href="/envios">Envíos</Link>
      </div>
    </nav>
  );
}
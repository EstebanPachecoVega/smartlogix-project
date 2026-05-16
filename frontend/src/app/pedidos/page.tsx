import PedidoList from '@/components/pedidos/PedidoList';

export default function PedidosPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Listado de Pedidos</h1>
      <PedidoList />
    </div>
  );
}
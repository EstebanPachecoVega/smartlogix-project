'use client';

import { useCarritoStore } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

export default function CarritoResumen() {
  const { items, actualizarCantidad, eliminar, totalPrecio } = useCarritoStore();

  if (items.length === 0) return <p className="text-center py-8">Carrito vacío</p>;

  return (
    <div className="space-y-4">
      {items.map((item) => (
        <div key={item.producto.id} className="flex justify-between items-center border-b pb-2">
          <div className="flex-1">
            <p className="font-semibold">{item.producto.nombre}</p>
            <p className="text-sm text-gray-600">${item.producto.precio.toLocaleString()} c/u</p>
          </div>
          <div className="flex items-center gap-2">
            <Input
              type="number"
              min={1}
              max={item.producto.cantidad}
              value={item.cantidad}
              onChange={(e) => actualizarCantidad(item.producto.id, parseInt(e.target.value) || 1)}
              className="w-20 text-center"
            />
            <Button variant="destructive" size="sm" onClick={() => eliminar(item.producto.id)}>
              Eliminar
            </Button>
          </div>
        </div>
      ))}
      <div className="text-right text-xl font-bold pt-4">Total: ${totalPrecio.toLocaleString()}</div>
    </div>
  );
}
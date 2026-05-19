'use client';

import { Producto } from '@/types';
import { useCarritoStore } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';

export default function ProductCard({ producto }: { producto: Producto }) {
  const agregar = useCarritoStore((state) => state.agregar);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">{producto.nombre}</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-gray-600 text-sm">{producto.descripcion?.slice(0, 80)}...</p>
        <p className="text-xl font-bold mt-2">${producto.precio.toLocaleString()}</p>
        <p className="text-xs text-gray-500">Stock: {producto.cantidad}</p>
      </CardContent>
      <CardFooter>
        <Button
          onClick={() => agregar(producto, 1)}
          disabled={producto.cantidad <= 0}
          className="w-full"
        >
          Agregar
        </Button>
      </CardFooter>
    </Card>
  );
}
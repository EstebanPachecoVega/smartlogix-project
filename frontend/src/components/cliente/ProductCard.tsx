'use client';

import { Producto } from '@/types';
import { useCarritoStore } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardTitle } from '@/components/ui/card';
import { ImageIcon } from 'lucide-react';
import Link from 'next/link';

function generarSlug(nombre: string) {
  return nombre
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
}

export default function ProductCard({ producto }: { producto: Producto }) {
  const agregar = useCarritoStore((state) => state.agregar);
  const slug = producto.slug || generarSlug(producto.nombre);

  return (
    <Card className="overflow-hidden flex flex-col h-full group">
      <Link href={`/productos/${slug}`} className="block" draggable={false}>
        <div className="aspect-[4/3] bg-gray-100 relative overflow-hidden">
          {producto.imagenPrincipal ? (
            <img
              src={producto.imagenPrincipal}
              alt={producto.nombre}
              draggable={false}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-300">
              <ImageIcon className="h-12 w-12" />
            </div>
          )}
        </div>
      </Link>

      <Link href={`/productos/${slug}`} className="flex flex-col flex-1" draggable={false}>
        <div className="p-3 pb-0 flex-1">
          <CardTitle className="text-sm font-semibold truncate">{producto.nombre}</CardTitle>
          {producto.descripcion && (
            <p className="text-gray-500 text-xs mt-1 line-clamp-2">{producto.descripcion}</p>
          )}
        </div>
        <CardContent className="p-3 pb-0">
          <p className="text-lg font-bold">${producto.precio.toLocaleString()}</p>
          <p className="text-xs text-gray-500 mt-0.5">
            {producto.cantidad > 0 ? `Stock: ${producto.cantidad}` : 'Sin stock'}
          </p>
        </CardContent>
      </Link>

      <CardFooter className="p-3 mt-auto">
        <Button
          onClick={(e) => {
            e.preventDefault();
            agregar(producto, 1);
          }}
          disabled={producto.cantidad <= 0}
          className="w-full text-sm"
          size="sm"
        >
          {producto.cantidad > 0 ? 'Agregar' : 'Sin stock'}
        </Button>
      </CardFooter>
    </Card>
  );
}

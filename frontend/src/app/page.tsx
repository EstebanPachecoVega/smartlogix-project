'use client';

import { useEffect, useState } from 'react';
import { productosPublicApi } from '@/lib/api';
import { Producto } from '@/types';
import ProductCard from '@/components/cliente/ProductCard';
import Spinner from '@/components/shared/Spinner';

export default function CatalogoPage() {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    productosPublicApi.listar()
      .then(setProductos)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Catálogo de productos</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {productos.map((prod) => (
          <ProductCard key={prod.id} producto={prod} />
        ))}
      </div>
    </div>
  );
}
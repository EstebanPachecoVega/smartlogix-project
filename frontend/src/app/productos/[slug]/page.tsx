import type { Metadata } from 'next';
import { productosPublicApi } from '@/lib/api';
import ProductDetailClient from './ProductDetailClient';

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;

  try {
    const producto = await productosPublicApi.obtenerPorSlug(slug);
    return {
      title: producto.nombre,
      description: producto.descripcion?.slice(0, 160) || `${producto.nombre} — SmartLogix. Envíos a todo Chile.`,
      openGraph: {
        title: producto.nombre,
        description: producto.descripcion?.slice(0, 160),
        images: producto.imagenPrincipal ? [{ url: producto.imagenPrincipal }] : undefined,
      },
    };
  } catch {
    return { title: 'Producto no encontrado' };
  }
}

export default function ProductoDetallePage() {
  return <ProductDetailClient />;
}

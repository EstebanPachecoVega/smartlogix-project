import type { Metadata } from 'next';
import { Suspense } from 'react';
import HomeClient from './HomeClient';
import Spinner from '@/components/shared/Spinner';

export async function generateMetadata({ searchParams }: { searchParams: Promise<{ [key: string]: string | string[] | undefined }> }): Promise<Metadata> {
  const params = await searchParams;
  const catSlug = typeof params.cat === 'string' ? params.cat : undefined;
  const search = typeof params.search === 'string' ? params.search : undefined;
  const novedad = typeof params.novedad === 'string' ? params.novedad : undefined;

  if (novedad === 'true') {
    return {
      title: 'Novedades',
      description: 'Descubre las últimas novedades en tecnología en SmartLogix. Productos recién llegados al mejor precio.',
    };
  }

  if (search) {
    return {
      title: `Resultados: "${search}"`,
      description: `Resultados de búsqueda para "${search}" en SmartLogix. Encuentra los mejores productos tecnológicos.`,
    };
  }

  if (catSlug) {
    try {
      const { categoriasPublicApi } = await import('@/lib/api');
      const cat = await categoriasPublicApi.obtenerPorSlug(catSlug);
      return {
        title: cat.nombre,
        description: `Compra ${cat.nombre} en SmartLogix. Los mejores productos tecnológicos al mejor precio.`,
      };
    } catch {
      // fallback to default from layout
    }
  }

  return {};
}

export default function HomePage() {
  return (
    <Suspense fallback={<Spinner />}>
      <HomeClient />
    </Suspense>
  );
}

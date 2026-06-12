'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { productosPublicApi, categoriasPublicApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import ProductCard from '@/components/cliente/ProductCard';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import Spinner from '@/components/shared/Spinner';

function buildCategoryChainBySlug(slug: string, allCats: Categoria[]): Categoria[] {
  const chain: Categoria[] = [];
  let current = allCats.find((c) => c.slug === slug);
  while (current) {
    chain.unshift(current);
    current = current.padreId ? allCats.find((c) => c.id === current!.padreId) : undefined;
  }
  return chain;
}

function CatalogoContent() {
  const searchParams = useSearchParams();
  const catSlug = searchParams.get('cat');
  const search = searchParams.get('search');

  const [productos, setProductos] = useState<Producto[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [breadcrumbCats, setBreadcrumbCats] = useState<Categoria[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const allCats = await categoriasPublicApi.listar();
        setCategorias(allCats);

        let currentCat: Categoria | undefined;
        if (catSlug) {
          currentCat = allCats.find((c) => c.slug === catSlug);
        }

        if (currentCat) {
          setBreadcrumbCats(buildCategoryChainBySlug(catSlug!, allCats));
        } else {
          setBreadcrumbCats([]);
        }

        const prods = await productosPublicApi.listar({
          ...(currentCat ? { categoriaId: currentCat.id } : {}),
          ...(search ? { nombre: search } : {}),
        });
        setProductos(prods);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [catSlug, search]);

  const title = search
    ? `Resultados: "${search}"`
    : breadcrumbCats.length > 0
      ? breadcrumbCats[breadcrumbCats.length - 1].nombre
      : 'Catálogo de productos';

  const breadcrumbItems: { label: string; href?: string }[] = [];

  if (search) {
    breadcrumbItems.push({ label: `Resultados: "${search}"` });
  } else if (breadcrumbCats.length > 0) {
    breadcrumbCats.forEach((cat, i) => {
      breadcrumbItems.push({
        label: cat.nombre,
        href: i < breadcrumbCats.length - 1 ? `/?cat=${cat.slug}` : undefined,
      });
    });
  } else {
    breadcrumbItems.push({ label: 'Catálogo' });
  }

  if (loading) return <Spinner />;

  return (
    <div>
      <Breadcrumbs items={breadcrumbItems} />
      <h1 className="text-2xl font-bold mb-6">{title}</h1>

      {productos.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500">No se encontraron productos</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {productos.map((prod) => (
            <ProductCard key={prod.id} producto={prod} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function CatalogoPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <CatalogoContent />
    </Suspense>
  );
}

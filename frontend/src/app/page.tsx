'use client';

import { useEffect, useState, Suspense, useMemo } from 'react';
import { useSearchParams } from 'next/navigation';
import { productosPublicApi, categoriasPublicApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import { getAllDescendantIds } from '@/lib/categoryTree';
import ProductCard from '@/components/cliente/ProductCard';
import ProductCarousel from '@/components/cliente/ProductCarousel';
import HeroSlider from '@/components/cliente/HeroSlider';
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

function HomeContent() {
  const searchParams = useSearchParams();
  const catSlug = searchParams.get('cat');
  const search = searchParams.get('search');

  const [destacados, setDestacados] = useState<Producto[]>([]);
  const [novedades, setNovedades] = useState<Producto[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [productos, setProductos] = useState<Producto[]>([]);
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
          ...(search ? { nombre: search } : {}),
        });
        setProductos(currentCat && !search
          ? prods.filter((p) => getAllDescendantIds(currentCat.id, allCats).has(p.categoriaId))
          : prods
        );

        if (!currentCat && !search) {
          const [dest, nov] = await Promise.all([
            productosPublicApi.listar({ destacado: true }),
            productosPublicApi.listar({ novedad: true }),
          ]);
          setDestacados(dest);
          setNovedades(nov);
        }
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [catSlug, search]);

  // Build parent category list sorted by ordenVisual
  const parentCats = useMemo(() =>
    categorias
      .filter((cat) => !cat.padreId)
      .sort((a, b) => (a.ordenVisual ?? 999) - (b.ordenVisual ?? 999)),
  [categorias]);

  // Map parent category ID → products (recursive, includes all descendants)
  const productosPorCategoria = useMemo(() => {
    const map: Record<number, Producto[]> = {};

    parentCats.forEach((parent) => {
      const ids = getAllDescendantIds(parent.id, categorias);
      const prods = productos.filter((p) => ids.has(p.categoriaId));
      if (prods.length > 0) {
        map[parent.id] = prods;
      }
    });

    return map;
  }, [parentCats, categorias, productos]);

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
    breadcrumbItems.push({ label: 'Inicio' });
  }

  const isFiltered = !!catSlug || !!search;

  if (loading) return <Spinner />;

  return (
    <div>
      {!isFiltered ? (
        <>
          <HeroSlider />

          <ProductCarousel
            productos={destacados}
            title="Productos Destacados"
          />

          <ProductCarousel
            productos={novedades}
            title="Novedades"
          />

          {parentCats.map((parent) => {
            const prods = productosPorCategoria[parent.id];
            if (!prods || prods.length === 0) return null;

            return (
              <ProductCarousel
                key={parent.id}
                productos={prods}
                title={parent.nombre}
              />
            );
          })}
        </>
      ) : (
        <>
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
        </>
      )}
    </div>
  );
}

export default function HomePage() {
  return (
    <Suspense fallback={<Spinner />}>
      <HomeContent />
    </Suspense>
  );
}

'use client';

import { useEffect, useState, useMemo } from 'react';
import { useSearchParams } from 'next/navigation';
import { productosPublicApi, categoriasPublicApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import { getAllDescendantIds } from '@/lib/categoryTree';
import ProductCard from '@/components/cliente/ProductCard';
import ProductCarousel from '@/components/cliente/ProductCarousel';
import HeroSlider from '@/components/cliente/HeroSlider';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import Spinner from '@/components/shared/Spinner';
import FilterSidebar from '@/components/cliente/FilterSidebar';

function buildCategoryChainBySlug(slug: string, allCats: Categoria[]): Categoria[] {
  const chain: Categoria[] = [];
  let current = allCats.find((c) => c.slug === slug);
  while (current) {
    chain.unshift(current);
    current = current.padreId ? allCats.find((c) => c.id === current!.padreId) : undefined;
  }
  return chain;
}

export default function HomeContent() {
  const searchParams = useSearchParams();
  const catSlug = searchParams.get('cat');
  const search = searchParams.get('search');
  const novedad = searchParams.get('novedad');

  const [destacados, setDestacados] = useState<Producto[]>([]);
  const [novedades, setNovedades] = useState<Producto[]>([]);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [productos, setProductos] = useState<Producto[]>([]);
  const [breadcrumbCats, setBreadcrumbCats] = useState<Categoria[]>([]);
  const [loading, setLoading] = useState(true);
  const [precioMin, setPrecioMin] = useState<number | undefined>();
  const [precioMax, setPrecioMax] = useState<number | undefined>();
  const [soloStock, setSoloStock] = useState(false);

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
          ...(novedad === 'true' ? { novedad: true } : {}),
        });
        setProductos(currentCat && !search && novedad !== 'true'
          ? prods.filter((p) => getAllDescendantIds(currentCat.id, allCats).has(p.categoriaId))
          : prods
        );

        if (!currentCat && !search && novedad !== 'true') {
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
  }, [catSlug, search, novedad]);

  const parentCats = useMemo(() =>
    categorias
      .filter((cat) => !cat.padreId)
      .sort((a, b) => (a.ordenVisual ?? 999) - (b.ordenVisual ?? 999)),
  [categorias]);

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

  const title = novedad === 'true'
    ? 'Novedades'
    : search
      ? `Resultados: "${search}"`
      : breadcrumbCats.length > 0
        ? breadcrumbCats[breadcrumbCats.length - 1].nombre
        : 'Catálogo de productos';

  const breadcrumbItems: { label: string; href?: string }[] = [];

  if (novedad === 'true') {
    breadcrumbItems.push({ label: 'Novedades' });
  } else if (search) {
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

  const isFiltered = !!catSlug || !!search || novedad === 'true';

  const globalMinPrice = useMemo(() =>
    productos.length > 0 ? Math.min(...productos.map((p) => p.precio)) : 0,
  [productos]);

  const globalMaxPrice = useMemo(() =>
    productos.length > 0 ? Math.max(...productos.map((p) => p.precio)) : 999999,
  [productos]);

  const filteredProductos = useMemo(() => {
    let result = productos;
    if (precioMin !== undefined) result = result.filter((p) => p.precio >= precioMin);
    if (precioMax !== undefined) result = result.filter((p) => p.precio <= precioMax);
    if (soloStock) result = result.filter((p) => p.cantidad > 0);
    return result;
  }, [productos, precioMin, precioMax, soloStock]);

  if (loading) return <Spinner />;

  return (
    <div>
      {!isFiltered ? (
        <>
          <section aria-label="Destacados principales">
            <HeroSlider />
          </section>

          <section aria-label="Productos destacados">
            <ProductCarousel
              productos={destacados}
              title="Productos Destacados"
            />
          </section>

          <section aria-label="Novedades">
            <ProductCarousel
              productos={novedades}
              title="Novedades"
            />
          </section>

          {parentCats.map((parent) => {
            const prods = productosPorCategoria[parent.id];
            if (!prods || prods.length === 0) return null;

            return (
              <section key={parent.id} aria-label={parent.nombre}>
                <ProductCarousel
                  productos={prods}
                  title={parent.nombre}
                />
              </section>
            );
          })}
        </>
      ) : (
        <>
          <Breadcrumbs items={breadcrumbItems} />
          <section aria-label={title}>
            <h1 className="text-2xl font-bold mb-6">{title}</h1>

            <div className="flex flex-col sm:flex-row gap-6">
              <FilterSidebar
                precioMin={precioMin}
                precioMax={precioMax}
                soloStock={soloStock}
                onPrecioChange={(min, max) => { setPrecioMin(min); setPrecioMax(max); }}
                onStockChange={setSoloStock}
                onClear={() => { setPrecioMin(undefined); setPrecioMax(undefined); setSoloStock(false); }}
                minPrice={globalMinPrice}
                maxPrice={globalMaxPrice}
              />

              <div className="flex-1 min-w-0">
                {filteredProductos.length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500">No se encontraron productos</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {filteredProductos.map((prod) => (
                      <ProductCard key={prod.id} producto={prod} />
                    ))}
                  </div>
                )}
              </div>
            </div>
          </section>
        </>
      )}
    </div>
  );
}

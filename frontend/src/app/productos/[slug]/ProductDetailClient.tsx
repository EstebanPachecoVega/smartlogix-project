'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { productosPublicApi, categoriasPublicApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import { buildCategoryChainById } from '@/lib/categoryTree';
import { useCarritoStore } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import ImageGallery from '@/components/cliente/ImageGallery';
import ProductCarousel from '@/components/cliente/ProductCarousel';
import Spinner from '@/components/shared/Spinner';
import JsonLdProduct from '@/components/seo/JsonLdProduct';
import JsonLdBreadcrumb from '@/components/seo/JsonLdBreadcrumb';
import { ArrowLeft, ShoppingCart } from 'lucide-react';

export default function ProductDetailClient() {
  const { slug } = useParams<{ slug: string }>();
  const router = useRouter();
  const agregar = useCarritoStore((state) => state.agregar);
  const [producto, setProducto] = useState<Producto | null>(null);
  const [similares, setSimilares] = useState<Producto[]>([]);
  const [breadcrumbCats, setBreadcrumbCats] = useState<Categoria[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [cantidad, setCantidad] = useState(1);

  useEffect(() => {
    productosPublicApi
      .obtenerPorSlug(slug)
      .then((prod) => {
        setProducto(prod);
        if (prod.categoriaId) {
          categoriasPublicApi.listar().then((allCats) => {
            setBreadcrumbCats(buildCategoryChainById(prod.categoriaId!, allCats));
          });
          productosPublicApi.listar({ categoriaId: prod.categoriaId }).then((all) => {
            const others = all.filter((p) => p.id !== prod.id);
            setSimilares(others.sort(() => Math.random() - 0.5).slice(0, 8));
          });
        }
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [slug]);

  if (loading) return <Spinner />;

  if (error || !producto) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold mb-4">Producto no encontrado</h1>
        <Button onClick={() => router.push('/')}>Volver al catálogo</Button>
      </div>
    );
  }

  const breadcrumbItems = breadcrumbCats.length > 0
    ? breadcrumbCats.map((cat) => ({ label: cat.nombre, href: `/?cat=${cat.slug}` }))
    : [{ label: 'Catálogo', href: '/' }];

  return (
    <div>
      <JsonLdProduct producto={producto} />
      <JsonLdBreadcrumb items={breadcrumbItems} productName={producto.nombre} />
      <Breadcrumbs items={[
        ...breadcrumbItems,
        { label: producto.nombre },
      ]} />

      <Button variant="ghost" size="sm" className="mb-4" onClick={() => router.push('/')}>
        <ArrowLeft className="h-4 w-4 mr-2" /> Volver al catálogo
      </Button>

      <article>
        <div className="grid md:grid-cols-2 gap-6 lg:gap-8">
          <ImageGallery
            imagenPrincipal={producto.imagenPrincipal}
            imagenes={producto.imagenes}
            nombre={producto.nombre}
          />

          <div className="space-y-4 sticky top-24 self-start">
            <div>
              <p className="text-xs text-muted-foreground uppercase tracking-wide">{producto.categoriaNombre || 'Producto'}</p>
              <h1 className="text-xl font-bold mt-1">{producto.nombre}</h1>
              {producto.sku && (
                <p className="text-xs text-muted-foreground/70 mt-1">SKU: {producto.sku}</p>
              )}
            </div>

            <p className="text-2xl font-bold text-foreground">
              ${producto.precio.toLocaleString()}
            </p>

            {producto.descripcion && (
              <div>
                <h3 className="font-semibold text-sm mb-1">Descripción</h3>
                <p className="text-muted-foreground text-sm leading-relaxed">{producto.descripcion}</p>
              </div>
            )}

            <div className="flex items-center gap-2">
              <span className={`inline-flex items-center gap-1.5 text-sm ${producto.cantidad > 0 ? 'text-green-600' : 'text-red-500'}`}>
                <span className={`w-2 h-2 rounded-full ${producto.cantidad > 0 ? 'bg-green-500' : 'bg-red-500'}`} />
                {producto.cantidad > 0 ? `En stock (${producto.cantidad} unidades)` : 'Sin stock'}
              </span>
            </div>

            {producto.cantidad > 0 && (
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-2">
                  <label className="text-sm text-muted-foreground">Cantidad:</label>
                  <Input
                    type="number"
                    min={1}
                    max={producto.cantidad}
                    value={cantidad}
                    onChange={(e) => setCantidad(Math.min(Math.max(1, parseInt(e.target.value) || 1), producto.cantidad))}
                    className="w-20 text-center"
                  />
                </div>
                <Button
                  onClick={() => {
                    agregar(producto, cantidad);
                    router.push('/dashboard/carrito');
                  }}
                  className="flex-1"
                >
                  <ShoppingCart className="h-4 w-4 mr-2" />
                  Agregar al carrito
                </Button>
              </div>
            )}

            <div className="flex gap-2">
              {producto.destacado && (
                <span className="inline-block bg-yellow-100 text-yellow-800 text-xs font-medium px-2.5 py-1 rounded">
                  Destacado
                </span>
              )}
              {producto.novedad && (
                <span className="inline-block bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-1 rounded">
                  Novedad
                </span>
              )}
            </div>
          </div>
        </div>
      </article>

      {similares.length > 0 && (
        <section aria-label="Productos similares" className="mt-12">
          <ProductCarousel
            productos={similares}
            title="Productos Similares"
          />
        </section>
      )}
    </div>
  );
}

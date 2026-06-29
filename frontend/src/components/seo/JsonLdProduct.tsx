import { Producto } from '@/types';

const BASE_URL = 'https://smartlogix.cl';

export default function JsonLdProduct({ producto }: { producto: Producto }) {
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: producto.nombre,
    description: producto.descripcion,
    image: producto.imagenPrincipal || producto.imagenes?.[0],
    sku: producto.sku,
    offers: {
      '@type': 'Offer',
      url: `${BASE_URL}/productos/${producto.slug || producto.nombre.toLowerCase().replace(/\s+/g, '-')}`,
      price: producto.precio,
      priceCurrency: 'CLP',
      availability: producto.cantidad > 0
        ? 'https://schema.org/InStock'
        : 'https://schema.org/OutOfStock',
    },
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}

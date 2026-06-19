import { Categoria } from '@/types';

const BASE_URL = 'https://smartlogix.cl';

interface BreadcrumbItem {
  label: string;
  href?: string;
}

export default function JsonLdBreadcrumb({
  items,
  productName,
}: {
  items: BreadcrumbItem[];
  productName?: string;
}) {
  const itemListElement: {
    '@type': 'ListItem';
    position: number;
    name: string;
    item?: string;
  }[] = [
    { '@type': 'ListItem', position: 1, name: 'Inicio', item: BASE_URL },
  ];

  items.forEach((item, i) => {
    const entry: { '@type': 'ListItem'; position: number; name: string; item?: string } = {
      '@type': 'ListItem',
      position: i + 2,
      name: item.label,
    };
    if (item.href) {
      entry.item = `${BASE_URL}${item.href.startsWith('/') ? '' : '/'}${item.href}`;
    }
    itemListElement.push(entry);
  });

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement,
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}

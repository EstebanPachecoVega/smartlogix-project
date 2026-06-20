'use client';

import { usePathname } from 'next/navigation';
import { Suspense } from 'react';
import CategoryNav from './CategoryNav';

export default function PublicCategoryNav() {
  const pathname = usePathname();
  if (pathname.startsWith('/logistica')) return null;
  return (
    <Suspense fallback={null}>
      <CategoryNav />
    </Suspense>
  );
}

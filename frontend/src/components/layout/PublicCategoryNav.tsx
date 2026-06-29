'use client';

import { usePathname } from 'next/navigation';
import { Suspense } from 'react';
import CategoryNav from './CategoryNav';

export default function PublicCategoryNav() {
  const pathname = usePathname();
  if (pathname.startsWith('/logistica') || pathname.startsWith('/dashboard')) return null;
  return (
    <Suspense fallback={null}>
      <CategoryNav />
    </Suspense>
  );
}

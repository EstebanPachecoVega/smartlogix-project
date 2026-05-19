'use client';

import Link from 'next/link';
import CarritoResumen from '@/components/cliente/CarritoResumen';
import { Button } from '@/components/ui/button';

export default function CarritoPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Carrito</h1>
      <CarritoResumen />
      <div className="mt-6 flex justify-end">
        <Link href="/checkout">
          <Button>Proceder al checkout</Button>
        </Link>
      </div>
    </div>
  );
}
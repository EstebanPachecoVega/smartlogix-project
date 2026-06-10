'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

interface ProductoPageProps {
    params: Promise<{ id: string }>;
}

export default function ProductoPage({ params }: ProductoPageProps) {
    const { id } = use(params);
    const router = useRouter();

    useEffect(() => {
        router.replace(`/logistica/productos/${id}/editar`);
    }, [id, router]);

    return null;
}
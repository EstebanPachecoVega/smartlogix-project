'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { categoriasPublicApi } from '@/lib/api';
import { Categoria } from '@/types';
import { ChevronRight } from 'lucide-react';

export default function CategoryNav() {
  const [parents, setParents] = useState<Categoria[]>([]);
  const [children, setChildren] = useState<Record<number, Categoria[]>>({});
  const [openId, setOpenId] = useState<number | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout>>(undefined as any);
  const router = useRouter();
  const searchParams = useSearchParams();
  const activeSlug = searchParams.get('cat');

  useEffect(() => {
    categoriasPublicApi.listar().then((all) => {
      const childMap: Record<number, Categoria[]> = {};
      const roots: Categoria[] = [];

      all.forEach((cat) => {
        if (cat.padreId) {
          if (!childMap[cat.padreId]) childMap[cat.padreId] = [];
          childMap[cat.padreId].push(cat);
        } else {
          roots.push(cat);
        }
      });

      setParents(roots);
      setChildren(childMap);
    });
  }, []);

  const scheduleClose = () => {
    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setOpenId(null), 150);
  };

  const cancelClose = () => {
    clearTimeout(timeoutRef.current);
  };

  if (parents.length === 0) return null;

  return (
    <nav className="border-t bg-white">
      <div className="container mx-auto px-4">
        <ul className="flex items-center justify-center gap-0.5 py-2 flex-wrap">
          {parents.map((parent) => {
            const subs = children[parent.id] || [];
            const isActive = activeSlug === parent.slug;
            const isOpen = openId === parent.id;

            return (
              <li
                key={parent.id}
                className="relative"
                onMouseEnter={() => { cancelClose(); setOpenId(parent.id); }}
                onMouseLeave={scheduleClose}
              >
                <button
                  onClick={() => router.push(`/?cat=${parent.slug}`)}
                  className={`px-3 py-1.5 text-sm rounded-md whitespace-nowrap transition-colors flex items-center gap-1 ${
                    isActive
                      ? 'text-blue-600 bg-blue-50 font-medium'
                      : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                  }`}
                >
                  {parent.nombre}
                  {subs.length > 0 && (
                    <ChevronRight className={`h-3 w-3 transition-transform ${isOpen ? 'rotate-90' : ''}`} />
                  )}
                </button>

                {subs.length > 0 && (
                  <div
                    className={`absolute top-full left-0 mt-0.5 bg-white border rounded-lg shadow-lg min-w-[180px] py-1 z-50 ${isOpen ? 'block' : 'hidden'}`}
                    onMouseEnter={cancelClose}
                    onMouseLeave={scheduleClose}
                  >
                    {subs.map((child) => (
                      <button
                        key={child.id}
                        onClick={() => router.push(`/?cat=${child.slug}`)}
                        className={`w-full text-left px-3 py-2 text-sm whitespace-nowrap transition-colors ${
                          activeSlug === child.slug
                            ? 'text-blue-600 bg-blue-50 font-medium'
                            : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                        }`}
                      >
                        {child.nombre}
                      </button>
                    ))}
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}

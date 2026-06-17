'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { categoriasPublicApi } from '@/lib/api';
import { buildCategoryTree, CategoryNode } from '@/lib/categoryTree';
import { ChevronDown } from 'lucide-react';

function CategoryColumn({
  node,
  activeSlug,
  router,
}: {
  node: CategoryNode;
  activeSlug: string | null;
  router: ReturnType<typeof useRouter>;
}) {
  return (
    <div>
      <button
        onClick={() => router.push(`/?cat=${node.category.slug}`)}
        className={`block w-full text-left font-semibold text-sm mb-2 transition-colors ${
          activeSlug === node.category.slug
            ? 'text-blue-600'
            : 'text-gray-800 hover:text-blue-600'
        }`}
      >
        {node.category.nombre}
      </button>
      {node.children.length > 0 && (
        <ul className="space-y-1">
          {node.children.map((child) => (
            <li key={child.category.id}>
              <button
                onClick={() => router.push(`/?cat=${child.category.slug}`)}
                className={`block w-full text-left text-sm transition-colors ${
                  activeSlug === child.category.slug
                    ? 'text-blue-600 font-medium'
                    : 'text-gray-600 hover:text-blue-600'
                }`}
              >
                {child.category.nombre}
              </button>
              {child.children.length > 0 && (
                <ul className="pl-3 mt-0.5 space-y-0.5">
                  {child.children.map((grandchild) => (
                    <li key={grandchild.category.id}>
                      <button
                        onClick={() => router.push(`/?cat=${grandchild.category.slug}`)}
                        className={`block w-full text-left text-xs transition-colors ${
                          activeSlug === grandchild.category.slug
                            ? 'text-blue-600 font-medium'
                            : 'text-gray-500 hover:text-blue-600'
                        }`}
                      >
                        {grandchild.category.nombre}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function MegaMenu({
  node,
  activeSlug,
  router,
}: {
  node: CategoryNode;
  activeSlug: string | null;
  router: ReturnType<typeof useRouter>;
}) {
  const children = node.children;

  return (
    <div className="absolute top-full left-1/2 -translate-x-1/2 mt-0.5 bg-white border rounded-lg shadow-xl z-50 p-4 sm:p-6 w-[800px] max-w-[90vw]">
      <div
        className="grid gap-4 sm:gap-6"
        style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))' }}
      >
        {children.map((child) => (
          <CategoryColumn
            key={child.category.id}
            node={child}
            activeSlug={activeSlug}
            router={router}
          />
        ))}
      </div>
    </div>
  );
}

export default function CategoryNav() {
  const [tree, setTree] = useState<CategoryNode[]>([]);
  const [openId, setOpenId] = useState<number | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout>>(undefined as any);
  const router = useRouter();
  const searchParams = useSearchParams();
  const activeSlug = searchParams.get('cat');

  useEffect(() => {
    categoriasPublicApi.listar().then((all) => {
      setTree(buildCategoryTree(all));
    });
  }, []);

  const scheduleClose = () => {
    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setOpenId(null), 200);
  };

  const cancelClose = () => {
    clearTimeout(timeoutRef.current);
  };

  if (tree.length === 0) return null;

  return (
    <nav className="bg-white">
      <div className="container mx-auto px-4">
        <ul className="flex items-center justify-center gap-0.5 py-2 overflow-x-auto scrollbar-hide lg:flex-wrap" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
          {tree.map((node) => {
            const hasChildren = node.children.length > 0;
            const isActive = activeSlug === node.category.slug;
            const isOpen = openId === node.category.id;

            return (
              <li
                key={node.category.id}
                className="relative"
                onMouseEnter={() => { cancelClose(); setOpenId(node.category.id); }}
                onMouseLeave={scheduleClose}
              >
                <button
                  onClick={() => router.push(`/?cat=${node.category.slug}`)}
                  className={`px-3 py-1.5 text-sm rounded-md whitespace-nowrap transition-colors flex items-center gap-1 ${
                    isActive
                      ? 'text-blue-600 bg-blue-50 font-medium'
                      : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                  }`}
                >
                  {node.category.nombre}
                  {hasChildren && (
                    <ChevronDown className={`h-3 w-3 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
                  )}
                </button>

                {hasChildren && isOpen && (
                  <div
                    onMouseEnter={cancelClose}
                    onMouseLeave={scheduleClose}
                  >
                    <MegaMenu
                      node={node}
                      activeSlug={activeSlug}
                      router={router}
                    />
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

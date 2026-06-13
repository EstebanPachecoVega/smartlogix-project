'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { categoriasPublicApi } from '@/lib/api';
import { buildCategoryTree, CategoryNode } from '@/lib/categoryTree';
import { ChevronRight } from 'lucide-react';

function SubmenuList({ nodes, router, activeSlug, parentHovered, onChildHover }: {
  nodes: CategoryNode[];
  router: ReturnType<typeof useRouter>;
  activeSlug: string | null;
  parentHovered: boolean | null;
  onChildHover: (hovered: boolean) => void;
}) {
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  return (
    <>
      {nodes.map((node, i) => {
        const hasChildren = node.children.length > 0;
        return (
          <div
            key={node.category.id}
            className="relative"
            onMouseEnter={() => { setOpenIndex(i); onChildHover(true); }}
            onMouseLeave={() => { setOpenIndex(null); onChildHover(false); }}
          >
            <button
              onClick={() => router.push(`/?cat=${node.category.slug}`)}
              className={`w-full text-left px-3 py-2 text-sm whitespace-nowrap transition-colors flex items-center justify-between gap-2 ${
                activeSlug === node.category.slug
                  ? 'text-blue-600 bg-blue-50 font-medium'
                  : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <span>{node.category.nombre}</span>
              {hasChildren && <ChevronRight className="h-3 w-3 shrink-0" />}
            </button>
            {hasChildren && openIndex === i && (
              <div
                className="absolute left-full top-0 bg-white border rounded-lg shadow-lg min-w-[180px] py-1 z-50 ml-0.5"
                onMouseEnter={() => setOpenIndex(i)}
                onMouseLeave={() => setOpenIndex(null)}
              >
                <SubmenuList
                  nodes={node.children}
                  router={router}
                  activeSlug={activeSlug}
                  parentHovered={null}
                  onChildHover={() => {}}
                />
              </div>
            )}
          </div>
        );
      })}
    </>
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
        <ul className="flex items-center justify-center gap-0.5 py-2 flex-wrap">
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
                    <ChevronRight className={`h-3 w-3 transition-transform ${isOpen ? 'rotate-90' : ''}`} />
                  )}
                </button>

                {hasChildren && (
                  <div
                    className={`absolute top-full left-0 mt-0.5 bg-white border rounded-lg shadow-lg min-w-[180px] py-1 z-50 ${isOpen ? 'block' : 'hidden'}`}
                    onMouseEnter={cancelClose}
                    onMouseLeave={scheduleClose}
                  >
                    <SubmenuList
                      nodes={node.children}
                      router={router}
                      activeSlug={activeSlug}
                      parentHovered={null}
                      onChildHover={() => {}}
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

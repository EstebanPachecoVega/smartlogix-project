import { Categoria } from '@/types';

export interface CategoriaNode extends Categoria {
  nivel: number;
  children: CategoriaNode[];
}

export type CategoryNode = CategoriaNode;

export function buildTree(categorias: Categoria[]): CategoriaNode[] {
  const map = new Map<number, CategoriaNode>();
  const roots: CategoriaNode[] = [];

  for (const cat of categorias) {
    map.set(cat.id, { ...cat, nivel: 0, children: [] });
  }

  for (const node of map.values()) {
    if (node.padreId && map.has(node.padreId)) {
      map.get(node.padreId)!.children.push(node);
    } else {
      roots.push(node);
    }
  }

  const setNivel = (nodes: CategoriaNode[], nivel: number) => {
    for (const n of nodes) {
      n.nivel = nivel;
      setNivel(n.children, nivel + 1);
    }
  };
  setNivel(roots, 0);

  const sortByOrden = (nodes: CategoriaNode[]) => {
    nodes.sort((a, b) => (a.ordenVisual ?? 0) - (b.ordenVisual ?? 0));
    for (const n of nodes) sortByOrden(n.children);
  };
  sortByOrden(roots);

  return roots;
}

export function flattenTree(nodes: CategoriaNode[]): CategoriaNode[] {
  const result: CategoriaNode[] = [];
  for (const node of nodes) {
    result.push(node);
    result.push(...flattenTree(node.children));
  }
  return result;
}

export function getNextOrden(categorias: Categoria[], padreId?: number): number {
  const siblings = categorias.filter((c) => c.padreId === padreId);
  const max = Math.max(0, ...siblings.map((c) => c.ordenVisual ?? 0));
  return max + 1;
}

export function getDescendantIds(categorias: Categoria[], id: number): number[] {
  const ids: number[] = [id];
  const children = categorias.filter((c) => c.padreId === id);
  for (const child of children) {
    ids.push(...getDescendantIds(categorias, child.id));
  }
  return ids;
}

export function getSiblings(categorias: Categoria[], padreId?: number): Categoria[] {
  return categorias
    .filter((c) => c.padreId === padreId)
    .sort((a, b) => (a.ordenVisual ?? 0) - (b.ordenVisual ?? 0));
}

// Alias for backward compatibility
export const buildCategoryTree = buildTree;

// Build breadcrumb chain from a category up to the root
export function buildCategoryChainById(categoriaId: number, categorias: Categoria[]): Categoria[] {
  const chain: Categoria[] = [];
  const map = new Map<number, Categoria>();
  for (const cat of categorias) map.set(cat.id, cat);

  let current = map.get(categoriaId);
  while (current) {
    chain.unshift(current);
    current = current.padreId ? map.get(current.padreId) : undefined;
  }
  return chain;
}

// Returns a Set of all descendant IDs (including the parent itself)
export function getAllDescendantIds(parentId: number, allCats: Categoria[]): Set<number> {
  const ids = new Set<number>();
  const collect = (id: number) => {
    ids.add(id);
    for (const cat of allCats) {
      if (cat.padreId === id) collect(cat.id);
    }
  };
  collect(parentId);
  return ids;
}

import { Categoria } from '@/types';

export function buildCategoryChainById(categoriaId: number, allCats: Categoria[]): Categoria[] {
  const chain: Categoria[] = [];
  let current = allCats.find((c) => c.id === categoriaId);
  while (current) {
    chain.unshift(current);
    current = current.padreId ? allCats.find((c) => c.id === current!.padreId) : undefined;
  }
  return chain;
}

export function getAllDescendantIds(categoriaId: number, allCats: Categoria[]): Set<number> {
  const ids = new Set<number>([categoriaId]);
  allCats
    .filter((c) => c.padreId === categoriaId)
    .forEach((child) => {
      getAllDescendantIds(child.id, allCats).forEach((id) => ids.add(id));
    });
  return ids;
}

export interface CategoryNode {
  category: Categoria;
  children: CategoryNode[];
}

export function buildCategoryTree(allCats: Categoria[]): CategoryNode[] {
  const childMap: Record<number, CategoryNode[]> = {};
  const roots: CategoryNode[] = [];

  const sorted = [...allCats].sort((a, b) => (a.ordenVisual ?? 999) - (b.ordenVisual ?? 999));

  sorted.forEach((cat) => {
    const node: CategoryNode = { category: cat, children: [] };
    if (cat.padreId) {
      if (!childMap[cat.padreId]) childMap[cat.padreId] = [];
      childMap[cat.padreId].push(node);
    } else {
      roots.push(node);
    }
  });

  Object.values(childMap).forEach((children) =>
    children.sort((a, b) => (a.category.ordenVisual ?? 999) - (b.category.ordenVisual ?? 999))
  );

  function attachChildren(node: CategoryNode) {
    node.children = childMap[node.category.id] || [];
    node.children.forEach(attachChildren);
  }

  roots.forEach(attachChildren);

  return roots;
}

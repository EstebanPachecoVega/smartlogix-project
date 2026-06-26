import { Page } from '@playwright/test';

const BFF_URL = process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8084/bff';

async function api(page: Page, method: string, path: string, data?: object) {
  const session = await page.evaluate(() =>
    fetch('/api/auth/session').then(r => r.json())
  );
  const res = await page.evaluate(
    async ({ method, path, data, token }) => {
      const r = await fetch(path, {
        method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: data ? JSON.stringify(data) : undefined,
      });
      return { status: r.status, body: await r.json().catch(() => ({})) };
    },
    { method, path: `${BFF_URL}${path}`, data, token: session?.accessToken }
  );
  return res;
}

async function apiGetAll<T>(page: Page, path: string): Promise<T[]> {
  const all: T[] = [];
  let pageNum = 0;
  const pageSize = 100;
  while (true) {
    const res = await api(page, 'GET', `${path}?page=${pageNum}&size=${pageSize}`);
    const data = res.body as any;
    const content: T[] = Array.isArray(data) ? data : data.content || [];
    if (content.length === 0) break;
    all.push(...content);
    if (content.length < pageSize) break;
    pageNum++;
  }
  return all;
}

export async function seedCategoria(page: Page, nombre: string, padreId?: number) {
  return api(page, 'POST', '/logistica/categorias', {
    nombre,
    slug: nombre.toLowerCase().replace(/\s+/g, '-'),
    categoriaPadreId: padreId || null,
    orden: 1,
  });
}

export async function seedProducto(
  page: Page,
  overrides: Partial<{
    sku: string; nombre: string; slug: string; descripcion: string;
    precio: number; stock: number; categoriaId: number; activo: boolean;
  }> = {}
) {
  const defaults = {
    sku: `SKU-E2E-${Date.now()}`,
    nombre: `E2E Product ${Date.now()}`,
    slug: `e2e-product-${Date.now()}`,
    descripcion: 'Producto de prueba E2E',
    precio: 14990,
    stock: 50,
    categoriaId: 1,
    activo: true,
  };
  return api(page, 'POST', '/logistica/productos', { ...defaults, ...overrides });
}

export async function seedPedido(page: Page, usuarioId: string) {
  return api(page, 'POST', '/pedidos', {
    usuarioId,
    destinatario: 'Test E2E',
    calle: 'Calle Test 123',
    numero: '456',
    comuna: 'Santiago',
    ciudad: 'Santiago',
    codigoPostal: '8320000',
    metodoEnvio: 'standard',
    plataforma: 'DESKTOP',
    items: [{
      productoId: 1,
      sku: 'SKU-E2E',
      nombreProducto: 'Producto E2E',
      precioUnitario: 1000,
      cantidad: 1,
      imagenPrincipal: '',
    }],
  });
}

export async function deleteProducto(page: Page, id: number) {
  return api(page, 'DELETE', `/logistica/productos/${id}`);
}

export async function deleteCategoria(page: Page, id: number) {
  return api(page, 'DELETE', `/logistica/categorias/${id}`);
}

export async function cleanupE2EData(page: Page) {
  const results = { productos: 0, categorias: 0, errors: 0 };

  // Limpiar productos E2E
  try {
    const productos = await apiGetAll<{ id: number; sku: string }>(page, '/logistica/productos');
    for (const p of productos) {
      if (p.sku && p.sku.startsWith('SKU-E2E-')) {
        const res = await deleteProducto(page, p.id);
        if (res.status >= 200 && res.status < 300) results.productos++;
        else results.errors++;
      }
    }
  } catch (e) {
    console.warn('cleanupE2EData: error limpiando productos', e);
  }

  // Limpiar categorías E2E
  try {
    const categorias = await apiGetAll<{ id: number; nombre: string }>(page, '/logistica/categorias');
    const e2eCats = categorias.filter(c => c.nombre && c.nombre.startsWith('Cat E2E '));
    // Eliminar de abajo hacia arriba (subcategorías primero)
    const sorted = [...e2eCats].sort((a, b) => b.id - a.id);
    for (const c of sorted) {
      const res = await deleteCategoria(page, c.id);
      if (res.status >= 200 && res.status < 300) results.categorias++;
      else results.errors++;
    }
  } catch (e) {
    console.warn('cleanupE2EData: error limpiando categorías', e);
  }

  return results;
}

export async function listPedidos(page: Page) {
  const session = await page.evaluate(() =>
    fetch('/api/auth/session').then(r => r.json())
  );
  const res = await page.evaluate(
    async ({ path, token }) => {
      const r = await fetch(path, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return r.json();
    },
    { path: `${BFF_URL}/pedidos`, token: session?.accessToken }
  );
  return Array.isArray(res) ? res : res.content || [];
}

export async function listEnvios(page: Page) {
  const session = await page.evaluate(() =>
    fetch('/api/auth/session').then(r => r.json())
  );
  const res = await page.evaluate(
    async ({ path, token }) => {
      const r = await fetch(path, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return r.json();
    },
    { path: `${BFF_URL}/envios`, token: session?.accessToken }
  );
  return Array.isArray(res) ? res : res.content || [];
}

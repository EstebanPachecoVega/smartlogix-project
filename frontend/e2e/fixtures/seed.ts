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
    sku: `SKU-${Date.now()}`,
    nombre: `Producto Test ${Date.now()}`,
    slug: `producto-test-${Date.now()}`,
    descripcion: 'Producto de prueba E2E',
    precio: 9990,
    stock: 100,
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
  }, crypto.randomUUID());
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

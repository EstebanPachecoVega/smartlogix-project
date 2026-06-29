/**
 * Global setup: limpia datos E2E residuales de ejecuciones anteriores.
 * Usa Keycloak admin para obtener un token de gestor y limpiar vía API.
 */
import { request } from '@playwright/test';

const GATEWAY_URL = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:8080';
const KEYCLOAK_ISSUER = process.env.KEYCLOAK_ISSUER || 'http://localhost:8180/realms/smartlogix';
const ADMIN_USER = process.env.KEYCLOAK_ADMIN_USER || 'admin';
const ADMIN_PASS = process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin';
const GESTOR_EMAIL = process.env.E2E_GESTOR_EMAIL || 'gestor@smartlogix.com';
const GESTOR_PASS = process.env.E2E_GESTOR_PASSWORD || 'Gestor123!';
const CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID || 'smartlogix-frontend';

async function getGestorToken(baseUrl: string, adminToken: string) {
  const adminCtx = await request.newContext({ timeout: 30000 });

  // Buscar usuario gestor
  const usersRes = await adminCtx.get(
    `${baseUrl}/admin/realms/smartlogix/users?email=${encodeURIComponent(GESTOR_EMAIL)}`,
    { headers: { Authorization: `Bearer ${adminToken}` } }
  );
  const users = await usersRes.json();

  if (!users[0]?.id) {
    // Crear usuario gestor si no existe
    await adminCtx.post(`${baseUrl}/admin/realms/smartlogix/users`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        username: 'gestor',
        email: GESTOR_EMAIL,
        firstName: 'Gestor',
        lastName: 'Admin',
        enabled: true,
        emailVerified: true,
        credentials: [{ type: 'password', value: GESTOR_PASS, temporary: false }],
      },
    });
    // Esperar y obtener ID
    const retryRes = await adminCtx.get(
      `${baseUrl}/admin/realms/smartlogix/users?email=${encodeURIComponent(GESTOR_EMAIL)}`,
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    const retryUsers = await retryRes.json();
    if (retryUsers[0]?.id) {
      await adminCtx.post(
        `${baseUrl}/admin/realms/smartlogix/users/${retryUsers[0].id}/role-mappings/realm`,
        {
          headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
          data: [{ name: 'gestor' }],
        }
      );
    }
  }

  await adminCtx.dispose();

  // Login como gestor para obtener token
  const loginCtx = await request.newContext({ timeout: 15000 });
  const tokenRes = await loginCtx.post(
    `${KEYCLOAK_ISSUER}/protocol/openid-connect/token`,
    {
      form: {
        client_id: CLIENT_ID,
        username: GESTOR_EMAIL,
        password: GESTOR_PASS,
        grant_type: 'password',
      },
    }
  );
  const tokenBody = await tokenRes.json();
  await loginCtx.dispose();
  return tokenBody.access_token as string;
}

async function fetchAll(gestorToken: string, path: string) {
  const ctx = await request.newContext({
    baseURL: GATEWAY_URL,
    extraHTTPHeaders: { Authorization: `Bearer ${gestorToken}` },
  });
  const all: any[] = [];
  let page = 0;
  const size = 100;
  while (true) {
    const res = await ctx.get(`${path}?page=${page}&size=${size}`);
    const data = await res.json();
    const content = Array.isArray(data) ? data : data.content || [];
    if (content.length === 0) break;
    all.push(...content);
    if (content.length < size) break;
    page++;
  }
  await ctx.dispose();
  return all;
}

async function cleanupProducts(gestorToken: string) {
  const ctx = await request.newContext({
    baseURL: GATEWAY_URL,
    extraHTTPHeaders: { Authorization: `Bearer ${gestorToken}` },
  });
  const productos = await fetchAll(gestorToken, '/api/productos');
  let deleted = 0;
  for (const p of productos) {
    if (p.sku && (p.sku.startsWith('SKU-E2E-') || p.sku.startsWith('SKU-'))) {
      const name = p.nombre || '';
      if (name.startsWith('E2E Product ') || name.startsWith('Producto Test ')) {
        const delRes = await ctx.delete(`/api/productos/${p.id}`);
        if (delRes.ok()) deleted++;
      }
    }
  }
  await ctx.dispose();
  return deleted;
}

async function cleanupCategories(gestorToken: string) {
  const ctx = await request.newContext({
    baseURL: GATEWAY_URL,
    extraHTTPHeaders: { Authorization: `Bearer ${gestorToken}` },
  });
  const categorias = await fetchAll(gestorToken, '/api/categorias');
  const e2eCats = categorias.filter(
    (c: any) => c.nombre && c.nombre.startsWith('Cat E2E ')
  );
  // Eliminar de mayor a menor ID para evitar FK conflicts
  e2eCats.sort((a: any, b: any) => b.id - a.id);
  let deleted = 0;
  for (const c of e2eCats) {
    const delRes = await ctx.delete(`/api/categorias/${c.id}`);
    if (delRes.ok()) deleted++;
  }
  await ctx.dispose();
  return deleted;
}

export default async function globalSetup() {
  console.log('\n[globalSetup] Limpiando datos E2E residuales...');

  try {
    const baseUrl = KEYCLOAK_ISSUER.replace('/realms/smartlogix', '');
    const adminCtx = await request.newContext({ timeout: 30000 });

    const tokenRes = await adminCtx.post(
      `${baseUrl}/realms/master/protocol/openid-connect/token`,
      {
        form: {
          client_id: 'admin-cli',
          username: ADMIN_USER,
          password: ADMIN_PASS,
          grant_type: 'password',
        },
      }
    );
    const adminToken = (await tokenRes.json()).access_token;

    const gestorToken = await getGestorToken(baseUrl, adminToken);
    await adminCtx.dispose();

    const prodDeleted = await cleanupProducts(gestorToken);
    const catDeleted = await cleanupCategories(gestorToken);

    console.log(`[globalSetup] Eliminados: ${prodDeleted} productos, ${catDeleted} categorías E2E\n`);
  } catch (e) {
    console.warn('[globalSetup] Error durante limpieza (no bloqueante):', e);
  }
}

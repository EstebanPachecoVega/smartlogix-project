/**
 * Script standalone para limpiar datos E2E residuales de la BD.
 * Ejecutar: npx tsx e2e/cleanup-existing.ts
 */
const BFF_URL = 'http://localhost:8084/bff';
const GATEWAY_URL = 'http://localhost:8080';
const KEYCLOAK_ISSUER = 'http://localhost:8180/realms/smartlogix';

async function getGestorToken() {
  const baseUrl = 'http://localhost:8180';
  const adminUser = process.env.KEYCLOAK_ADMIN_USER || 'admin';
  const adminPass = process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin';
  const gestorEmail = 'gestor@smartlogix.com';
  const gestorPass = 'Gestor123!';

  // Get admin token
  const adminTokenRes = await fetch(`${baseUrl}/realms/master/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      client_id: 'admin-cli',
      username: adminUser,
      password: adminPass,
      grant_type: 'password',
    }),
  });
  const adminData = await adminTokenRes.json() as any;
  const adminToken = adminData.access_token;

  // Ensure gestor user exists with gestor role
  const usersRes = await fetch(
    `${baseUrl}/admin/realms/smartlogix/users?email=${encodeURIComponent(gestorEmail)}`,
    { headers: { Authorization: `Bearer ${adminToken}` } }
  );
  const users = await usersRes.json() as any[];

  if (!users[0]?.id) {
    // Create gestor user
    await fetch(`${baseUrl}/admin/realms/smartlogix/users`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: 'gestor',
        email: gestorEmail,
        firstName: 'Gestor',
        lastName: 'Admin',
        enabled: true,
        emailVerified: true,
        credentials: [{ type: 'password', value: gestorPass, temporary: false }],
      }),
    });
    // Wait and get user ID
    await new Promise(r => setTimeout(r, 1000));
    const retryRes = await fetch(
      `${baseUrl}/admin/realms/smartlogix/users?email=${encodeURIComponent(gestorEmail)}`,
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    const retryUsers = await retryRes.json() as any[];
    if (retryUsers[0]?.id) {
      // Assign gestor role
      const rolesRes = await fetch(
        `${baseUrl}/admin/realms/smartlogix/roles/gestor`,
        { headers: { Authorization: `Bearer ${adminToken}` } }
      );
      const role = await rolesRes.json() as any;
      await fetch(
        `${baseUrl}/admin/realms/smartlogix/users/${retryUsers[0].id}/role-mappings/realm`,
        {
          method: 'POST',
          headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
          body: JSON.stringify([{ id: role.id, name: role.name }]),
        }
      );
    }
  }

  // Login as gestor
  const tokenRes = await fetch(`${KEYCLOAK_ISSUER}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      client_id: 'smartlogix-frontend',
      username: gestorEmail,
      password: gestorPass,
      grant_type: 'password',
    }),
  });
  const tokenData = await tokenRes.json() as any;
  return tokenData.access_token as string;
}

async function fetchAll(token: string, path: string) {
  const all: any[] = [];
  let page = 0;
  const size = 100;
  while (true) {
    const res = await fetch(`${GATEWAY_URL}${path}?page=${page}&size=${size}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await res.json();
    const content = Array.isArray(data) ? data : data.content || [];
    if (content.length === 0) break;
    all.push(...content);
    if (content.length < size) break;
    page++;
  }
  return all;
}

async function main() {
  console.log('Obteniendo token de gestor...');
  const token = await getGestorToken();

  console.log('Listando productos E2E...');
  const productos = await fetchAll(token, '/api/productos');
  const e2eProducts = productos.filter(
    (p: any) =>
      (p.sku && (p.sku.startsWith('SKU-E2E-') || p.sku.startsWith('SKU-'))) &&
      (p.nombre && (p.nombre.startsWith('E2E Product ') || p.nombre.startsWith('Producto Test ')))
  );
  console.log(`  Encontrados: ${e2eProducts.length}`);

  let prodDeleted = 0;
  for (const p of e2eProducts) {
    const res = await fetch(`${GATEWAY_URL}/api/productos/${p.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) prodDeleted++;
  }
  console.log(`  Eliminados: ${prodDeleted}`);

  console.log('Listando categorías E2E...');
  const categorias = await fetchAll(token, '/api/categorias');
  const e2eCats = categorias.filter((c: any) => c.nombre && c.nombre.startsWith('Cat E2E '));
  console.log(`  Encontradas: ${e2eCats.length}`);
  e2eCats.sort((a: any, b: any) => b.id - a.id);

  let catDeleted = 0;
  for (const c of e2eCats) {
    const res = await fetch(`${GATEWAY_URL}/api/categorias/${c.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) catDeleted++;
  }
  console.log(`  Eliminadas: ${catDeleted}`);

  console.log('Listando pedidos...');
  const pedidos = await fetchAll(token, '/api/pedidos');
  const e2ePedidos = pedidos.filter((p: any) =>
    p.destinatario === 'Test E2E' ||
    (p.calle === 'Calle Test 123' && p.codigoPostal === '8320000')
  );
  console.log(`  Encontrados: ${pedidos.length} (E2E: ${e2ePedidos.length})`);

  let pedidosDeleted = 0;
  for (const p of e2ePedidos) {
    const res = await fetch(`${GATEWAY_URL}/api/pedidos/${p.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) pedidosDeleted++;
  }
  console.log(`  Eliminados: ${pedidosDeleted}`);

  console.log('Listando envíos...');
  const envios = await fetchAll(token, '/api/envios');
  const e2ePedidoIds = new Set(e2ePedidos.map((p: any) => p.id));
  const e2eEnvios = envios.filter((e: any) => e2ePedidoIds.has(e.pedidoId));
  console.log(`  Encontrados: ${envios.length} (E2E: ${e2eEnvios.length})`);

  let enviosDeleted = 0;
  for (const e of e2eEnvios) {
    const res = await fetch(`${GATEWAY_URL}/api/envios/${e.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (res.ok) enviosDeleted++;
  }
  console.log(`  Eliminados: ${enviosDeleted}`);

  console.log(`\nLimpieza completada: ${prodDeleted} productos, ${catDeleted} categorías, ${pedidosDeleted} pedidos, ${enviosDeleted} envíos`);
}

main().catch(console.error);

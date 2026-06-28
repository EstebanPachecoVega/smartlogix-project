import { Page, request } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

interface TestUser {
  email: string;
  password: string;
  name: string;
  role: 'cliente' | 'gestor';
}

export async function registerUser(user: TestUser) {
  const ctx = await request.newContext({ timeout: 15000 });
  const [firstName, ...lastParts] = user.name.split(' ');
  const lastName = lastParts.join(' ') || firstName;
  const res = await ctx.post(`${BASE_URL}/api/auth/register`, {
    data: {
      primerNombre: firstName,
      primerApellido: lastName,
      email: user.email,
      password: user.password,
    },
  });
  const body = await res.text();
  await ctx.dispose();
  return res.ok();
}

export async function loginAs(page: Page, email: string, password: string) {
  await page.goto('/login', { timeout: 15000, waitUntil: 'domcontentloaded' });
  await page.getByLabel('Email').fill(email);
  await page.getByLabel(/contraseña/i).fill(password);
  
  const btn = page.locator('form').getByRole('button', { name: /iniciar sesión/i });
  await btn.click();
  
  const errorLocator = page.getByText(/credenciales incorrectas|incorrectos/i);
  const homeUrl = new URL('/', BASE_URL);
  
  try {
    await Promise.race([
      page.waitForURL(url => url.origin === homeUrl.origin && url.pathname === '/', { timeout: 30000 }),
      errorLocator.waitFor({ state: 'visible', timeout: 20000 }).then(() => { throw new Error('login error shown'); }),
    ]);
  } catch (e: any) {
    if (e.message === 'login error shown') {
      throw new Error(`Login failed: bad credentials for ${email}`);
    }
    throw e;
  }
  await page.waitForLoadState('domcontentloaded').catch(() => {});
  await page.waitForTimeout(400);
}

async function assignRole(email: string, roleName: string) {
  try {
    const tokenBase = process.env.KEYCLOAK_ISSUER || 'http://localhost:8180/realms/smartlogix';
    const baseUrl = tokenBase.replace('/realms/smartlogix', '');
    
    const adminCtx = await request.newContext({ timeout: 15000 });
    const tokenRes = await adminCtx.post(`${baseUrl}/realms/master/protocol/openid-connect/token`, {
      form: {
        client_id: 'admin-cli',
        username: process.env.KEYCLOAK_ADMIN_USER || 'admin',
        password: process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin',
        grant_type: 'password',
      },
    });
    const tokenBody = await tokenRes.json();
    const adminToken = tokenBody.access_token;
    
    const usersRes = await adminCtx.get(`${baseUrl}/admin/realms/smartlogix/users?email=${encodeURIComponent(email)}`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    const users = await usersRes.json();
    const userId = users[0]?.id;
    if (!userId) { await adminCtx.dispose(); return false; }
    
    const rolesRes = await adminCtx.get(`${baseUrl}/admin/realms/smartlogix/roles/${roleName}`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    const role = await rolesRes.json();
    
    const assignRes = await adminCtx.post(`${baseUrl}/admin/realms/smartlogix/users/${userId}/role-mappings/realm`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: [{ id: role.id, name: role.name }],
    });
    
    await adminCtx.dispose();
    return assignRes.ok();
  } catch (e) {
    console.warn(`assignRole ${email} as ${roleName}: failed`, e);
    return false;
  }
}

export async function createTestUsers() {
  const cliente: TestUser = {
    email: `e2e-cliente-${Date.now()}@test.com`,
    password: 'Test123!',
    name: 'Cliente E2E',
    role: 'cliente',
  };
  const gestor: TestUser = {
    email: `e2e-gestor-${Date.now()}@test.com`,
    password: 'Test123!',
    name: 'Gestor E2E',
    role: 'gestor',
  };
  const cOk = await registerUser(cliente);
  const gOk = await registerUser(gestor);
  if (gOk) { await assignRole(gestor.email, 'gestor'); }
  return { cliente, gestor, ok: cOk && gOk };
}

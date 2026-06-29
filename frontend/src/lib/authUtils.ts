import { getSession } from "next-auth/react";

export async function getUserRoles(): Promise<string[]> {
  const session = await getSession();
  return session?.roles || [];
}

export async function hasRole(role: string): Promise<boolean> {
  const roles = await getUserRoles();
  return roles.some((r: string) => r.toLowerCase() === role.toLowerCase());
}

export function hasRoleSync(session: any, role: string): boolean {
  return session?.roles?.some((r: string) => r.toLowerCase() === role.toLowerCase()) || false;
}
// src/lib/userId.ts
/**
 * Convierte un sub (UUID) en un número entero determinístico.
 * Este método es solo para desarrollo. En producción, el microservicio de usuarios
 * asignará IDs numéricos reales.
 */
export function getNumericUserId(sub: string): number {
    let hash = 0;
    for (let i = 0; i < sub.length; i++) {
        hash = (hash << 5) - hash + sub.charCodeAt(i);
        hash |= 0; // Convertir a entero de 32 bits
    }
    return Math.abs(hash) % 1000000000;
}

/**
 * Obtiene el ID numérico del usuario actual.
 * Actualmente usa el hash del sub. En el futuro, llamará al microservicio.
 */
export async function getCurrentUserId(): Promise<number | null> {
    const { getSession } = await import('next-auth/react');
    const session = await getSession();
    if (!session?.sub) return null;
    return getNumericUserId(session.sub);
}
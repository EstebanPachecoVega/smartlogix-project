// src/lib/userService.ts
import { getSession } from "next-auth/react";
import { getNumericUserId } from "./userId";

export interface UserProfile {
    primerNombre: string;
    segundoNombre: string;
    primerApellido: string;
    segundoApellido: string;
    calle: string;
    numero: string;
    comuna: string;
    ciudad: string;
    codigoPostal: string;
}

// Clave para localStorage (se usa solo en modo simulación)
const STORAGE_KEY_PREFIX = "perfil_";

/**
 * Obtiene el perfil del usuario.
 * Actualmente usa localStorage. Futuro: GET /api/usuarios/perfil
 */
export async function getUserProfile(): Promise<UserProfile | null> {
    const session = await getSession();
    if (!session?.sub) return null;

    // --- SIMULACIÓN LOCALSTORAGE (reemplazar por API) ---
    const storageKey = `${STORAGE_KEY_PREFIX}${session.sub}`;
    const saved = localStorage.getItem(storageKey);
    if (saved) {
        return JSON.parse(saved);
    }
    // Si no hay perfil, inicializa con datos del nombre del token
    const fullName = session.user?.name || '';
    const nameParts = fullName.split(' ');
    const defaultProfile: UserProfile = {
        primerNombre: nameParts[0] || '',
        segundoNombre: nameParts[1] || '',
        primerApellido: nameParts[2] || '',
        segundoApellido: nameParts[3] || '',
        calle: '',
        numero: '',
        comuna: '',
        ciudad: '',
        codigoPostal: '',
    };
    return defaultProfile;
}

/**
 * Actualiza todo el perfil (incluyendo dirección).
 * Actualmente guarda en localStorage. Futuro: PUT /api/usuarios/perfil
 */
export async function updateUserProfile(profile: UserProfile): Promise<void> {
    const session = await getSession();
    if (!session?.sub) throw new Error('No hay sesión');
    const storageKey = `${STORAGE_KEY_PREFIX}${session.sub}`;
    localStorage.setItem(storageKey, JSON.stringify(profile));
    // Simular latencia
    await new Promise(resolve => setTimeout(resolve, 300));
}

/**
 * Actualiza solo la dirección del usuario.
 * Futuro: PATCH /api/usuarios/direccion
 */
export async function updateUserAddress(
    address: Partial<Pick<UserProfile, 'calle' | 'numero' | 'comuna' | 'ciudad' | 'codigoPostal'>>
): Promise<void> {
    const current = await getUserProfile();
    if (!current) throw new Error('Perfil no encontrado');
    const updated = { ...current, ...address };
    await updateUserProfile(updated);
}
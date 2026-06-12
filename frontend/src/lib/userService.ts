// src/lib/userService.ts
import { getSession } from "next-auth/react";
import { getNumericUserId } from "./userId";
import { capitalizeName, normalizeField } from "./normalize";

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

const STORAGE_KEY_PREFIX = "perfil_";

export async function getUserProfile(): Promise<UserProfile | null> {
    const session = await getSession();
    if (!session?.sub) return null;

    const storageKey = `${STORAGE_KEY_PREFIX}${session.sub}`;
    const saved = localStorage.getItem(storageKey);
    if (saved) {
        return JSON.parse(saved);
    }
    const fullName = session.user?.name || '';
    const nameParts = fullName.split(' ');
    const defaultProfile: UserProfile = {
        primerNombre: nameParts[0] || '',
        segundoNombre: nameParts.length >= 3 ? nameParts[1] || '' : '',
        primerApellido: nameParts.length >= 2 ? nameParts[nameParts.length - 1] || '' : '',
        segundoApellido: nameParts.length >= 4 ? nameParts[2] || '' : '',
        calle: '',
        numero: '',
        comuna: '',
        ciudad: '',
        codigoPostal: '',
    };
    return defaultProfile;
}

export async function updateUserProfile(profile: UserProfile): Promise<void> {
    const session = await getSession();
    if (!session?.sub) throw new Error('No hay sesión');

    const current = await getUserProfile();
    const namesChanged = current && (
        current.primerNombre !== profile.primerNombre ||
        current.segundoNombre !== profile.segundoNombre ||
        current.primerApellido !== profile.primerApellido ||
        current.segundoApellido !== profile.segundoApellido
    );

    if (namesChanged) {
        const normalizedPrimerNombre = capitalizeName(profile.primerNombre);
        const normalizedSegundoNombre = capitalizeName(profile.segundoNombre);
        const normalizedPrimerApellido = capitalizeName(profile.primerApellido);
        const normalizedSegundoApellido = capitalizeName(profile.segundoApellido);

        try {
            await fetch('/api/auth/update-profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    primerNombre: normalizedPrimerNombre,
                    segundoNombre: normalizedSegundoNombre,
                    primerApellido: normalizedPrimerApellido,
                    segundoApellido: normalizedSegundoApellido,
                }),
            });
        } catch (e) {
            console.warn('No se pudo sincronizar con Keycloak:', e);
        }

        profile.primerNombre = normalizedPrimerNombre;
        profile.segundoNombre = normalizedSegundoNombre;
        profile.primerApellido = normalizedPrimerApellido;
        profile.segundoApellido = normalizedSegundoApellido;
    }

    const storageKey = `${STORAGE_KEY_PREFIX}${session.sub}`;
    localStorage.setItem(storageKey, JSON.stringify(profile));
    await new Promise(resolve => setTimeout(resolve, 300));
}

export async function updateUserAddress(
    address: Partial<Pick<UserProfile, 'calle' | 'numero' | 'comuna' | 'ciudad' | 'codigoPostal'>>
): Promise<void> {
    const current = await getUserProfile();
    if (!current) throw new Error('Perfil no encontrado');
    const normalized = {
        calle: normalizeField(address.calle || ''),
        numero: normalizeField(address.numero || ''),
        comuna: normalizeField(address.comuna || ''),
        ciudad: normalizeField(address.ciudad || ''),
        codigoPostal: normalizeField(address.codigoPostal || ''),
    };
    const updated = { ...current, ...normalized };
    await updateUserProfile(updated);
}

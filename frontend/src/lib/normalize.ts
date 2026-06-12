export function capitalizeName(value: string): string {
    return value
        .trim()
        .replace(/\s+/g, ' ')
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
        .join(' ');
}

export function normalizeField(value: string): string {
    return value.trim().replace(/\s+/g, ' ');
}

export function hasInvalidSpaces(value: string): boolean {
    return value !== value.trim() || /\s{2,}/.test(value);
}

export function validateFormSpaces<T extends Record<string, unknown>>(data: T, labels: Record<string, string> = {}): string | null {
    for (const [key, value] of Object.entries(data)) {
        if (typeof value === 'string' && hasInvalidSpaces(value)) {
            const label = labels[key] || key;
            return `El campo "${label}" contiene espacios al inicio, al final o espacios dobles.`;
        }
    }
    return null;
}

export function normalizeFormData<T extends Record<string, unknown>>(
    data: T,
    nameFields: (keyof T)[] = [],
): T {
    const result = { ...data };
    for (const key of Object.keys(result) as (keyof T)[]) {
        const value = result[key];
        if (typeof value === 'string') {
            if (nameFields.includes(key)) {
                (result as any)[key] = capitalizeName(value);
            } else {
                (result as any)[key] = normalizeField(value);
            }
        }
    }
    return result;
}

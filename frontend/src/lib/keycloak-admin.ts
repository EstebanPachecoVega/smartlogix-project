const realm = "smartlogix";

function getKeycloakBaseUrl(): string {
    const issuer = process.env.KEYCLOAK_INTERNAL_ISSUER || process.env.KEYCLOAK_ISSUER;
    if (!issuer) throw new Error("KEYCLOAK_ISSUER or KEYCLOAK_INTERNAL_ISSUER must be configured");
    return issuer.replace("/realms/smartlogix", "");
}

async function getAdminToken(): Promise<string> {
    const baseUrl = getKeycloakBaseUrl();
    const res = await fetch(`${baseUrl}/realms/master/protocol/openid-connect/token`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
            client_id: "admin-cli",
            username: process.env.KEYCLOAK_ADMIN_USER!,
            password: process.env.KEYCLOAK_ADMIN_PASSWORD!,
            grant_type: "password",
        }),
    });

    if (!res.ok) {
        const error = await res.json().catch(() => ({}));
        throw new Error((error as any).error_description || "Error al autenticar admin");
    }

    const data = await res.json();
    return data.access_token;
}

export async function updateKeycloakUser(userId: string, firstName: string, lastName: string): Promise<void> {
    const keycloakBaseUrl = getKeycloakBaseUrl();
    const adminToken = await getAdminToken();

    const res = await fetch(`${keycloakBaseUrl}/admin/realms/${realm}/users/${userId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${adminToken}`,
        },
        body: JSON.stringify({ firstName, lastName }),
    });

    if (!res.ok) {
        const error = await res.text();
        throw new Error(`Error al actualizar usuario en Keycloak: ${error}`);
    }
}

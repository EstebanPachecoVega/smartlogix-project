import { NextRequest, NextResponse } from "next/server";
import { capitalizeName, normalizeField } from "@/lib/normalize";

const realm = "smartlogix";

function getKeycloakBaseUrl(): string {
  const issuer = process.env.KEYCLOAK_INTERNAL_ISSUER || process.env.KEYCLOAK_ISSUER;
  if (!issuer) throw new Error("KEYCLOAK_ISSUER or KEYCLOAK_INTERNAL_ISSUER must be configured");
  return issuer.replace("/realms/smartlogix", "");
}

interface RegisterBody {
  primerNombre: string;
  segundoNombre?: string;
  primerApellido: string;
  email: string;
  password: string;
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

export async function POST(req: NextRequest) {
  try {
    const body: RegisterBody = await req.json();

    const normalizedPrimerNombre = capitalizeName(body.primerNombre || '');
    const normalizedSegundoNombre = body.segundoNombre ? capitalizeName(body.segundoNombre) : '';
    const normalizedPrimerApellido = capitalizeName(body.primerApellido || '');
    const normalizedEmail = normalizeField(body.email || '');
    const normalizedPassword = body.password || '';

    if (!normalizedPrimerNombre || !normalizedPrimerApellido || !normalizedEmail || !normalizedPassword) {
      return NextResponse.json(
        { error: "Todos los campos obligatorios deben estar completos" },
        { status: 400 }
      );
    }

    if (normalizedPassword.length < 6) {
      return NextResponse.json(
        { error: "La contraseña debe tener al menos 6 caracteres" },
        { status: 400 }
      );
    }

    const keycloakBaseUrl = getKeycloakBaseUrl();
    const adminToken = await getAdminToken();

    const firstName = normalizedSegundoNombre
      ? `${normalizedPrimerNombre} ${normalizedSegundoNombre}`
      : normalizedPrimerNombre;
    const lastName = normalizedPrimerApellido;

    const createRes = await fetch(`${keycloakBaseUrl}/admin/realms/${realm}/users`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${adminToken}`,
      },
      body: JSON.stringify({
        username: normalizedEmail,
        email: normalizedEmail,
        enabled: true,
        emailVerified: false,
        firstName,
        lastName,
        requiredActions: [],
        credentials: [
          {
            type: "password",
            value: normalizedPassword,
            temporary: false,
          },
        ],
      }),
    });

    if (!createRes.ok) {
      const error = await createRes.json().catch(() => ({}));
      const msg =
        (error as any).errorMessage ||
        (error as any).error_description ||
        `Error al crear usuario (${createRes.status})`;
      return NextResponse.json({ error: msg }, { status: createRes.status });
    }

    const location = createRes.headers.get("location");
    let userId = "";
    if (location) {
      userId = location.split("/").pop() || "";
    }

    if (userId) {
      const roleRes = await fetch(
        `${keycloakBaseUrl}/admin/realms/${realm}/roles/cliente`,
        {
          headers: { Authorization: `Bearer ${adminToken}` },
        }
      );

      if (roleRes.ok) {
        const roleData = await roleRes.json();
        const clienteRole = { id: roleData.id, name: roleData.name };

        const realmRolesRes = await fetch(
          `${keycloakBaseUrl}/admin/realms/${realm}/users/${userId}/role-mappings/realm`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${adminToken}`,
            },
            body: JSON.stringify([clienteRole]),
          }
        );

        if (!realmRolesRes.ok) {
          console.warn("No se pudo asignar rol cliente:", await realmRolesRes.text());
        }
      } else {
        console.warn("No se encontró el rol 'cliente' en Keycloak");
      }
    }

    return NextResponse.json(
      { message: "Usuario creado exitosamente" },
      { status: 201 }
    );
  } catch (error: any) {
    console.error("Error en registro:", error);
    return NextResponse.json(
      { error: error.message || "Error interno del servidor" },
      { status: 500 }
    );
  }
}

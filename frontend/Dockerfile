# Etapa 1: Instalar dependencias
FROM node:22-alpine AS deps
WORKDIR /app

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"

RUN corepack enable
COPY package.json pnpm-lock.yaml ./

# --ignore-scripts evita el bloqueo de build scripts en pnpm 11
RUN pnpm install --frozen-lockfile --ignore-scripts

# Etapa 2: Compilar la aplicación
FROM node:22-alpine AS builder
WORKDIR /app

ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
ENV NEXT_TELEMETRY_DISABLED=1

# Build args para variables NEXT_PUBLIC_* (se incrustan en el build de Next.js)
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_KEYCLOAK_URL
ARG NEXT_PUBLIC_BFF_URL
ARG NEXT_PUBLIC_GATEWAY_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_PUBLIC_KEYCLOAK_URL=${NEXT_PUBLIC_KEYCLOAK_URL}
ENV NEXT_PUBLIC_BFF_URL=${NEXT_PUBLIC_BFF_URL}
ENV NEXT_PUBLIC_GATEWAY_URL=${NEXT_PUBLIC_GATEWAY_URL}

RUN corepack enable
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Recompilar sharp nativamente (necesita binarios para la arquitectura del contenedor)
RUN apk add --no-cache python3 make g++ \
    && cd node_modules/sharp && npm run install --ignore-scripts=false || true

RUN pnpm build

# Etapa 3: Imagen final ligera
FROM node:22-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME=0.0.0.0

RUN addgroup -S nodejs -g 1001 && adduser -S nextjs -u 1001

COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000

CMD ["node", "server.js"]
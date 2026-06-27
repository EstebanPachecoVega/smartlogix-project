# SmartLogix

## Plataforma Inteligente de Gestión Logística basada en Microservicios

SmartLogix es una plataforma moderna de gestión logística diseñada bajo una arquitectura de microservicios, enfocada en la resiliencia, escalabilidad y mantenibilidad. El sistema implementa comunicación síncrona (REST + OpenFeign) y asíncrona (RabbitMQ), autenticación centralizada con Keycloak, manejo de errores bajo RFC 7807 y transacciones distribuidas mediante el patrón Saga Orquestado. Incluye un dashboard administrativo con analíticas y gráficos en tiempo real.

---

# Tabla de Contenidos

* [Descripción General](#descripción-general)
* [Arquitectura del Sistema](#arquitectura-del-sistema)
* [Microservicios](#microservicios)
* [Stack Tecnológico](#stack-tecnológico)
* [Autenticación y Autorización](#autenticación-y-autorización)
* [BFF y Endpoints de Agregación](#bff-y-endpoints-de-agregación)
* [Patrones y Estándares](#patrones-y-estándares)
* [Transacciones Distribuidas](#transacciones-distribuidas)
* [Manejo de Errores](#manejo-de-errores)
* [Infraestructura](#infraestructura)
* [Estructura del Proyecto](#estructura-del-proyecto)
* [Configuración del Entorno](#configuración-del-entorno)
* [Ejecución Local](#ejecución-local)
* [CI/CD](#cicd)
* [Observabilidad](#observabilidad)
* [Pruebas](#pruebas)
* [Frontend](#frontend)
* [Roadmap](#roadmap)
* [Contribución](#contribución)
* [Licencia](#licencia)

---

# Descripción General

SmartLogix optimiza la gestión logística mediante una arquitectura desacoplada y orientada a eventos, con separación clara entre el panel administrativo y la experiencia del cliente final.

El sistema permite:

* Gestión completa del ciclo de vida de pedidos.
* Administración de inventario con reservas en Redis.
* Coordinación logística de envíos.
* Dashboard administrativo con analíticas y gráficos.
* Panel de cliente con carrito de compras y checkout.
* Comunicación resiliente entre servicios (Circuit Breaker, Rate Limiting).
* Escalabilidad horizontal.
* Observabilidad centralizada (tracing, métricas, logs).
* Integración continua con análisis de calidad de código.

La solución utiliza el enfoque **Database-per-Service**: cada microservicio administra su propia base de datos MySQL independiente.

---

# Arquitectura del Sistema

## Diagrama de alto nivel

```text
                       Cliente (Navegador)
                            │
                            ▼
                     Next.js Frontend (:3000)
                       │          │
              NextAuth.js    Axios + JWT
                       │          │
                       ▼          ▼
                   Keycloak    BFF (:8084)
                   (:8180)        │
                                  ▼
                           API Gateway (:8080)
                           (JWT, CORS, Rate Limit)
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
   MS Inventario (:8081)   MS Pedidos (:8082)     MS Envíos (:8083)
   (Productos, Stock)      (Saga Orchestrator)    (Logística, Despachos)
          │                       │                       │
          └───────────────────────┼───────────────────────┘
                                  │
                           RabbitMQ (:5672)
                        (Eventos asíncronos)
                                  │
                          Redis (:6379)
                     (Cache, Rate Limit, Stock)
                                  │
          ┌───────────────────────┼───────────────────────┐
          ▼                       ▼                       ▼
   MySQL Inventario        MySQL Pedidos          MySQL Envíos
      (:3306)                 (:3307)                (:3308)
```

## Componentes principales

| Componente       | Puerto     | Responsabilidad                                                         |
| ---------------- | ---------- | ----------------------------------------------------------------------- |
| Frontend         | 3000       | Next.js 16 — landing pública, dashboard admin, panel cliente            |
| Keycloak         | 8180       | Autenticación OIDC, gestión de roles (gestor / cliente)                 |
| BFF              | 8084       | Backend-for-Frontend: agregación de datos y endpoints para el frontend  |
| API Gateway      | 8080       | Punto de entrada único, validación JWT, Rate Limiting, CORS             |
| MS Inventario    | 8081       | Catálogo de productos, categorías, stock con reservas en Redis          |
| MS Pedidos       | 8082       | Ciclo de vida del pedido, Saga Orquestado, idempotencia                 |
| MS Envíos        | 8083       | Procesamiento logístico, tracking de despachos                          |
| RabbitMQ         | 5672/15672 | Mensajería asíncrona (eventos Saga, compensaciones)                     |
| Redis            | 6379       | Rate limiting, cache, reservas temporales de stock                      |
| MySQL (×3)       | 3306-3308  | Persistencia — una instancia por microservicio                          |
| Zipkin           | 9411       | Trazas distribuidas                                                     |
| Prometheus       | 9090       | Recolección de métricas                                                 |
| Loki             | 3100       | Agregación de logs                                                      |
| Grafana          | 4000       | Dashboards de observabilidad                                            |

---

# Microservicios

## MS Inventario (puerto 8081)

Responsable de la gestión del catálogo de productos y el control de stock.

**Endpoints principales:**

| Método | Ruta                                  | Descripción                          |
| ------ | ------------------------------------- | ------------------------------------ |
| GET    | `/api/productos`                      | Listar productos (paginado, filtrable) |
| GET    | `/api/productos/{id}`                 | Obtener producto por ID              |
| GET    | `/api/productos/slug/{slug}`          | Obtener producto por slug            |
| GET    | `/api/productos/sku/{sku}`            | Obtener producto por SKU             |
| GET    | `/api/productos/categoria/{catId}`    | Productos por categoría              |
| GET    | `/api/productos/mapa-categorias`      | Mapa productoId → categoría          |
| POST   | `/api/productos`                      | Crear producto (solo gestor)         |
| PUT    | `/api/productos/{id}`                 | Actualizar producto (solo gestor)    |
| DELETE | `/api/productos/{id}`                 | Eliminar producto (solo gestor)      |
| GET    | `/api/categorias`                     | Listar categorías                    |
| GET    | `/api/categorias/{id}`                | Obtener categoría por ID             |
| GET    | `/api/categorias/slug/{slug}`         | Obtener categoría por slug           |
| GET    | `/api/categorias/principales`         | Categorías principales               |
| POST   | `/api/categorias`                     | Crear categoría (solo gestor)        |
| PUT    | `/api/categorias/{id}`                | Actualizar categoría (solo gestor)   |
| DELETE | `/api/categorias/{id}`                | Eliminar categoría (solo gestor)     |
| PATCH  | `/api/categorias/reordenar`           | Reordenar categorías (solo gestor)   |
| POST   | `/api/inventario/reservar`            | Reservar stock (consumido por Saga)  |
| POST   | `/api/inventario/confirmar`           | Confirmar reserva (consumido por Saga) |
| POST   | `/api/inventario/cancelar`            | Cancelar reserva (compensación Saga) |
| GET    | `/api/inventario/stock/{productoId}`  | Stock disponible del producto        |

**Reservas en Redis:** Las reservas de stock se gestionan en Redis con TTL para prevenir bloqueos permanentes. Si el TTL expira, se publica un evento `ReservaExpirada` para que ms-pedidos ejecute la compensación.

**Excepciones principales:**

```java
StockInsuficienteException
ProductoNoEncontradoException
```

---

## MS Pedidos (puerto 8082)

Orquestador central del sistema. Gestiona el ciclo de vida completo del pedido y ejecuta la lógica del Saga Orquestado.

**Endpoints principales:**

| Método | Ruta                                              | Descripción                              |
| ------ | ------------------------------------------------- | ---------------------------------------- |
| GET    | `/api/pedidos`                                    | Listar pedidos (paginado, filtrable por estado) |
| GET    | `/api/pedidos/{id}`                               | Obtener pedido por ID                    |
| GET    | `/api/pedidos/orden/{numeroOrden}`                | Obtener pedido por número de orden       |
| POST   | `/api/pedidos`                                    | Crear pedido (requiere idempotencyKey)   |
| DELETE | `/api/pedidos/{id}`                               | Eliminar pedido (solo gestor)            |
| GET    | `/api/pedidos/estadisticas/ventas-plataforma`     | Ventas agrupadas por plataforma          |
| GET    | `/api/pedidos/estadisticas/comparacion-anual`     | Comparación mes a mes (año actual vs anterior) |
| GET    | `/api/pedidos/estadisticas/ventas-por-producto`   | Ventas agrupadas por producto            |
| GET    | `/api/pedidos/estadisticas/ventas-por-producto-cantidad` | Cantidad vendida por producto       |

**Idempotencia:** La creación de pedidos requiere el header `Idempotency-Key`. Pedidos duplicados con la misma key retornan el pedido original sin crear uno nuevo (mecanismo `@Transactional` + Redis).

**Estados del pedido:**

| Estado       | Descripción                                      |
| ------------ | ------------------------------------------------ |
| PENDIENTE    | Pedido creado, pendiente de reserva de stock     |
| APROBADO     | Stock reservado, evento emitido a ms-envios      |
| RECHAZADO    | Stock insuficiente o error en el flujo           |
| EN_CAMINO    | Despacho en curso                                |
| ENTREGADO    | Entregado al cliente                             |

**Plataforma:** Cada pedido registra la plataforma de origen (`DESKTOP` o `MOBILE`), detectada automáticamente desde el frontend mediante el user agent del navegador.

**Excepciones principales:**

```java
EstadoPedidoInvalidoException
PedidoNoEncontradoException
```

---

## MS Envíos (puerto 8083)

Consume eventos desde RabbitMQ y gestiona el ciclo de vida logístico de los despachos.

**Endpoints principales:**

| Método | Ruta                              | Descripción                                   |
| ------ | --------------------------------- | --------------------------------------------- |
| GET    | `/api/envios`                     | Listar envíos (paginado, filtrable por estado) |
| GET    | `/api/envios/{id}`                | Obtener envío por ID                          |
| GET    | `/api/envios/pedido/{pedidoId}`   | Obtener envío por pedido                      |
| GET    | `/api/envios/tracking/{tracking}` | Obtener envío por número de tracking          |
| GET    | `/api/envios/problemas`           | Listar envíos con problemas                   |
| PATCH  | `/api/envios/{id}/estado`         | Actualizar estado (solo gestor)               |
| DELETE | `/api/envios/{id}`                | Eliminar envío (solo gestor)                  |

**Estados del envío:**

| Estado           | Descripción                      |
| ---------------- | -------------------------------- |
| PENDIENTE        | Despacho creado, sin procesar    |
| PREPARANDO       | En preparación para envío        |
| ENVIADO          | Despachado desde origen          |
| EN_TRANSITO      | En ruta de transporte            |
| EN_REPARTO       | En reparto local                 |
| ENTREGADO        | Entregado al destinatario        |
| INTENTO_FALLIDO  | Intento de entrega fallido       |
| RETRASADO        | Envío con retraso                |
| DEVUELTO         | Devuelto al remitente            |
| CANCELADO        | Despacho cancelado               |

**Consumidores RabbitMQ:**
- `PedidoAprobadoEvent` → crea automáticamente un nuevo envío.
- Publica `EnvioActualizadoEvent` cuando cambia el estado, para que ms-pedidos actualice el pedido correspondiente.

---

# Stack Tecnológico

## Backend

| Tecnología              | Versión       | Uso                                                |
| ----------------------- | ------------- | -------------------------------------------------- |
| Java                    | 21            | Lenguaje principal                                 |
| Spring Boot             | 3.5.14        | Framework de microservicios                        |
| Spring Cloud            | 2025.0.0      | Gateway, OpenFeign, Circuit Breaker                |
| Spring Cloud Gateway    | —             | API Gateway (reactivo, WebFlux)                    |
| Spring Cloud OpenFeign  | —             | Comunicación síncrona entre servicios              |
| Spring Data JPA         | —             | Persistencia relacional                            |
| Spring WebFlux          | —             | BFF reactivo para agregación                       |
| MapStruct               | —             | Conversión automática entidades ↔ DTOs             |
| Resilience4j            | —             | Circuit Breaker, Retry, Rate Limiter               |
| RabbitMQ                | 3.12          | Mensajería asíncrona (eventos Saga)                |
| MySQL                   | 8.0           | Base de datos relacional (3 instancias)            |
| Redis                   | 7.2           | Cache, rate limiting, reservas temporales          |
| Keycloak                | 24.0.1        | Autenticación OIDC, gestión de roles               |

## Frontend

| Tecnología              | Versión       | Uso                                                |
| ----------------------- | ------------- | -------------------------------------------------- |
| Next.js                 | 16.2.6        | Framework full-stack (App Router)                  |
| React                   | 19.2.4        | Librería de UI                                     |
| TypeScript              | 5             | Tipado estático                                    |
| Tailwind CSS            | 4             | Estilos utilitarios (PostCSS, sin config)          |
| Zustand                 | 4.5.2         | Manejo global de estado                            |
| NextAuth.js             | 4.24.14       | Autenticación OIDC con Keycloak                    |
| Axios                   | 1.6.0         | Cliente HTTP                                       |
| shadcn/ui               | 4.7.0         | Componentes de UI accesibles                       |
| recharts                | 3.8.1         | Gráficos interactivos (dashboard admin)            |
| React Hook Form         | —             | Manejo de formularios                              |
| Zod                     | 3.23.8        | Validación de esquemas                             |
| next-cloudinary         | 6.17.5        | Gestión de imágenes                                |
| Lucide React            | 1.16.0        | Iconografía                                        |
| Sonner                  | 2.0.7         | Notificaciones toast                               |

## DevOps y Calidad

| Herramienta             | Uso                                                     |
| ----------------------- | ------------------------------------------------------- |
| Docker + Docker Compose | Contenedores y orquestación local                       |
| Testcontainers          | Pruebas de integración con servicios reales             |
| JaCoCo                  | Cobertura de código (mínimo 90%)                        |
| JUnit 5 + Mockito       | Pruebas unitarias                                       |
| Playwright              | Pruebas E2E del frontend                                |
| SonarCloud              | Análisis estático de calidad de código                  |
| GitHub Actions          | CI/CD — build, test, E2E, SonarCloud                    |
| Zipkin                  | Trazas distribuidas                                     |
| Prometheus + Grafana    | Métricas y dashboards                                   |
| Loki                    | Agregación de logs                                      |

---

# Autenticación y Autorización

SmartLogix utiliza **Keycloak** como servidor de identidad OIDC, integrado con **NextAuth.js** en el frontend y con validación JWT a nivel de API Gateway.

## Flujo de autenticación

1. El usuario accede al frontend y es redirigido a la pantalla de login de Keycloak.
2. Tras autenticarse, Keycloak emite un JWT (access token) firmado con RS256.
3. El frontend almacena el token y lo envía en cada request al BFF/API Gateway.
4. El API Gateway valida el JWT contra el JWK Set de Keycloak antes de enrutar al microservicio correspondiente.
5. Los microservicios confían en el gateway (no revalidan el token).

## Roles y permisos

| Rol       | Acceso                                                       |
| --------- | ------------------------------------------------------------ |
| `gestor`  | Dashboard administrativo (`/logistica`), CRUD de productos, gestión de pedidos y envíos, analíticas |
| `cliente` | Panel de cliente (`/dashboard`), carrito, checkout, historial de pedidos, perfil |

## Usuarios preconfigurados

| Usuario       | Rol       | Email                          |
| ------------- | --------- | ------------------------------ |
| `diegoramos`  | gestor    | diego.smartlogix@gmail.com     |
| `juanperez`   | cliente   | juan.perez@gmail.com           |

Las contraseñas están definidas en la importación del realm (`infrastructure/smartlogix-realm.json`).

## Cliente Keycloak `smartlogix-frontend`

- Cliente público (sin client secret) con protocolo OIDC.
- Redirect URI: `http://localhost:3000/api/auth/callback/keycloak`
- Web origins: `http://localhost:3000`

---

# BFF y Endpoints de Agregación

El **BFF** (Backend-for-Frontend, puerto 8084) es una capa de agregación reactiva construida con Spring WebFlux. Orquesta datos de múltiples microservicios para que el frontend consuma un único endpoint.

## Endpoints del BFF

| Método | Ruta                                              | Descripción                                                  |
| ------ | ------------------------------------------------- | ------------------------------------------------------------ |
| GET    | `/bff/pedidos`                                    | Listar pedidos (agregados desde ms-pedidos)                  |
| GET    | `/bff/pedidos/{id}`                               | Obtener pedido por ID                                        |
| POST   | `/bff/pedidos`                                    | Crear pedido (requiere idempotencyKey)                       |
| GET    | `/bff/productos`                                  | Listar productos (desde ms-inventario)                       |
| GET    | `/bff/envios`                                     | Listar envíos (desde ms-envios)                              |
| GET    | `/bff/envios/{id}`                                | Obtener envío por ID                                         |
| GET    | `/bff/envios/pedido/{pedidoId}`                   | Obtener envío por pedido                                     |
| GET    | `/bff/envios/tracking/{tracking}`                 | Obtener envío por número de tracking                         |
| PATCH  | `/bff/envios/{id}/estado`                         | Actualizar estado de envío                                   |
| GET    | `/bff/logistica/productos`                        | CRUD productos (admin)                                       |
| POST   | `/bff/logistica/productos`                        | Crear producto (admin)                                       |
| GET    | `/bff/logistica/productos/{id}`                   | Obtener producto (admin)                                     |
| PUT    | `/bff/logistica/productos/{id}`                   | Actualizar producto (admin)                                  |
| DELETE | `/bff/logistica/productos/{id}`                   | Eliminar producto (admin)                                    |
| GET    | `/bff/logistica/categorias`                       | CRUD categorías (admin)                                      |
| POST   | `/bff/logistica/categorias`                       | Crear categoría (admin)                                      |
| PUT    | `/bff/logistica/categorias/{id}`                  | Actualizar categoría (admin)                                 |
| DELETE | `/bff/logistica/categorias/{id}`                  | Eliminar categoría (admin)                                   |
| GET    | `/bff/estadisticas/ventas-plataforma`             | Ventas agrupadas por plataforma (passthrough)                |
| GET    | `/bff/estadisticas/comparacion-anual`             | Comparación mes a mes (passthrough)                          |
| GET    | `/bff/estadisticas/ventas-por-categoria`          | Ventas por categoría (orquestado: combina ms-pedidos + ms-inventario) |

### Orquestación `ventas-por-categoria`

Este endpoint es el más complejo del BFF:
1. Llama a ms-pedidos para obtener `ventas-por-producto`.
2. Llama a ms-inventario para obtener `mapa-categorias` (productoId → nombreCategoria).
3. Agrupa los datos de ventas por categoría y retorna el resultado consolidado.

---

# Patrones y Estándares

## DTO Pattern

Está prohibido exponer entidades JPA directamente — todos los microservicios usan DTOs de request/response.

Estructura de paquetes:
```text
.dto.request
.dto.response
```

### Validaciones

Todos los Request DTOs utilizan Bean Validation:

```java
@NotNull
@Size(min = 1, max = 100)
@Min(0)
@Valid
```

---

## MapStruct

Conversión automática entre entidades, DTOs y Event DTOs:

```java
@Mapper(componentModel = "spring")
public interface PedidoMapper {
    PedidoResponseDTO toDTO(Pedido pedido);
    Pedido toEntity(CrearPedidoRequestDTO dto);
}
```

---

## Comunicación basada en eventos

Los eventos de RabbitMQ se modelan con DTOs ligeros que contienen solo la información mínima necesaria:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoAprobadoEventDTO {
    private Long pedidoId;
    private Long productoId;
    private Integer cantidad;
    private Instant timestamp;
}
```

### Eventos del sistema

| Evento                        | Publicador     | Consumidor   | Descripción                              |
| ----------------------------- | -------------- | ------------ | ---------------------------------------- |
| `PedidoAprobadoEvent`         | ms-pedidos     | ms-envios    | Inicia la creación de un despacho        |
| `EnvioActualizadoEvent`       | ms-envios      | ms-pedidos   | Notifica cambio de estado del envío      |
| `ReservaExpiradaEvent`        | ms-inventario  | ms-pedidos   | Notifica que una reserva Redis expiró    |

---

## Correlation ID

Todos los requests incluyen el header `X-Correlation-ID` para trazabilidad extremo a extremo. Se propaga automáticamente a través de:

- **API Gateway → Microservicios:** Filtro que lee/genera el ID en la entrada.
- **Microservicio → Microservicio (Feign):** `FeignCorrelationIdInterceptor` incluye el header en cada request saliente.
- **RabbitMQ:** Se incluye como propiedad del mensaje y se extrae en el consumer.

---

# Transacciones Distribuidas

## Saga Orquestado

El microservicio de pedidos (`ms-pedidos`) actúa como orquestador del Saga, coordinando la transacción distribuida que involucra inventario y envíos.

## Flujo completo

### 1. Creación del pedido (idempotente)
El frontend envía un `CrearPedidoRequestDTO` al BFF con el header `Idempotency-Key`. El BFF reenvía al API Gateway → ms-pedidos. Si la key ya existe, se retorna el pedido original.

### 2. Reserva de stock (síncrono vía OpenFeign)
Ms-pedidos llama a `POST /api/inventario/reservar` en ms-inventario. La reserva se almacena en Redis con un TTL configurable.

### 3. Éxito — flujo feliz
- El pedido cambia a estado `APROBADO`.
- Ms-pedidos publica `PedidoAprobadoEvent` en RabbitMQ.
- Ms-envios consume el evento y crea automáticamente un nuevo envío en estado `PENDIENTE`.
- El gestor actualiza el estado del envío → ms-envios publica `EnvioActualizadoEvent` → ms-pedidos actualiza el estado del pedido.

### 4. Fallo — compensación
Si ms-inventario rechaza la reserva (stock insuficiente):
- El pedido cambia a estado `RECHAZADO`.
- No se emite evento a ms-envios.

Si la reserva en Redis expira (TTL):
- Ms-inventario publica `ReservaExpiradaEvent`.
- Ms-pedidos consume el evento y ejecuta la compensación (cambia el pedido a `RECHAZADO`).

---

# Manejo de Errores

El sistema implementa el estándar **RFC 7807 — Problem Details for HTTP APIs**.

## Manejo global por servicio

Cada microservicio implementa:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Manejo centralizado de excepciones
}
```

## Estructura de respuesta de error

```json
{
  "type": "/errors/recurso-no-encontrado",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "El pedido con ID 10 no existe",
  "instance": "/api/pedidos/10",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Tipos de excepciones

### Negocio
* `StockInsuficienteException` — 409 Conflict
* `EstadoPedidoInvalidoException` — 422 Unprocessable Entity
* `EstadoEnvioInvalidoException` — 422 Unprocessable Entity

### Recursos
* `ProductoNoEncontradoException` — 404 Not Found
* `PedidoNoEncontradoException` — 404 Not Found
* `EnvioNoEncontradoException` — 404 Not Found

### Seguridad
* 401 Unauthorized — token ausente o inválido
* 403 Forbidden — rol insuficiente

### Validación
* `MethodArgumentNotValidException` — 400 Bad Request
* `HttpMessageNotReadableException` — 400 Bad Request

---

# Infraestructura

## Docker Compose — 16 servicios

| Servicio           | Imagen                        | Puerto Host | Puerto Container |
| ------------------ | ----------------------------- | ----------- | ---------------- |
| mysql-inventario   | mysql:8.0                     | 3306        | 3306             |
| mysql-pedidos      | mysql:8.0                     | 3307        | 3306             |
| mysql-envios       | mysql:8.0                     | 3308        | 3306             |
| rabbitmq           | rabbitmq:3.12-management      | 5672, 15672 | 5672, 15672      |
| redis              | redis:7.2-alpine              | 6379        | 6379             |
| ms-inventario      | build: ./backend/ms-inventario | 8081       | 8081             |
| ms-pedidos         | build: ./backend/ms-pedidos    | 8082       | 8082             |
| ms-envios          | build: ./backend/ms-envios     | 8083       | 8083             |
| api-gateway        | build: ./backend/api-gateway   | 8080       | 8080             |
| bff                | build: ./backend/bff           | 8084       | 8084             |
| frontend           | build: ./frontend              | 3000       | 3000             |
| keycloak           | quay.io/keycloak/keycloak:24.0.1 | 8180     | 8080             |
| zipkin             | openzipkin/zipkin:3           | 9411        | 9411             |
| prometheus         | prom/prometheus:v3.3.1        | 9090        | 9090             |
| loki               | grafana/loki:3.3.2            | 3100        | 3100             |
| grafana            | grafana/grafana:11.5.2        | 4000        | 3000             |

### Healthchecks
Todos los servicios de infraestructura (MySQL, RabbitMQ, Redis) tienen healthchecks configurados. Los microservicios usan `depends_on` con condiciones (`service_healthy` / `service_started`) para garantizar el orden de arranque.

### Perfiles en CI
El archivo `docker-compose.ci.yml` coloca Zipkin, Prometheus, Loki y Grafana bajo el perfil `observability`, de modo que no se inicien en CI para reducir consumo de recursos.

---

# Estructura del Proyecto

```text
smartlogix-project/
│
├── backend/                        # Microservicios Java (Maven multi-módulo)
│   ├── pom.xml                     # POM raíz (agregador)
│   ├── api-gateway/                # Spring Cloud Gateway (:8080)
│   ├── bff/                        # Backend-for-Frontend (:8084)
│   ├── ms-inventario/              # Productos y stock (:8081)
│   ├── ms-pedidos/                 # Saga Orchestrator (:8082)
│   └── ms-envios/                  # Despachos logísticos (:8083)
│
├── frontend/                       # Next.js 16 App Router
│   ├── src/
│   │   ├── app/
│   │   │   ├── logistica/          # Dashboard admin (sidebar layout)
│   │   │   ├── dashboard/          # Panel cliente (carrito, checkout, pedidos)
│   │   │   └── page.tsx            # Landing pública
│   │   ├── components/
│   │   │   ├── logistica/          # Sidebar, tablas, gráficos (recharts)
│   │   │   ├── cliente/            # ProductCard, ProductCarousel, HeroSlider
│   │   │   ├── ui/                 # Primitivas shadcn/ui (button, card, table...)
│   │   │   ├── layout/             # Navbar, Footer, SearchBar, CategoryNav
│   │   │   └── shared/             # Spinner
│   │   ├── lib/
│   │   │   ├── api.ts              # Clientes Axios + métodos de API
│   │   │   └── estados.ts          # Enums de estado + type guards + colores
│   │   ├── store/                  # Stores Zustand (carrito, envíos)
│   │   └── types/                  # PageResponse, Producto, Pedido, Envio...
│   └── package.json
│
├── infrastructure/                 # Configuraciones de infraestructura
│   ├── smartlogix-realm.json       # Realm de Keycloak preconfigurado
│   ├── prometheus.yml              # Configuración de Prometheus
│   └── grafana-datasources.yml     # Datasources de Grafana
│
├── .github/workflows/              # CI/CD pipelines
│   ├── ci.yml                      # Build + test backend (Java 21, Maven)
│   ├── e2e.yml                     # Playwright E2E tests
│   └── sonarqube-analysis.yml      # Análisis SonarCloud
│
├── test/                           # Utilidades de testing
├── docker-compose.yml              # Stack completo de desarrollo
├── docker-compose.ci.yml           # Override para CI (sin observabilidad)
├── .env                            # Variables de entorno para Docker Compose
└── README.md
```

---

# Configuración del Entorno

## Archivo `.env` (raíz del proyecto)

Variables utilizadas por Docker Compose:

```env
# MySQL
MYSQL_ROOT_PASSWORD=root
DB_INVENTARIO_NAME=smartlogix_inventario
DB_PEDIDOS_NAME=smartlogix_pedidos
DB_ENVIOS_NAME=smartlogix_envios

# RabbitMQ
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# Keycloak
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
JWT_JWK_SET_URI=http://keycloak:8080/realms/smartlogix/protocol/openid-connect/certs
CORS_ALLOWED_ORIGINS=http://localhost:3000

# NextAuth
NEXTAUTH_SECRET=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLW5leHRhdXRoLXRlc3RpbmcK
KEYCLOAK_INTERNAL_ISSUER=http://localhost:8180/realms/smartlogix
```

## Archivo `frontend/.env.local` (desarrollo local)

```env
NEXT_PUBLIC_BFF_URL=http://localhost:8084/bff
NEXT_PUBLIC_INVENTARIO_URL=http://localhost:8081
KEYCLOAK_ISSUER=http://localhost:8180/realms/smartlogix
KEYCLOAK_CLIENT_ID=smartlogix-frontend
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLW5leHRhdXRoLXRlc3RpbmcK
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=dtkxwlj5g
NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET=smartlogix_preset
```

---

# Ejecución Local

## Requisitos previos

- Docker y Docker Compose
- Java 21 (Temurin)
- Node.js 22
- pnpm

## 1. Clonar repositorio

```bash
git clone https://github.com/tu-organizacion/smartlogix.git
cd smartlogix
```

## 2. Levantar todo el stack con Docker

```bash
# Construir e iniciar todos los servicios en segundo plano
docker compose up -d --build

# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f ms-pedidos

# Reconstruir un solo servicio (ej. tras cambios en ms-inventario)
docker compose up -d --build ms-inventario

# Detener todo
docker compose down

# Detener todo y eliminar volúmenes (reinicio limpio)
docker compose down -v
```

Esto levanta los 16 servicios. Una vez iniciado:
- **Frontend:** http://localhost:3000
- **Keycloak:** http://localhost:8180 (admin / admin)
- **Zipkin:** http://localhost:9411
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:4000 (admin / admin)
- **RabbitMQ Management:** http://localhost:15672 (guest / guest)

## 3. Ejecutar backend en modo desarrollo (sin Docker)

Si prefieres ejecutar los microservicios directamente desde el IDE o terminal en lugar de usar los contenedores, asegúrate de tener las bases de datos, RabbitMQ y Redis corriendo (puedes levantar solo infraestructura con `docker compose up -d mysql-inventario mysql-pedidos mysql-envios rabbitmq redis keycloak`).

```bash
# Desde la raíz del proyecto
cd backend

# Ejecutar todos los microservicios (cada uno en su propia terminal)
./mvnw spring-boot:run -pl ms-inventario
./mvnw spring-boot:run -pl ms-pedidos
./mvnw spring-boot:run -pl ms-envios
./mvnw spring-boot:run -pl api-gateway
./mvnw spring-boot:run -pl bff

# Compilar todo el backend sin ejecutar
mvn clean compile -DskipTests
```

## 4. Ejecutar frontend

```bash
# Desarrollo (hot reload)
cd frontend
pnpm install
pnpm run dev

# Producción (build + start)
pnpm run build
pnpm start
```

---

# CI/CD

Los pipelines están definidos en `.github/workflows/` y se ejecutan en cada push y PR a las ramas `main` y `develop`.

## CI — Build y tests del backend

**Archivo:** `ci.yml`

- Ejecuta `mvn clean verify` en `backend/` con Java 21 (Temurin).
- Verifica cobertura con JaCoCo (mínimo 90% de instrucciones).
- Usa caché de Maven para acelerar builds.

## E2E — Playwright tests

**Archivo:** `e2e.yml`

- Levanta el stack completo con `docker compose up -d --build`.
- Ejecuta `pnpm run test:e2e` en el frontend.
- En caso de fallo, sube reportes de Playwright y logs de Docker como artefactos.

## SonarCloud — Análisis de calidad

**Archivo:** `sonarqube-analysis.yml`

- Ejecuta `mvn clean verify sonar:sonar` contra SonarCloud.
- Usa caché de SonarCloud para análisis incremental.

---

# Observabilidad

## Stack de observabilidad

| Componente  | URL local              | Propósito                                    |
| ----------- | ---------------------- | -------------------------------------------- |
| Zipkin      | http://localhost:9411  | Trazas distribuidas (request flow completo)  |
| Prometheus  | http://localhost:9090  | Recolección de métricas (Spring Actuator)    |
| Loki        | http://localhost:3100  | Agregación de logs estructurados             |
| Grafana     | http://localhost:4000  | Dashboards unificados (admin / admin)        |

## Correlation ID

Todas las trazas y logs incluyen un `X-Correlation-ID` que se propaga automáticamente a través de todo el stack (gateway → microservicios → Feign → RabbitMQ).

---

# Pruebas

## Backend

### Unitarias
- **Framework:** JUnit 5 + Mockito
- Cada microservicio tiene pruebas unitarias para controladores, servicios, mappers y event listeners.

### Integración
- **Framework:** Testcontainers (MySQL 8.0, Redis 7, RabbitMQ 3.13)
- **Tag:** `@Tag("docker")` — excluidas por defecto en `mvn verify`. Para ejecutarlas se requiere Docker y eliminar temporalmente el grupo excluido.
- Pruebas del Saga orquestado con `@Nested` para agrupar escenarios.

### Cobertura de código (JaCoCo)

| Servicio        | Cobertura de instrucciones | Unit tests |
| --------------- | -------------------------- | ---------- |
| api-gateway     | 97.3%                      | 10         |
| bff             | 99.3%                      | 87         |
| ms-inventario   | 99.8%                      | 190        |
| ms-pedidos      | 97.7%                      | 73         |
| ms-envios       | 100%                       | 36         |

**Mínimo requerido: 90% de instrucciones** (enforced por `jacoco:check` en `mvn verify`).

### Smoke tests
Cada servicio tiene un test de carga de contexto (`MsXxxApplicationTests`) con `@SpringBootTest` + perfil `test`.

### Ejecución

```bash
# Todas las pruebas unitarias de todos los servicios
cd backend
mvn clean verify

# Pruebas de un solo módulo
mvn verify -pl ms-pedidos
mvn verify -pl ms-inventario
mvn verify -pl ms-envios
mvn verify -pl api-gateway
mvn verify -pl bff

# Solo compilar y ejecutar tests (sin verificar cobertura)
mvn test

# Saltar tests (solo compilar)
mvn compile -DskipTests

# Incluyendo pruebas de integración (requiere Docker)
# 1. Eliminar <excludedGroups>docker</excludedGroups> del pom.xml del módulo
# 2. Ejecutar:
mvn verify -pl ms-pedidos
```

## Frontend

### E2E

- **Framework:** Playwright
- **Requisito:** El stack de Docker debe estar corriendo (`docker compose up -d --build`).
- Flujos principales: login, navegación por catálogo, creación de pedido, checkout, gestión de pedidos en dashboard admin.

```bash
cd frontend
pnpm run test:e2e         # Ejecutar todos los tests E2E
pnpm run test:e2e:ui      # Modo interactivo con UI de Playwright
```

---

# Frontend

El frontend está construido con **Next.js 16 App Router**, **TypeScript**, **Zustand** para estado global y **Tailwind CSS v4** para estilos.

## Páginas y funcionalidades

### Landing pública (`/`)
- Hero slider con productos destacados.
- Catálogo de productos con búsqueda y filtros.
- Navegación por categorías.
- Navbar y Footer globales.

### Dashboard administrativo (`/logistica`)
- **Sidebar layout** fijo con navegación entre secciones.
- **Rutas:** pedidos (CRUD + detalle), productos (CRUD), categorías (CRUD), envíos (CRUD + tracking), buscar, perfil, problemas.
- **6 gráficos analíticos** (recharts):
  - Distribución de pedidos por estado (donut) — `DistribucionPedidosChart`
  - Distribución de envíos por estado (donut) — `DistribucionEnviosChart`
  - Stock bajo (barras horizontales, top 10) — `StockBajoChart`
  - Comparación anual (área con gradiente) — `ComparacionAnualChart`
  - Ventas por categoría (área con gradiente) — `VentasPorCategoriaChart`
  - Ventas últimos 30 días (línea) — `VentasLineChart`
- **Tarjetas de métricas** (KPIs).
- **Componentes:** `TablaEnvios`, `FiltrosEnvios`, `ActualizarEstadoForm`.
- Filtros temporales: semana / mes / año.

### Páginas públicas
- **`/login`** — Inicio de sesión con Keycloak (NextAuth.js).
- **`/registro`** — Registro de nuevos usuarios.
- **`/productos/[slug]`** — Detalle de producto individual.
- **Páginas informativas:** centro-de-ayuda, devoluciones, envíos, privacidad, quiénes-somos, términos.

### Panel de cliente (`/dashboard`)
- Carrito de compras (Zustand `carritoStore`).
- Checkout con detección automática de plataforma (mobile/desktop).
- Historial de pedidos con estado en tiempo real.
- Perfil de usuario.

### Estados y colores
- Enums tipados `EstadoPedido` y `EstadoEnvio` con type guards.
- Mapas de colores hex para gráficos y badges de estado.
- Integración con recharts para colores de gráficos consistentes con los badges.

## Convenciones

- **Manejo de estado global:** Zustand
- **Formularios:** react-hook-form + zod
- **API calls:** Axios con clientes tipados en `lib/api.ts`
- **Autenticación:** NextAuth.js con provider Keycloak (OIDC)
- **Componentes UI:** shadcn/ui (basados en Radix)
- **Package manager:** pnpm (no npm)

## Configuración de Next.js

- **`output: 'standalone'`** — Build optimizado para Docker (multi-stage).
- **`images.remotePatterns`** — Permite imágenes desde Cloudinary (`res.cloudinary.com`) y dominios locales.
- **Security headers:** `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy: camera=(), microphone=(), geolocation=()`.

## API Routes de NextAuth

- **`/api/auth/[...nextauth]`** — Manejo de sesiones OIDC: KeycloakProvider + CredentialsProvider, refresh automático de tokens, extracción de roles.
- **`/api/auth/register`** — Registro de nuevos usuarios vía Keycloak Admin API.
- **`/api/auth/update-profile`** — Actualización de perfil de usuario.
- **`/api/auth/logout`** — Cierre de sesión con invalidación de token en Keycloak.

---

# Roadmap

## Etapa 1 — Fundación
* Arquitectura de microservicios.
* Modelado UML y OpenAPI.
* Docker Compose base con infraestructura.
* CI/CD inicial.

## Etapa 2 — Core
* Desarrollo de microservicios (CRUD, Saga, RabbitMQ).
* Frontend con landing, catálogo y autenticación.
* Integración Keycloak + NextAuth.js.
* Dashboard administrativo con gráficos.
* Panel de cliente (carrito, checkout).

## Etapa 3 — Madurez
* Resilience4j (Circuit Breaker, Retry, Rate Limiter).
* Testcontainers para pruebas de integración.
* Stack de observabilidad (Zipkin, Prometheus, Loki, Grafana).
* Playwright E2E tests.
* JaCoCo con 90% de cobertura mínima.
* SonarCloud.

## Etapa 4 — Producción
* Kubernetes (manifiestos de despliegue).
* Optimización de rendimiento.
* Monitoreo y alertas.

---

# Contribución

## Flujo Git

```text
main
 develop
  feature/*
```

## Reglas

* Pull Requests obligatorios con Code Review.
* Convenciones de código consistentes con el proyecto.
* Tests unitarios obligatorios para nuevas funcionalidades.
* Cobertura mínima del 90% mantenida.
* No exponer entidades JPA directamente (usar DTOs).
* Usar `pnpm` en frontend (nunca npm/npx).

---

# Licencia

Proyecto académico y demostrativo.

Todos los derechos reservados © SmartLogix.

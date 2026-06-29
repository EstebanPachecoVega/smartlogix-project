# SmartLogix - Plataforma Inteligente de Gestión Logística

[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-black)](https://nextjs.org/)


---

## 📋 Tabla de Contenidos

<<<<<<< HEAD
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
=======
- [Descripción General](#descripción-general)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Microservicios](#microservicios)
- [Stack Tecnológico](#stack-tecnológico)
- [Patrones y Estándares](#patrones-y-estándares)
- [Transacciones Distribuidas (Saga)](#transacciones-distribuidas-saga)
- [Manejo de Errores](#manejo-de-errores)
- [Infraestructura](#infraestructura)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Configuración del Entorno](#configuración-del-entorno)
- [Ejecución Local](#ejecución-local)
- [CI/CD y Observabilidad](#cicd-y-observabilidad)
- [Pruebas](#pruebas)
- [Frontend](#frontend)
- [Roadmap](#roadmap)
---

## Descripción General

SmartLogix nace de la necesidad de gestionar operaciones logísticas con alta concurrencia y requisitos estrictos de consistencia de inventario. El sistema resuelve el problema de **doble reserva de stock** mediante operaciones atómicas en Redis y un patrón de **Saga orquestada** con RabbitMQ, garantizando que solo una compra pueda reservar el stock disponible.

Además, provee un **panel de gestión logística** completo para que los operadores puedan administrar productos, categorías, pedidos y envíos, actualizar estados y resolver incidencias.
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

---

## Arquitectura del Sistema

<<<<<<< HEAD
SmartLogix optimiza la gestión logística mediante una arquitectura desacoplada y orientada a eventos, con separación clara entre el panel administrativo y la experiencia del cliente final.
=======
La solución se basa en una arquitectura de **microservicios** con bases de datos por servicio (*Database‑per‑Service*), comunicación síncrona vía **API Gateway** (Spring Cloud Gateway) y asíncrona mediante **RabbitMQ**. El **BFF** (Backend for Frontend) actúa como capa de adaptación para el frontend Next.js, implementando **Circuit Breakers** y propagación de **Correlation‑ID**.
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

**Flujo de comunicación resumido:**

<<<<<<< HEAD
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
=======
- El frontend Next.js se comunica con el BFF (Spring Boot WebFlux) a través de HTTP con autenticación JWT.
- El BFF agrega el token y el Correlation‑ID y redirige las peticiones al API Gateway.
- El API Gateway enruta las peticiones a los microservicios correspondientes (`ms-inventario`, `ms-pedidos`, `ms-envios`) y aplica rate limiting, circuit breakers y validación JWT.
- `ms-pedidos` se comunica con `ms-inventario` mediante Feign (síncrono) y con `ms-envios` mediante eventos de RabbitMQ (asíncrono).
- `ms-pedidos` utiliza Redis para la reserva atómica de stock (operaciones con TTL).
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

---

## Microservicios

<<<<<<< HEAD
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
=======
| Microservicio | Puerto | Descripción |
|---------------|--------|-------------|
| **ms-inventario** | 8081 | Gestión de productos, categorías y stock maestro (MySQL). Reserva atómica con Redis. |
| **ms-pedidos** | 8082 | Orquestador de la saga: crea pedidos, reserva stock, confirma y publica eventos. |
| **ms-envios** | 8083 | Consume eventos de pedidos aprobados, genera envíos y publica actualizaciones de estado. |
| **api-gateway** | 8080 | Punto de entrada único: enruta, valida JWT, aplica rate limiting y circuit breakers. |
| **bff** | 8084 | Backend for Frontend. Agrega datos, propaga token y correlation‑id, con circuit breakers. |

---

## Stack Tecnológico

### Backend
- **Java 21** y **Spring Boot 3.5.14**  
- **Spring Cloud Gateway**, **OpenFeign**, **Resilience4j**  
- **Spring Data JPA** (MySQL)  
- **Spring Data Redis** (Lettuce)  
- **Spring AMQP** (RabbitMQ)  
- **MapStruct**, **Lombok**, **Jakarta Validation**  
- **Micrometer Tracing** (Zipkin/Brave)  
- **OpenAPI** (SpringDoc)

### Frontend
- **Next.js 16** (App Router, TypeScript)  
- **Zustand** (estado global)  
- **Tailwind CSS** + **shadcn/ui**  
- **Axios** (cliente HTTP)  
- **NextAuth.js** (preparado para Keycloak)

### Infraestructura
- **Docker** y **Docker Compose** (desarrollo)  
- **Kubernetes** (manifiestos para producción)  
- **GitHub Actions** (CI/CD)  
- **SonarQube**, **ELK/Loki** (logs)

---

## Patrones y Estándares

- **Database‑per‑Service** – cada microservicio tiene su propia base de datos MySQL.  
- **API First** – contratos definidos con OpenAPI 3.0.  
- **DTOs y Mappers** – prohibido exponer entidades JPA; se utiliza MapStruct.  
- **Manejo de errores** – RFC 7807 (ProblemDetail) con `instance` y `type`.  
- **Correlation‑ID** – propagado desde el API Gateway hasta logs y mensajes de RabbitMQ.  
- **Circuit Breaker** – Resilience4j en todas las llamadas a Redis, RabbitMQ y Feign.  
- **Rate Limiting** – basado en IP o JWT, implementado en el Gateway.  
- **Logs estructurados** – en formato JSON con el campo `correlationId`.

---

## Transacciones Distribuidas (Saga)

El flujo de creación de un pedido se ejecuta como una **Saga orquestada** centralizada en `ms-pedidos`:

1. **Cliente** → BFF → Gateway → `ms-pedidos` con `POST /api/pedidos`.  
2. `ms-pedidos` guarda el pedido en estado `PENDIENTE`.  
3. Llama a `ms-inventario` (vía Feign) para **reservar stock atómicamente** en Redis (script Lua o WATCH/MULTI).  
4. Si la reserva es exitosa, confirma el descuento definitivo en MySQL y cambia el estado a `APROBADO`.  
5. Publica `PedidoAprobadoEventDTO` en RabbitMQ.  
6. `ms-envios` consume el evento, crea el envío y publica actualizaciones de estado.  
7. Si falla la reserva o la confirmación, el orquestador ejecuta la **compensación** (cambia el pedido a `RECHAZADO` y publica eventos de liberación de stock).  
8. Las reservas en Redis tienen **TTL de 10 minutos**; al expirar, se publica un evento que cancela el pedido automáticamente.

---

## Manejo de Errores

- **@RestControllerAdvice** global en cada microservicio y en el BFF.  
- **ProblemDetail** (RFC 7807) con campos: `type`, `title`, `status`, `detail`, `instance`.  
- Excepciones personalizadas:  
  - `ResourceNotFoundException` → HTTP 404  
  - `DomainException` → HTTP 422  
  - `DuplicateResourceException` → HTTP 409  
- **FeignErrorDecoder** convierte `ProblemDetail` en excepciones manejables.  
- **Validaciones** (`@Valid`) capturadas con `MethodArgumentNotValidException` retornando los errores campo a campo.  
- El **API Gateway** tiene un `GlobalExceptionHandler` reactivo y un *fallback* para rutas con Circuit Breaker.

---

## Infraestructura

### Desarrollo local (Docker Compose)
El archivo `infrastructure/docker-compose.yml` levanta:
- 3 bases de datos MySQL (inventario, pedidos, envíos)
- RabbitMQ (con management UI en puerto 15672)
- Redis (para stock y rate limiting)
- Los 5 servicios (microservicios, gateway, BFF)  
- Opcionalmente el frontend (si se incluye el servicio `frontend`)

### Producción (Kubernetes)
Se proporcionan manifiestos `yaml` en la carpeta `k8s/` para:
- Deployments, Services, ConfigMaps, Secrets  
- Ingress con TLS  
- PersistentVolumeClaims para las bases de datos  
- Horizontal Pod Autoscaler basado en métricas de CPU/memoria

### Variables de entorno esenciales
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`  
- `REDIS_HOST`, `REDIS_PORT`  
- `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASS`  
- `INVENTARIO_URL` (para ms-pedidos)  
- `GATEWAY_URL` (para el BFF)  
- `JWT_JWK_SET_URI` (para validar tokens, opcional en desarrollo)

---

## Estructura del Proyecto

```
smartlogix-project/
├── backend/
│   ├── ms-inventario/
│   ├── ms-pedidos/
│   ├── ms-envios/
│   ├── api-gateway/
│   └── bff/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── cliente/
│   │   │   └── logistica/
│   │   ├── components/
│   │   ├── lib/
│   │   ├── store/
│   │   └── types/
│   ├── public/
│   ├── Dockerfile
│   ├── next.config.ts
│   └── package.json
├── infrastructure/
│   ├── docker-compose.yml
│   └── .env
├── k8s/
│   ├── deployments/
│   ├── services/
│   └── ingress.yaml
├── scripts/
│   └── concurrencia.js
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa
└── README.md
```

---

## Configuración del Entorno

<<<<<<< HEAD
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
=======
1. **Clonar el repositorio**  
   ```bash
   git clone https://github.com/tu-usuario/smartlogix-project.git
   cd smartlogix-project
   ```

2. **Variables de entorno**  
   - Copia `infrastructure/.env.example` → `infrastructure/.env` y ajusta contraseñas.  
   - Copia `frontend/.env.example` → `frontend/.env.local` y define las URLs del backend.

3. **Construir los artefactos Java** (opcional, Docker lo hace automáticamente)  
   ```bash
   cd backend/ms-inventario && ./mvnw clean package
   # repetir para cada microservicio, gateway y bff
   ```
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

---

## Ejecución Local

<<<<<<< HEAD
## Requisitos previos

- Docker y Docker Compose
- Java 21 (Temurin)
- Node.js 22
- pnpm

## 1. Clonar repositorio
=======
### Backend en Docker + Frontend local (para desarrollo rápido)
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

```bash
cd infrastructure
docker-compose up -d mysql-inventario mysql-pedidos mysql-envios redis rabbitmq
# luego ejecutar cada microservicio desde el IDE o con ./mvnw spring-boot:run
```

<<<<<<< HEAD
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
=======
En otra terminal:
```bash
cd frontend
pnpm install
pnpm dev
```

- Acceso:  
  - Frontend: `http://localhost:3000`  
  - API Gateway: `http://localhost:8080`  
  - Swagger de cada microservicio: `http://localhost:8081/swagger-ui.html` (cambiar puerto)  
  - RabbitMQ UI: `http://localhost:15672` (guest/guest)

---

## CI/CD y Observabilidad

- **GitHub Actions** – pipeline que:  
  - Compila y ejecuta pruebas unitarias e integración.  
  - Analiza calidad con SonarQube.  
  - Construye imágenes Docker y las publica en un registry (Docker Hub / GHCR).  
  - Despliega en Kubernetes (mediante `kubectl` o Helm).

- **Logs centralizados** – con ELK (Elasticsearch, Logstash, Kibana) o Loki. Los logs se emiten en JSON para facilitar su ingestión.

- **Trazabilidad** – Zipkin/Brave integrado; el API Gateway propaga un `X-B3-TraceId` y `X-Correlation-Id`.

- **Métricas** – Actuator expone `/actuator/metrics`, `/actuator/health`, `/actuator/circuitbreakers`.

---

## Pruebas

### Concurrencia (reserva de stock)

Se incluye un script Node.js (`scripts/concurrencia.js`) que lanza dos peticiones simultáneas al BFF. Ejecutar:

```bash
node scripts/concurrencia.js
```

Resultado esperado: una reserva exitosa, la otra falla con `409 Conflict` o `422 Unprocessable Entity`.

### Unitarias e integración

- JUnit 5 + Mockito para cada microservicio.  
- Testcontainers para levantar MySQL, Redis y RabbitMQ en tests de integración.  
- Pruebas de carga con k6 o JMeter (opcional).

---

## Frontend

### Módulo Cliente
- Catálogo de productos (desde `ms-inventario`).  
- Carrito de compras (Zustand + persistencia en localStorage).  
- Checkout con validación y creación de pedido (llama al BFF).  
- Listado de pedidos con detalle y seguimiento de envío.

### Módulo Logística (Gestión)
- Dashboard con métricas (productos, ventas, pedidos, envíos).  
- CRUD de productos y categorías (con diálogos modales).  
- Gestión de pedidos (listado, detalle, actualización de estado).  
- Gestión de envíos (listado, filtros, actualización de estado, búsqueda por tracking, envíos con problemas).

Los componentes de estado (`EstadoPedidoBadge`, `EstadoEnvioBadge`) muestran textos amigables en español y colores semánticos.

---

## Roadmap

- [x] Arquitectura base y definición de contratos  
- [x] Implementación de microservicios con saga y reserva atómica  
- [x] API Gateway, BFF y frontend completo  
- [x] Prueba de concurrencia y logs estructurados  
- [ ] **Integración con Keycloak** (autenticación JWT real y roles) – *pendiente*  
- [ ] WebSockets para notificaciones en tiempo real (estado de envíos)  
- [ ] Gráficos en el dashboard (Chart.js o Recharts)  
- [ ] Tests end‑to‑end con Playwright o Cypress  
- [ ] Despliegue automatizado en Kubernetes con Helm  

---

*Documentación generada en Mayo 2026*
>>>>>>> 235c5d908d02d9ebc8ac8fccf3e4aa7050f658fa

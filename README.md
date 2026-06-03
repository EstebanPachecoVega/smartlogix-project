# SmartLogix - Plataforma Inteligente de Gestión Logística

[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-black)](https://nextjs.org/)

SmartLogix es una plataforma escalable y resiliente de gestión logística basada en microservicios. Permite a los clientes realizar compras con control de stock en tiempo real, y a los operadores logísticos gestionar productos, categorías, pedidos y envíos con total trazabilidad y tolerancia a fallos.

---

## 📋 Tabla de Contenidos

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

---

## Arquitectura del Sistema

La solución se basa en una arquitectura de **microservicios** con bases de datos por servicio (*Database‑per‑Service*), comunicación síncrona vía **API Gateway** (Spring Cloud Gateway) y asíncrona mediante **RabbitMQ**. El **BFF** (Backend for Frontend) actúa como capa de adaptación para el frontend Next.js, implementando **Circuit Breakers** y propagación de **Correlation‑ID**.

**Flujo de comunicación resumido:**

- El frontend Next.js se comunica con el BFF (Spring Boot WebFlux) a través de HTTP con autenticación JWT.
- El BFF agrega el token y el Correlation‑ID y redirige las peticiones al API Gateway.
- El API Gateway enruta las peticiones a los microservicios correspondientes (`ms-inventario`, `ms-pedidos`, `ms-envios`) y aplica rate limiting, circuit breakers y validación JWT.
- `ms-pedidos` se comunica con `ms-inventario` mediante Feign (síncrono) y con `ms-envios` mediante eventos de RabbitMQ (asíncrono).
- `ms-pedidos` utiliza Redis para la reserva atómica de stock (operaciones con TTL).

---

## Microservicios

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
└── README.md
```

---

## Configuración del Entorno

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

---

## Ejecución Local

### Backend en Docker + Frontend local (para desarrollo rápido)

```bash
cd infrastructure
docker-compose up -d mysql-inventario mysql-pedidos mysql-envios redis rabbitmq
# luego ejecutar cada microservicio desde el IDE o con ./mvnw spring-boot:run
```

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

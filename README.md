# SmartLogix

## Plataforma Inteligente de Gestión Logística basada en Microservicios

SmartLogix es una plataforma moderna de gestión logística diseñada bajo una arquitectura de microservicios, enfocada en la resiliencia, escalabilidad y mantenibilidad. El sistema implementa patrones de comunicación síncrona y asíncrona, manejo centralizado de errores y transacciones distribuidas mediante el patrón Saga Orquestado.

---

# Tabla de Contenidos

* [Descripción General](#descripción-general)
* [Arquitectura del Sistema](#arquitectura-del-sistema)
* [Microservicios](#microservicios)
* [Stack Tecnológico](#stack-tecnológico)
* [Patrones y Estándares](#patrones-y-estándares)
* [Transacciones Distribuidas](#transacciones-distribuidas)
* [Manejo de Errores](#manejo-de-errores)
* [Infraestructura](#infraestructura)
* [Estructura del Proyecto](#estructura-del-proyecto)
* [Configuración del Entorno](#configuración-del-entorno)
* [Ejecución Local](#ejecución-local)
* [CI/CD y Observabilidad](#cicd-y-observabilidad)
* [Pruebas](#pruebas)
* [Frontend](#frontend)
* [Roadmap](#roadmap)
* [Contribución](#contribución)
* [Licencia](#licencia)

---

# Descripción General

SmartLogix busca optimizar la gestión logística mediante una arquitectura desacoplada y orientada a eventos.

El sistema permite:

* Gestión de pedidos.
* Administración de inventario.
* Coordinación logística de envíos.
* Comunicación resiliente entre servicios.
* Escalabilidad horizontal.
* Observabilidad centralizada.
* Integración continua y despliegue automatizado.

La solución utiliza el enfoque **Database-per-Service**, donde cada microservicio administra su propia base de datos.

---

# Arquitectura del Sistema

La plataforma está compuesta por:

```text
Cliente
   │
   ▼
API Gateway
   │
   ▼
BFF (Backend for Frontend)
   │
   ├──────────────┬──────────────┬──────────────┐
   ▼              ▼              ▼              ▼
MS Pedidos    MS Inventario   MS Envíos    RabbitMQ
```

## Componentes principales

| Componente    | Responsabilidad                                                         |
| ------------- | ----------------------------------------------------------------------- |
| API Gateway   | Punto de entrada único, validación JWT, Rate Limiting y Circuit Breaker |
| BFF           | Orquestación y agregación de datos para el frontend                     |
| MS Inventario | Gestión de productos y stock                                            |
| MS Pedidos    | Gestión del ciclo de vida del pedido y Saga Orquestado                  |
| MS Envíos     | Procesamiento logístico y seguimiento de despachos                      |
| RabbitMQ      | Comunicación asíncrona basada en eventos                                |
| Redis         | Rate limiting y caching                                                 |
| MySQL         | Persistencia de datos                                                   |

---

# Microservicios

## MS Pedidos

Responsable de:

* Crear pedidos.
* Gestionar estados.
* Ejecutar la lógica Saga.
* Emitir eventos.

### Estados posibles

* PENDIENTE
* APROBADO
* RECHAZADO
* EN_PROCESO
* DESPACHADO
* ENTREGADO

---

## MS Inventario

Responsable de:

* Gestionar catálogo.
* Reservar stock.
* Validar disponibilidad.

### Excepción principal

```java
StockInsuficienteException
```

---

## MS Envíos

Responsable de:

* Crear órdenes logísticas.
* Gestionar estados de despacho.
* Consumir eventos desde RabbitMQ.

---

# Stack Tecnológico

## Backend

* Java 25
* Spring Boot 4.x
* Spring Cloud Gateway
* Spring Cloud OpenFeign
* Spring Data JPA
* MapStruct
* RabbitMQ
* MySQL
* Redis
* Resilience4j

## Frontend

* Next.js
* TypeScript
* Zustand
* Tailwind CSS
* NextAuth.js

## DevOps e Infraestructura

* Docker
* Docker Compose
* Kubernetes
* GitHub Actions
* SonarQube
* ELK / Loki

---

# Patrones y Estándares

## DTO Pattern

Está prohibido exponer entidades JPA directamente.

Se implementan:

```text
.dto.request
.dto.response
```

### Validaciones

Todos los Request DTOs utilizan:

```java
@NotNull
@Size
@Min
@Valid
```

---

## MapStruct

Conversión automática entre:

* Entidades
* DTOs
* Event DTOs

Ejemplo:

```java
@Mapper(componentModel = "spring")
public interface PedidoMapper {
    PedidoResponseDTO toDTO(Pedido pedido);
}
```

---

## Comunicación basada en eventos

Se utilizan Event DTOs ligeros:

```java
PedidoAprobadoEventDTO
```

Incluyen únicamente:

* IDs
* timestamps
* información mínima requerida

---

# Transacciones Distribuidas

## Saga Orquestado

El microservicio de pedidos actúa como orquestador.

## Flujo principal

### 1. Creación del pedido

El cliente envía:

```text
CrearPedidoRequestDTO
```

### 2. Reserva de stock

MS Pedidos solicita reserva a Inventario.

### 3. Éxito

* Pedido cambia a APROBADO.
* Se emite evento RabbitMQ.
* MS Envíos crea despacho.

### 4. Fallo

Si Inventario falla:

* Pedido cambia a RECHAZADO.
* Se ejecuta compensación.
* Se notifica al cliente.

---

# Manejo de Errores

El sistema implementa el estándar:

```text
RFC 7807 - Problem Details for HTTP APIs
```

## Manejo global

Cada microservicio implementa:

```java
@RestControllerAdvice
```

## Estructura de respuesta

```json
{
  "type": "/errors/recurso-no-encontrado",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "El pedido solicitado no existe",
  "instance": "/api/pedidos/10"
}
```

## Tipos de excepciones

### Negocio

* StockInsuficienteException
* EstadoPedidoInvalidoException

### Recursos

* ProductoNoEncontradoException
* PedidoNoEncontradoException

### Seguridad

* HTTP 401
* HTTP 403

### Validación

* MethodArgumentNotValidException

---

# Infraestructura

## Docker Compose

Servicios principales:

* MySQL
* RabbitMQ
* Redis
* API Gateway
* Microservicios

---

# Estructura del Proyecto

```text
smartlogix/
│
├── api-gateway/
├── bff/
├── ms-pedidos/
├── ms-inventario/
├── ms-envios/
├── frontend/
├── docker/
├── kubernetes/
├── docs/
└── README.md
```

---

# Configuración del Entorno

## Variables de entorno

### Base de datos

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=smartlogix
MYSQL_USER=root
MYSQL_PASSWORD=root
```

### RabbitMQ

```env
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
```

### Redis

```env
REDIS_HOST=localhost
REDIS_PORT=6379
```

### JWT

```env
JWT_SECRET=secret-key
JWT_EXPIRATION=3600
```

---

# Ejecución Local

## Clonar repositorio

```bash
git clone https://github.com/tu-organizacion/smartlogix.git
cd smartlogix
```

## Levantar infraestructura

```bash
docker-compose up -d
```

## Ejecutar microservicios

```bash
./mvnw spring-boot:run
```

## Ejecutar frontend

```bash
npm install
npm run dev
```

---

# CI/CD y Observabilidad

## GitHub Actions

Pipeline automatizado para:

* Compilación.
* Ejecución de pruebas.
* SonarQube.
* Construcción Docker.
* Despliegue Kubernetes.

---

## Observabilidad

### Correlation ID

Todos los requests incluyen:

```text
X-Correlation-ID
```

### Logging estructurado

Formato JSON compatible con:

* ELK Stack
* Loki

---

# Pruebas

## Unitarias

* JUnit 5
* Mockito

## Integración

* Testcontainers

## End-to-End

Validación completa del flujo Saga.

### Cobertura mínima

```text
60%
```

---

# Frontend

Frontend desarrollado con:

* Next.js App Router
* TypeScript
* Zustand
* Tailwind CSS

## Funcionalidades

* Gestión de pedidos.
* Visualización de estados.
* Manejo amigable de errores.
* Consumo centralizado del BFF.

---

# Roadmap

## Etapa 1

* Arquitectura.
* UML.
* OpenAPI.
* Docker base.

## Etapa 2

* Desarrollo core.
* Integraciones.
* RabbitMQ.
* Frontend.

## Etapa 3

* Resilience4j.
* Testcontainers.
* Observabilidad.
* Kubernetes.
* CI/CD.

---

# Contribución

## Flujo Git

```text
main
 develop
  feature/*
```

## Reglas

* Pull Requests obligatorios.
* Code Review.
* Convenciones de código.
* Tests obligatorios.

---

# Licencia

Proyecto académico y demostrativo.

Todos los derechos reservados © SmartLogix.

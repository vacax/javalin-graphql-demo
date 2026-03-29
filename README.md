# Javalin + GraphQL Demo

Proyecto de demostración del uso de **GraphQL** con el framework **Javalin 7** en Java 25,
desarrollado para estudiantes de la PUCMM / EICT.

Demuestra las tres operaciones fundamentales de GraphQL:
- **Queries** — consulta de datos (sin efectos secundarios)
- **Mutations** — creación, actualización y eliminación de datos
- **Subscriptions** — eventos en tiempo real por WebSocket

## Tecnologías

| Componente | Librería / Versión |
|---|---|
| Framework web | Javalin 7.1.0 |
| Motor GraphQL | graphql-java 21.3 (vía graphql-spqr) |
| Generador de esquema | graphql-spqr 0.12.4 |
| Subscriptions | Project Reactor (Flux) |
| Serialización | Jackson 2.17 |
| Lenguaje | Java 25 |

> **Nota:** El plugin oficial `io.javalin:javalin-graphql` solo existe hasta Javalin 4.x.
> Este proyecto integra directamente **graphql-spqr**, que es la librería subyacente del plugin,
> con las mismas anotaciones (`@GraphQLQuery`, `@GraphQLMutation`, `@GraphQLSubscription`).

---

## Requisitos previos

- Java 25+
- Docker y Docker Compose (para ejecución en contenedor)

---

## Ejecución

### Con Gradle (local)

```bash
./gradlew run
```

### Con Docker Compose

```bash
docker compose up --build
```

### Con Docker directamente

```bash
docker build -t javalin-graphql-demo .
docker run -p 7000:7000 javalin-graphql-demo
```

El servidor queda escuchando en:

| Protocolo | URL | Uso |
|---|---|---|
| GET | `http://localhost:7000/graphiql` | **GraphiQL Playground** (navegador) |
| HTTP | `http://localhost:7000/graphql` | Queries y Mutations |
| WebSocket | `ws://localhost:7000/graphql` | Subscriptions (protocolo `graphql-ws`) |

---

## GraphiQL Playground

Abrir en el navegador: **[http://localhost:7000/graphiql](http://localhost:7000/graphiql)**

GraphiQL 3 está integrado directamente (sin dependencias adicionales, carga desde CDN).
Soporta queries, mutations y subscriptions en tiempo real usando el mismo endpoint WebSocket.

---

## Ejemplos con `curl`

Todas las queries y mutations se envían como `POST` con un cuerpo JSON:

```json
{
  "query": "...",
  "variables": { ... },
  "operationName": "..."
}
```

---

### Queries

#### Listar todos los estudiantes

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerEstudiantes { id nombre carrera } }"
  }'
```

#### Buscar un estudiante por ID

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerEstudiante(id: 1) { id nombre carrera } }"
  }'
```

#### Listar todos los profesores

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerProfesores { id nombre carrera } }"
  }'
```

#### Buscar un profesor por ID

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerProfesor(id: 1) { id nombre carrera } }"
  }'
```

#### Listar todos los grupos con profesor y estudiantes

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerGrupos { numeroClase nombre profesor { id nombre } estudiantes { id nombre carrera } } }"
  }'
```

#### Buscar un grupo por número de clase

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ obtenerGrupo(numeroClase: 1) { numeroClase nombre profesor { nombre } estudiantes { nombre } } }"
  }'
```

#### Listar estudiantes de un grupo específico

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ estudiantesDeGrupo(numeroClase: 1) { id nombre carrera } }"
  }'
```

#### Query con variables (forma recomendada)

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query BuscarEstudiante($id: Int!) { obtenerEstudiante(id: $id) { id nombre carrera } }",
    "variables": { "id": 2 },
    "operationName": "BuscarEstudiante"
  }'
```

---

### Mutations

#### Agregar un estudiante

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { agregarEstudiante(nombre: \"Ana Torres\", carrera: \"Ing. Software\") { id nombre carrera } }"
  }'
```

#### Actualizar un estudiante

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { actualizarEstudiante(id: 1, nombre: \"Juan Carlos Perez\", carrera: null) { id nombre carrera } }"
  }'
```

#### Eliminar un estudiante

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { eliminarEstudiante(id: 1) }"
  }'
```

#### Agregar un profesor

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { agregarProfesor(nombre: \"Dr. Sanchez\", carrera: \"Redes y Comunicaciones\") { id nombre carrera } }"
  }'
```

#### Actualizar un profesor

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { actualizarProfesor(id: 1, nombre: \"Dr. Garcia Reyes\", carrera: null) { id nombre carrera } }"
  }'
```

#### Crear un grupo de clase

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { agregarGrupo(nombre: \"Algoritmos\", idProfesor: 2) { numeroClase nombre profesor { nombre } } }"
  }'
```

#### Inscribir un estudiante en un grupo

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { inscribirEstudiante(numeroClase: 1, idEstudiante: 3) { numeroClase nombre estudiantes { id nombre } } }"
  }'
```

#### Mutation con variables

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation AgregarEstudiante($nombre: String!, $carrera: String!) { agregarEstudiante(nombre: $nombre, carrera: $carrera) { id nombre carrera } }",
    "variables": { "nombre": "Pedro Ramirez", "carrera": "Ing. de Sistemas" },
    "operationName": "AgregarEstudiante"
  }'
```

---

### Subscriptions

Las subscriptions no son compatibles con `curl` ya que requieren una conexión WebSocket persistente.
Se recomienda una de estas herramientas:

#### Opción A — Altair GraphQL Client (recomendado)

1. Descargar [Altair GraphQL Client](https://altairgraphql.dev)
2. Configurar la URL: `http://localhost:7000/graphql`
3. En la sección de subscriptions activar **WebSocket**
4. Ejecutar:

```graphql
subscription {
  nuevosEstudiantes {
    id
    nombre
    carrera
  }
}
```

5. En otra pestaña ejecutar la mutación `agregarEstudiante` y observar el evento en tiempo real.

#### Opción B — `websocat` (línea de comandos)

```bash
# Instalación en Linux
curl -L https://github.com/vi/websocat/releases/latest/download/websocat.x86_64-unknown-linux-musl \
  -o /usr/local/bin/websocat && chmod +x /usr/local/bin/websocat

# Conectar e iniciar el handshake graphql-ws
websocat ws://localhost:7000/graphql
```

Una vez conectado, enviar los mensajes en orden:

```json
{"type":"connection_init"}
```
```json
{"type":"subscribe","id":"1","payload":{"query":"subscription { nuevosEstudiantes { id nombre carrera } }"}}
```

En otra terminal, ejecutar la mutación para ver el evento:

```bash
curl -s -X POST http://localhost:7000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { agregarEstudiante(nombre: \"Luis Mora\", carrera: \"Ing. Software\") { id } }"}'
```

#### Opción C — `wscat` (Node.js)

```bash
# Instalación
npm install -g wscat

# Conectar
wscat -c ws://localhost:7000/graphql
```

#### Suscripción de contador periódico

Esta suscripción emite el total de estudiantes cada N segundos (sin necesidad de mutations):

```graphql
subscription {
  contadorEstudiantes(intervalSegundos: 3)
}
```

---

## Protocolo graphql-ws

Las subscriptions siguen el protocolo estándar [graphql-ws](https://github.com/enisdenjo/graphql-ws).
El flujo de mensajes es:

```
Cliente → {"type": "connection_init"}
Servidor ← {"type": "connection_ack"}

Cliente → {"type": "subscribe", "id": "1",
            "payload": {"query": "subscription { nuevosEstudiantes { id nombre } }"}}
Servidor ← {"type": "next", "id": "1",
             "payload": {"data": {"nuevosEstudiantes": {"id": 4, "nombre": "..."}}} }

Cliente → {"type": "complete", "id": "1"}   ← cancelar suscripción
```

---

## Estructura del proyecto

```
src/main/java/edu/pucmm/eict/jqd/
├── Main.java                     Configuración de Javalin (endpoints HTTP y WS)
├── datos/
│   └── AlmacenDatos.java         Repositorio en memoria con datos de ejemplo
├── entidades/
│   ├── Estudiante.java
│   ├── Profesor.java
│   └── GrupoClase.java
└── graphql/
    ├── ConsultaGraphql.java      Resolvers con @GraphQLQuery
    ├── MutacionGraphql.java      Resolvers con @GraphQLMutation
    ├── SuscripcionGraphql.java   Resolvers con @GraphQLSubscription (Flux)
    └── GraphQLServicio.java      Genera el esquema y ejecuta operaciones
```

## Datos precargados

Al arrancar el servidor se cargan los siguientes datos de ejemplo:

**Profesores**
- Prof. Carlos Camacho — Ingeniería Telemática
- Prof. José Alonso — Ing. en Ciencias de la Computación

**Estudiantes**
- Juan Pérez (ID 1) — Ing. en Ciencias de la Computación
- María López (ID 2) — Ing. en Ciencias de la Computación
- Carlos Rodríguez (ID 3) — Ingeniería Telemática

**Grupos**
- Grupo 1: Programación Web — Prof. Carlos Camacho — {Juan Pérez, María López}
- Grupo 2: Bases de Datos — Prof. José Alonso — {María López, Carlos Rodríguez}

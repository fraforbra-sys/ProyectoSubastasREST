# Prompt para Claude Code: Migración de ProyectoSubastasRMI a Spring Boot RESTful

## Contexto del proyecto

Tengo un sistema de subastas implementado con Java RMI que quiero migrar completamente a un servicio web RESTful usando Spring Boot con Maven, siguiendo el patrón de las prácticas 7, 8 y 9 de la asignatura.

El proyecto RMI original está en el directorio actual (`ProyectoSubastasRMI/`) con esta estructura:

```
ProyectoSubastasRMI/
├── comun/
│   ├── Articulo.java           # Modelo serializable
│   ├── Puja.java               # Modelo serializable
│   ├── Usuario.java            # Modelo serializable
│   ├── HistorialSubasta.java   # Modelo serializable
│   ├── SubastaCompletada.java  # Modelo serializable
│   ├── IGestorSubastas.java    # Interfaz remota RMI (Factory + gestión)
│   ├── ISubasta.java           # Interfaz remota RMI (Stateful)
│   ├── IObservadorCliente.java # Interfaz remota RMI (Callback/Observer)
│   └── BuscadorSubastas.java   # Interfaz remota RMI (Stateless)
├── servidor/
│   ├── GestorSubastasImpl.java        # Implementación del gestor principal
│   ├── SubastaImpl.java               # Implementación de cada subasta (con timer)
│   ├── BuscadorSubastasImpl.java      # Implementación del buscador
│   ├── ServidorSubastas.java          # Main del servidor RMI
│   ├── dao/
│   │   ├── DatabaseManager.java       # Singleton de conexión SQLite
│   │   ├── UsuarioDAO.java            # CRUD de usuarios
│   │   └── SubastaCompletadaDAO.java  # CRUD de subastas completadas
│   └── servicio/
│       └── ServicioUsuarios.java      # Lógica de negocio de usuarios (BCrypt)
├── cliente/
│   ├── ClienteSubastas.java           # Cliente de línea de comandos RMI
│   └── ObservadorClienteImpl.java     # Implementación del callback observer
└── db/
    ├── init.sql       # Script DDL: tablas usuarios y subastas_completadas (SQLite)
    └── subastas.db    # Base de datos SQLite existente
```

---

## Lógica de negocio a preservar

### Modelos de datos (del paquete `comun/`)
- **Articulo**: id, nombre, descripcion, urlImagen, precioSalida
- **Puja**: usuario, cantidad, timestamp
- **HistorialSubasta**: articulo, lista de pujas, precioActual, liderActual, activa
- **SubastaCompletada**: idSubasta, nombreArticulo, precioFinal, foto, comprador, fechaFinalizacion
- **Usuario**: id, username, passwordHash (BCrypt)

### Base de datos SQLite
Mantener la misma BD SQLite con las tablas:
- `usuarios` (id, username, password_hash, created_at)
- `subastas_completadas` (id_subasta, nombre_articulo, precio_final, foto, comprador, fecha_finalizacion)

### Lógica de subastas en memoria
Las subastas **activas** se guardan en memoria (ConcurrentHashMap), igual que en RMI. Solo las **completadas** se persisten en SQLite. Cada subasta tiene un timer que la finaliza automáticamente al expirar el tiempo.

### Autenticación
Login con usuario + contraseña (BCrypt). Por ahora sin JWT: la autenticación se pasa como parámetro en las peticiones (query param o header básico). Usuarios de prueba: admin/admin123, usuario1/pass1, usuario2/pass2, cliente/cliente123.

---

## Lo que quiero construir: API REST

Crea un nuevo proyecto Maven Spring Boot en el directorio `ProyectoSubastasREST/` con la siguiente estructura:

```
ProyectoSubastasREST/
├── pom.xml
└── src/main/java/com/subastas/
    ├── SubastasApplication.java
    ├── modelo/
    │   ├── Articulo.java
    │   ├── Puja.java
    │   ├── HistorialSubasta.java
    │   ├── SubastaActiva.java         # DTO para exponer el estado de una subasta activa
    │   └── SubastaCompletada.java
    ├── controlador/
    │   ├── UsuarioController.java     # Endpoints de autenticación y registro
    │   ├── SubastaController.java     # Endpoints CRUD de subastas activas
    │   └── HistorialController.java   # Endpoints de subastas completadas
    ├── servicio/
    │   ├── ServicioUsuarios.java      # Lógica de usuarios (migrada del RMI)
    │   ├── ServicioSubastas.java      # Lógica de subastas activas en memoria
    │   └── ServicioHistorial.java     # Lógica de subastas completadas (SQLite)
    └── dao/
        ├── DatabaseManager.java       # Singleton SQLite (migrado del RMI)
        ├── UsuarioDAO.java            # CRUD usuarios (migrado del RMI)
        └── SubastaCompletadaDAO.java  # CRUD historial (migrado del RMI)
```

---

## Endpoints REST requeridos

### Usuarios (`/usuario`)
| Método | URI | Body / Params | Descripción |
|--------|-----|---------------|-------------|
| POST | `/usuario/login` | JSON: `{username, password}` | Autenticar usuario. Devuelve 200 OK o 401 Unauthorized |
| POST | `/usuario/registro` | JSON: `{username, password}` | Registrar nuevo usuario. Devuelve 201 Created o 409 Conflict si ya existe |
| GET  | `/usuario/{username}` | — | Comprobar si un usuario existe. Devuelve 200 o 404 |

### Subastas activas (`/subasta`)
| Método | URI | Body / Params | Descripción |
|--------|-----|---------------|-------------|
| GET | `/subasta` | query: `?filtro=texto` (opcional) | Listar todas las subastas activas, o filtrar por nombre del artículo |
| GET | `/subasta/{id}` | — | Obtener detalles de una subasta activa (precio actual, líder, tiempo restante, historial de pujas) |
| POST | `/subasta` | JSON con Articulo + duracionSegundos | Crear nueva subasta. Devuelve 201 Created con el id de la subasta |
| POST | `/subasta/{id}/pujar` | JSON: `{usuario, cantidad}` | Realizar una puja. Devuelve 200 OK si aceptada, 400 si rechazada (precio insuficiente o subasta finalizada) |
| DELETE | `/subasta/{id}` | — | Finalizar una subasta anticipadamente. Devuelve 200 OK o 404 Not Found |
| GET | `/subasta/buscar` | query: `?precioMin=X&precioMax=Y` | Buscar subastas por rango de precio |
| GET | `/subasta/porTiempo` | query: `?segundos=N` | Subastas que finalizan en menos de N segundos |

### Historial (`/historial`)
| Método | URI | Body / Params | Descripción |
|--------|-----|---------------|-------------|
| GET | `/historial` | — | Obtener todas las subastas completadas |
| GET | `/historial/{id}` | — | Obtener subasta completada por su ID. Devuelve 200 o 404 |
| GET | `/historial/comprador/{username}` | — | Subastas ganadas por un comprador |
| GET | `/historial/count` | — | Número total de subastas completadas |

---

## Requisitos técnicos

### pom.xml
Usar Spring Boot 3.x con estas dependencias:
- `spring-boot-starter-web` (REST)
- `sqlite-jdbc` versión 3.45.x para la BD SQLite
- `spring-security-crypto` o `spring-boot-starter-security` solo para BCrypt (no para securizar endpoints)
- `springdoc-openapi-starter-webmvc-ui` versión 2.x para Swagger UI
- Java 17 o superior

### Anotaciones Spring a usar (según las prácticas)
- `@RestController` en los controladores
- `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@PutMapping` para mapear métodos HTTP
- `@PathVariable` para variables en la URI: `/subasta/{id}`
- `@RequestParam` para parámetros opcionales en query string: `?filtro=texto`
- `@RequestBody` para recibir JSON en el cuerpo de la petición (POST/PUT)
- `ResponseEntity<T>` como tipo de retorno con `HttpStatus.OK`, `HttpStatus.NOT_FOUND`, `HttpStatus.CREATED`, `HttpStatus.BAD_REQUEST`, `HttpStatus.CONFLICT`, etc.
- `@JsonProperty` en los setters de las clases modelo para deserialización JSON

### Gestión del estado de subastas
- Las subastas activas se mantienen en un `ConcurrentHashMap<String, SubastaActiva>` en `ServicioSubastas` (bean `@Service` Spring, equivalente al `GestorSubastasImpl` RMI)
- Cada `SubastaActiva` tiene su propio `ScheduledExecutorService` para auto-finalizar al expirar el tiempo (igual que `SubastaImpl` RMI)
- Al finalizar (por tiempo o por DELETE), la subasta se persiste en SQLite a través de `SubastaCompletadaDAO`

### application.properties
```properties
server.port=9000
spring.application.name=ProyectoSubastasREST
```

### Nota importante sobre el Observer/Callback
El patrón Observer con callbacks de RMI (`IObservadorCliente`) **no se puede migrar directamente** a REST puro (REST es sin estado y sin callbacks). Elimina esa funcionalidad del servidor. Si se quiere notificación en tiempo real en el futuro, sería con WebSockets, pero **no es parte de este proyecto**.

---

## Pasos a seguir

1. Crea la estructura de directorios del proyecto Maven.
2. Genera el `pom.xml` con las dependencias indicadas.
3. Migra las clases DAO (`DatabaseManager`, `UsuarioDAO`, `SubastaCompletadaDAO`) del paquete `servidor/dao/` adaptándolas al paquete `com.subastas.dao` y añadiendo `@JsonProperty` donde sea necesario.
4. Migra `ServicioUsuarios` al paquete `com.subastas.servicio`, anotándolo con `@Service`.
5. Crea `ServicioSubastas` como `@Service` con el `ConcurrentHashMap` de subastas activas y la lógica de creación, búsqueda, puja y finalización.
6. Crea `ServicioHistorial` como `@Service` que delega en `SubastaCompletadaDAO`.
7. Crea los tres controladores REST con todos los endpoints de la tabla de arriba.
8. Crea `SubastasApplication.java` con el `main` y `@SpringBootApplication`.
9. Crea `application.properties` con `server.port=9000`.
10. Copia la BD SQLite existente (`db/subastas.db`) o asegúrate de que `DatabaseManager` la inicializa correctamente en `db/subastas.db` relativa al directorio de ejecución.
11. Compila con `mvn clean compile` y verifica que no hay errores.
12. Ejecuta con `mvn spring-boot:run` y prueba en `http://localhost:9000/swagger-ui/index.html`.

---

## Pruebas a verificar

Una vez ejecutando, confirma que funcionan estos endpoints con curl o Swagger UI:

```bash
# Registro de usuario
curl -X POST http://localhost:9000/usuario/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test1234"}'

# Login
curl -X POST http://localhost:9000/usuario/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Crear subasta
curl -X POST http://localhost:9000/subasta \
  -H "Content-Type: application/json" \
  -d '{"articulo":{"id":"art1","nombre":"Reloj antiguo","descripcion":"Reloj de pared siglo XIX","urlImagen":"http://img.com/reloj.jpg","precioSalida":100.0},"duracionSegundos":120}'

# Listar subastas activas
curl http://localhost:9000/subasta

# Pujar
curl -X POST http://localhost:9000/subasta/{id}/pujar \
  -H "Content-Type: application/json" \
  -d '{"usuario":"usuario1","cantidad":150.0}'

# Ver historial
curl http://localhost:9000/historial
```

También acceder a `http://localhost:9000/swagger-ui/index.html` para ver la documentación OpenAPI generada automáticamente.

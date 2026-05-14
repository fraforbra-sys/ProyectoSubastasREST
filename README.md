# Sistema de Subastas Distribuido - Spring Boot REST

Este proyecto es una migración del sistema de subastas online distribuido originalmente implementado con **Java RMI**, ahora reescrito como un **servicio web RESTful** usando **Spring Boot**. Ha sido desarrollado como práctica de la asignatura "Sistemas Distribuidos y Servicios Web" de la Escuela Superior de Ingeniería de la Universidad de Sevilla.

---

## Descripción

El sistema permite a los usuarios registrarse, autenticarse, crear subastas, realizar pujas y consultar el historial de subastas completadas, todo ello a través de una API REST con representación JSON.

---

## Arquitectura

- **Controladores REST** (`@RestController`): gestionan las peticiones HTTP y devuelven `ResponseEntity<T>` con los códigos de estado adecuados.
- **Servicios** (`@Service`): contienen la lógica de negocio. Las subastas activas se mantienen en memoria con un `ConcurrentHashMap`.
- **DAOs**: acceso a la base de datos SQLite mediante JDBC para usuarios y subastas completadas.
- **Base de datos**: SQLite (`db/subastas.db`), creada automáticamente al arrancar.

---

## Estructura del Proyecto

```text
src/main/java/com/subastas/
├── SubastasApplication.java
├── controlador/
│   ├── UsuarioController.java
│   ├── SubastaController.java
│   └── HistorialController.java
├── servicio/
│   ├── ServicioUsuarios.java
│   ├── ServicioSubastas.java
│   └── ServicioHistorial.java
├── dao/
│   ├── DatabaseManager.java
│   ├── UsuarioDAO.java
│   └── SubastaCompletadaDAO.java
└── modelo/
    ├── Articulo.java
    ├── Puja.java
    ├── SubastaActiva.java
    ├── SubastaCompletada.java
    ├── HistorialSubasta.java
    ├── CrearSubastaRequest.java
    ├── PujaRequest.java
    └── Usuario.java
```

---

## Endpoints principales

### Usuarios
| Método | URI | Descripción |
|--------|-----|-------------|
| POST | `/usuario/login` | Autenticar usuario |
| POST | `/usuario/registro` | Registrar nuevo usuario |
| GET  | `/usuario/{username}` | Comprobar si existe un usuario |

### Subastas activas
| Método | URI | Descripción |
|--------|-----|-------------|
| GET | `/subasta` | Listar todas las subastas activas |
| GET | `/subasta/{id}` | Obtener detalles de una subasta |
| POST | `/subasta` | Crear nueva subasta |
| POST | `/subasta/{id}/pujar` | Realizar una puja |
| DELETE | `/subasta/{id}` | Finalizar una subasta |
| GET | `/subasta/buscar` | Buscar por rango de precio |
| GET | `/subasta/porTiempo` | Buscar por tiempo restante |

### Historial
| Método | URI | Descripción |
|--------|-----|-------------|
| GET | `/historial` | Todas las subastas completadas |
| GET | `/historial/{id}` | Subasta completada por ID |
| GET | `/historial/comprador/{username}` | Subastas ganadas por un usuario |
| GET | `/historial/count` | Total de subastas completadas |

---

## Ejecución

```powershell
.\mvnw.cmd spring-boot:run
```

El servidor arranca en el puerto **9000**. La base de datos SQLite se inicializa automáticamente.

Documentación interactiva disponible en:
```
http://localhost:9000/swagger-ui/index.html
```

---

## Tecnologías

- Java 17
- Spring Boot 3
- Maven (Maven Wrapper incluido)
- SQLite (sqlite-jdbc)
- Spring Security Crypto (BCrypt)
- SpringDoc OpenAPI (Swagger UI)

---

## Diferencias respecto a la versión RMI

| RMI | REST |
|-----|------|
| Interfaces remotas (`Remote`) | Controladores `@RestController` |
| `UnicastRemoteObject` | Servicios `@Service` con Spring |
| Callbacks Observer (`IObservadorCliente`) | Eliminado (REST es stateless) |
| `rmiregistry` | Servidor HTTP embebido (Tomcat) |
| Objetos `Serializable` por red | JSON via Jackson |

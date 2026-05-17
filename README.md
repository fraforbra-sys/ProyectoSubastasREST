# Sistema de Subastas Distribuido - Spring Boot REST

Este proyecto es una migración del sistema de subastas online distribuido originalmente implementado con **Java RMI**, ahora reescrito como un **servicio web RESTful** usando **Spring Boot**. Ha sido desarrollado como práctica de la asignatura "Sistemas Distribuidos y Servicios Web" de la Escuela Superior de Ingeniería de la Universidad de Sevilla.

---

## Descripción

El sistema permite a los usuarios registrarse, autenticarse, crear subastas, realizar pujas y consultar el historial de subastas completadas. Incluye:

- **API REST completa** con documentación OpenAPI/Swagger
- **Interfaz web** integrada con Bootstrap 5 y JavaScript vanilla
- **Auto-finalización** de subastas mediante timers programados
- **Persistencia** en SQLite para usuarios e historial

---

## Arquitectura

- **Controladores REST** (`@RestController`): gestionan las peticiones HTTP y devuelven `ResponseEntity<T>` con los códigos de estado adecuados.
- **Servicios** (`@Service`): contienen la lógica de negocio. Las subastas activas se mantienen en memoria con un `ConcurrentHashMap`.
- **DAOs**: acceso a la base de datos SQLite mediante JDBC para usuarios y subastas completadas.
- **Base de datos**: SQLite (`db/subastas.db`), creada automáticamente al arrancar.
- **Interfaz web**: HTML/CSS/JS servidos desde `src/main/resources/static/`.

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
    ├── LoginRequest.java
    ├── CrearSubastaRequest.java
    ├── PujaRequest.java
    └── Usuario.java

src/main/resources/
├── static/
│   ├── index.html
│   ├── subastas.html
│   ├── subasta.html
│   ├── historial.html
│   ├── css/
│   │   └── styles.css
│   └── js/
│       ├── api.js
│       ├── auth.js
│       ├── subastas.js
│       ├── subasta.js
│       └── historial.js
└── application.properties
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

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

El servidor arranca en el puerto **9000**. La base de datos SQLite se inicializa automáticamente con usuarios de prueba:
- `admin` / `admin123`
- `usuario1` / `pass1`
- `usuario2` / `pass2`
- `cliente` / `cliente123`

---

## Interfaces disponibles

### Interfaz web
```
http://localhost:9000/
```

**Funcionalidades:**
- Login y registro de usuarios
- Listado de subastas activas con filtros (nombre, precio, tiempo restante)
- Creación de nuevas subastas
- Vista detallada de subasta con:
  - Contador de tiempo restante en vivo
  - Historial de pujas
  - Formulario para pujar
  - Auto-refresco cada 3 segundos
- Historial de subastas completadas con filtros

### Documentación API (Swagger UI)
```
http://localhost:9000/swagger-ui/index.html
```

Interfaz interactiva para probar todos los endpoints de la API REST.

---

## Tecnologías

- Java 17
- Spring Boot 3
- Maven (Maven Wrapper incluido)
- SQLite (sqlite-jdbc)
- Spring Security Crypto (BCrypt)
- SpringDoc OpenAPI (Swagger UI)
- Bootstrap 5 (CDN)
- JavaScript ES6 Modules

---

## Diferencias respecto a la versión RMI

| RMI | REST |
|-----|------|
| Interfaces remotas (`Remote`) | Controladores `@RestController` |
| `UnicastRemoteObject` | Servicios `@Service` con Spring |
| Callbacks Observer (`IObservadorCliente`) | Eliminado (REST es stateless, polling vía web) |
| `rmiregistry` | Servidor HTTP embebido (Tomcat) |
| Objetos `Serializable` por red | JSON via Jackson |
| Cliente Java RMI | Interfaz web con Bootstrap + fetch API |

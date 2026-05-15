# Prompt para Claude Code: Limpieza y mejora del proyecto ProyectoSubastasREST

## Situación actual

El repositorio `ProyectoSubastasREST` contiene código mezclado: el proyecto RMI original que NO debería estar aquí, y el proyecto REST que es el que hay que mantener. Además falta un DTO y hay un archivo Java inutilizado.

La estructura actual del repositorio es:

```
ProyectoSubastasREST/
├── cliente/                        ← BASURA (código RMI, ELIMINAR)
│   ├── ClienteSubastas.java
│   └── ObservadorClienteImpl.java
├── comun/                          ← BASURA (código RMI, ELIMINAR)
│   ├── Articulo.java
│   ├── BuscadorSubastas.java
│   ├── HistorialSubasta.java
│   ├── IGestorSubastas.java
│   ├── IObservadorCliente.java
│   ├── ISubasta.java
│   ├── Puja.java
│   ├── SubastaCompletada.java
│   └── Usuario.java
├── servidor/                       ← BASURA (código RMI, ELIMINAR)
│   ├── BuscadorSubastasImpl.java
│   ├── GestorSubastasImpl.java
│   ├── ServidorSubastas.java
│   ├── SubastaImpl.java
│   ├── dao/
│   │   ├── DatabaseManager.java
│   │   ├── SubastaCompletadaDAO.java
│   │   └── UsuarioDAO.java
│   └── servicio/
│       └── ServicioUsuarios.java
├── compile.sh                      ← BASURA (scripts RMI, ELIMINAR)
├── compile.bat                     ← BASURA (scripts RMI, ELIMINAR)
├── run-server.sh                   ← BASURA (scripts RMI, ELIMINAR)
├── run-server.bat                  ← BASURA (scripts RMI, ELIMINAR)
├── run-client.sh                   ← BASURA (scripts RMI, ELIMINAR)
├── run-client.bat                  ← BASURA (scripts RMI, ELIMINAR)
├── help.txt                        ← BASURA (ELIMINAR)
├── prompt_rmi_a_rest.md            ← BASURA (ELIMINAR)
├── DOCUMENTACION_BD.md             ← BASURA (ELIMINAR)
├── USUARIOS.md                     ← BASURA (ELIMINAR)
├── Memoria.md                      ← BASURA (ELIMINAR)
├── src/                            ← MANTENER (proyecto REST real)
│   └── main/java/com/subastas/
│       ├── SubastasApplication.java
│       ├── controlador/
│       │   ├── HistorialController.java
│       │   ├── SubastaController.java
│       │   └── UsuarioController.java    ← MODIFICAR (ver abajo)
│       ├── dao/
│       │   ├── DatabaseManager.java
│       │   ├── SubastaCompletadaDAO.java
│       │   └── UsuarioDAO.java
│       ├── modelo/
│       │   ├── Articulo.java
│       │   ├── CrearSubastaRequest.java
│       │   ├── HistorialSubasta.java     ← ELIMINAR (no se usa)
│       │   ├── Puja.java
│       │   ├── PujaRequest.java
│       │   ├── SubastaActiva.java
│       │   ├── SubastaCompletada.java
│       │   └── Usuario.java
│       └── servicio/
│           ├── ServicioHistorial.java
│           ├── ServicioSubastas.java
│           └── ServicioUsuarios.java
├── pom.xml                         ← MANTENER
├── mvnw.cmd                        ← MANTENER
├── .mvn/                           ← MANTENER
├── db/                             ← MANTENER
│   └── init.sql
├── .gitignore                      ← MANTENER
└── README.md                       ← MANTENER
```

---

## Tareas a realizar

### 1. Eliminar todo el código RMI y archivos basura

Elimina completamente estas carpetas y archivos del repositorio:

```
cliente/
comun/
servidor/
compile.sh
compile.bat
run-server.sh
run-server.bat
run-client.sh
run-client.bat
help.txt
prompt_rmi_a_rest.md
DOCUMENTACION_BD.md
USUARIOS.md
Memoria.md
```

### 2. Eliminar archivo inutilizado del proyecto REST

```
src/main/java/com/subastas/modelo/HistorialSubasta.java
```

Este archivo no se referencia en ninguna clase del proyecto REST. Era del RMI original.

### 3. Crear el DTO `LoginRequest.java`

Crea el archivo `src/main/java/com/subastas/modelo/LoginRequest.java` con este contenido exacto:

```java
package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales de usuario")
public class LoginRequest {

    @Schema(description = "Nombre de usuario", example = "admin")
    private String username;

    @Schema(description = "Contraseña", example = "admin123")
    private String password;

    public LoginRequest() {}

    public String getUsername() { return username; }

    @JsonProperty("username")
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    @JsonProperty("password")
    public void setPassword(String password) { this.password = password; }
}
```

### 4. Modificar `UsuarioController.java`

Sustituye el contenido actual de `src/main/java/com/subastas/controlador/UsuarioController.java` por este:

```java
package com.subastas.controlador;

import com.subastas.modelo.LoginRequest;
import com.subastas.servicio.ServicioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final ServicioUsuarios servicioUsuarios;

    @Autowired
    public UsuarioController(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest body) {
        String username = body.getUsername();
        String password = body.getPassword();
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username y password requeridos");
        }
        boolean valido = servicioUsuarios.validarLogin(username, password);
        if (valido) {
            return ResponseEntity.ok("Login exitoso: " + username);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody LoginRequest body) {
        String username = body.getUsername();
        String password = body.getPassword();
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username y password requeridos");
        }
        try {
            servicioUsuarios.registrarUsuario(username, password);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado: " + username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<String> existeUsuario(@PathVariable String username) {
        if (servicioUsuarios.existeUsuario(username)) {
            return ResponseEntity.ok("Usuario existe: " + username);
        }
        return ResponseEntity.notFound().build();
    }
}
```

---

## Verificación final

Una vez hechos los cambios:

1. Compila para verificar que no hay errores:
```powershell
.\mvnw.cmd clean compile
```

2. Confirma que la estructura final es exactamente:
```
ProyectoSubastasREST/
├── src/main/java/com/subastas/
│   ├── SubastasApplication.java
│   ├── controlador/
│   │   ├── HistorialController.java
│   │   ├── SubastaController.java
│   │   └── UsuarioController.java
│   ├── dao/
│   │   ├── DatabaseManager.java
│   │   ├── SubastaCompletadaDAO.java
│   │   └── UsuarioDAO.java
│   ├── modelo/
│   │   ├── Articulo.java
│   │   ├── CrearSubastaRequest.java
│   │   ├── LoginRequest.java
│   │   ├── Puja.java
│   │   ├── PujaRequest.java
│   │   ├── SubastaActiva.java
│   │   ├── SubastaCompletada.java
│   │   └── Usuario.java
│   └── servicio/
│       ├── ServicioHistorial.java
│       ├── ServicioSubastas.java
│       └── ServicioUsuarios.java
├── src/main/resources/
│   └── application.properties
├── db/
│   └── init.sql
├── pom.xml
├── mvnw.cmd
├── .mvn/
├── .gitignore
└── README.md
```

3. Haz commit y push:
```powershell
git add .
git commit -m "Limpieza: eliminar código RMI residual, añadir LoginRequest DTO"
git push
```

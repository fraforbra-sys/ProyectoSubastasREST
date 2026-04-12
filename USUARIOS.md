# Sistema de Registro de Usuarios Persistente

## Decisiones de Diseño

### 1. Base de Datos: SQLite
- **Elección**: SQLite en lugar de MySQL
- **Razón**: Es la opción más sencilla - no requiere servidor externo, la base de datos es un archivo local, y es perfecta para desarrollo y pruebas
- **Ubicación**: `db/subastas.db`

### 2. Hash de Contraseñas: BCrypt
- **Algoritmo**: BCrypt con 12 rounds (cost factor)
- **Librería**: `org.mindrot:jbcrypt` (estándar en Java)
- **Ventajas**:
  - Salt incorporado (cada hash es único incluso para misma contraseña)
  - Computacionalmente costoso (protege contra ataques de fuerza bruta)
  - Adaptable (se puede aumentar el costo con el tiempo)

### 3. Arquitectura en Capas

```
┌─────────────────────────────────────────────────┐
│           Cliente (ClienteSubastas)             │
│  - Menú de registro                             │
│  - Autenticación vía RMI                        │
└─────────────────────────────────────────────────┘
                      │
                      │ RMI
                      ▼
┌─────────────────────────────────────────────────┐
│      IGestorSubastas (Interfaz Remota)          │
│  - registrarUsuario()                           │
│  - autenticar()                                 │
│  - existeUsuario()                              │
└─────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│     GestorSubastasImpl (Servidor RMI)           │
│  - Delega a ServicioUsuarios                    │
└─────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│     ServicioUsuarios (Lógica de Negocio)        │
│  - Validación de datos                          │
│  - Hash de contraseñas (BCrypt)                 │
│  - Manejo de excepciones de negocio             │
└─────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│     UsuarioDAO (Acceso a Datos)                 │
│  - Operaciones CRUD                             │
│  - SQL puro                                     │
└─────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│     DatabaseManager (SQLite)                    │
│  - Conexión única                               │
│  - Inicialización de tablas                     │
└─────────────────────────────────────────────────┘
```

### 4. Validaciones Implementadas

- **Username**:
  - Longitud: 3-50 caracteres
  - Caracteres permitidos: alfanuméricos, guión bajo (_), guión (-)
  - No vacío
  - Único (verificado contra BD)

- **Contraseña**:
  - Longitud mínima: 4 caracteres
  - No vacía
  - Confirmación requerida en el cliente

## Estructura de la Base de Datos

```sql
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_username ON usuarios(username);
```

## Cómo Probar el Sistema

### 1. Compilar el Proyecto

```bash
cd /Escritorio/SDSW/ProyectoSubastasRMI
./compile.sh
```

### 2. Iniciar el Servidor

```bash
./run-server.sh
```

Verás un mensaje como:
```
[DB] Conectado a SQLite: db/subastas.db
[DB] Tabla 'usuarios' verificada/creada
[Gestor] Servicio de usuarios inicializado con BD
[Gestor] Usuario migrado: admin
...
```

### 3. Iniciar el Cliente

En otra terminal:
```bash
./run-client.sh
```

### 4. Probar Registro de Usuario

1. En el menú de autenticación, selecciona opción **2** (Registrarse)
2. Ingresa un nombre de usuario (ej: `testuser`)
3. Ingresa una contraseña (mínimo 4 caracteres)
4. Confirma la contraseña
5. Verás: `¡Registro exitoso! Ahora puede iniciar sesión.`

### 5. Probar Login

1. Ingresa el usuario registrado
2. Ingresa la contraseña
3. Si las credenciales son correctas: `Autenticación correcta. Bienvenido, testuser`

### 6. Verificar en la Base de Datos

```bash
sqlite3 db/subastas.db "SELECT id, username, password_hash FROM usuarios;"
```

Verás algo como:
```
1|admin|$2a$12$KIXx... (hash de 60 caracteres)
2|testuser|$2a$12$ABCx... (hash de 60 caracteres)
```

### 7. Casos de Prueba

| Caso | Resultado Esperado |
|------|-------------------|
| Registro con usuario vacío | Error: "El nombre de usuario no puede estar vacío" |
| Registro con usuario < 3 chars | Error: "debe tener al menos 3 caracteres" |
| Registro con usuario duplicado | Error: "ya está registrado" |
| Registro con contraseña < 4 chars | Error: "mínimo 4 caracteres" |
| Registro exitoso | Usuario creado, puede hacer login |
| Login con credenciales correctas | Acceso concedido |
| Login con contraseña incorrecta | "Credenciales inválidas" |
| Login con usuario inexistente | "Credenciales inválidas" |

## Usuarios por Defecto

El sistema migra automáticamente estos usuarios al iniciar el servidor:

| Usuario | Contraseña |
|---------|------------|
| admin | admin123 |
| usuario1 | pass1 |
| usuario2 | pass2 |
| cliente | cliente123 |

## Archivos Creados

```
ProyectoSubastasRMI/
├── comun/
│   └── Usuario.java              # Modelo de usuario
├── servidor/
│   ├── dao/
│   │   ├── DatabaseManager.java  # Gestión de conexión SQLite
│   │   └── UsuarioDAO.java       # Acceso a datos de usuarios
│   └── servicio/
│       └── ServicioUsuarios.java # Lógica de negocio y validaciones
├── db/
│   ├── init.sql                  # Script de inicialización
│   └── subastas.db               # Base de datos (generada)
├── lib/
│   ├── sqlite-jdbc.jar           # Driver SQLite
│   └── jbcrypt.jar               # Librería BCrypt
├── IGestorSubastas.java          # Interfaz actualizada
├── GestorSubastasImpl.java       # Implementación actualizada
└── ClienteSubastas.java          # Cliente con menú de registro
```

## Seguridad

- ✅ Contraseñas NUNCA se almacenan en texto plano
- ✅ BCrypt con salt incorporado (hash único por usuario)
- ✅ Cost factor de 12 (equilibrio seguridad/rendimiento)
- ✅ Validación de entrada en capa de servicio
- ✅ Excepciones de negocio propagadas correctamente
- ✅ Logs sin exponer información sensible

## Posibles Mejoras Futuras

1. **Email de verificación**: Añadir campo email y token de verificación
2. **Recuperación de contraseña**: Sistema de reset por email
3. **Roles de usuario**: Admin, usuario normal, etc.
4. **Intentos de login**: Bloqueo tras N intentos fallidos
5. **Password strength**: Validar complejidad (mayúsculas, números, símbolos)

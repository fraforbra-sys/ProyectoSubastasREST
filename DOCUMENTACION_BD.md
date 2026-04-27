# Documentación de la Base de Datos SQLite para Subastas Completadas



## 1. Modelo de Datos

### Tabla: `subastas_completadas`

```sql
CREATE TABLE IF NOT EXISTS subastas_completadas (
    id_subasta TEXT PRIMARY KEY,
    nombre_articulo TEXT NOT NULL,
    precio_final REAL NOT NULL,
    foto TEXT,
    comprador TEXT,
    fecha_finalizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_subastas_comprador ON subastas_completadas(comprador);
CREATE INDEX IF NOT EXISTS idx_subastas_fecha ON subastas_completadas(fecha_finalizacion);
```

### Campos

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id_subasta` | TEXT (PK) | Identificador único UUID de la subasta |
| `nombre_articulo` | TEXT | Nombre del artículo subastado |
| `precio_final` | REAL | Precio final de adjudicación en euros |
| `foto` | TEXT | URL o ruta de la imagen del artículo (almacenada como string) |
| `comprador` | TEXT | Nombre de usuario del ganador |
| `fecha_finalizacion` | TIMESTAMP | Fecha y hora de finalización (auto-generada) |

### Decisión sobre el campo `foto`

Se optó por almacenar la **ruta/URL como TEXT** en lugar de BLOB por las siguientes razones:

1. **Rendimiento:** Los BLOB incrementan significativamente el tamaño de la BD
2. **Simplicidad:** Las consultas son más rápidas sin BLOBs
3. **Flexibilidad:** Permite cambiar el almacenamiento de imágenes sin migrar la BD
4. **SQLite:** No está optimizado para BLOBs grandes

---

## 2. Compilación y Ejecución

### Requisitos Previos

- Java JDK 17 o superior
- Librerías en `lib/`:
  - `sqlite-jdbc.jar` - Driver JDBC para SQLite
  - `jbcrypt.jar` - Librería para hashing de contraseñas
  - `slf4j-api.jar` - Logging (requerido por jbcrypt)
  - `slf4j-simple.jar` - Implementación de logging

### 2.1 Compilar el Proyecto

```bash
cd /Escritorio/SDSW/ProyectoSubastasRMI
./compile.sh
```

**Opción manual (sin script):**

```bash
# Crear directorio de salida
mkdir -p bin

# Compilar clases comunes
javac -d bin comun/*.java

# Compilar DAOs
javac -cp "bin:lib/*.jar" -d bin servidor/dao/*.java

# Compilar servicio de usuarios
javac -cp "bin:lib/*.jar" -d bin servidor/servicio/*.java

# Compilar servidor principal
javac -cp "bin:lib/*.jar" -d bin servidor/*.java

# Compilar cliente
javac -cp "bin:lib/*.jar" -d bin cliente/*.java

# Copiar recursos
cp -r db bin/
```

### 2.2 Ejecutar el Servidor RMI

```bash
cd /Escritorio/SDSW/ProyectoSubastasRMI
./run-server.sh [puerto]
```

**Opción manual:**

```bash
export PUERTO=${1:-1099}
java -cp "bin:lib/sqlite-jdbc.jar:lib/jbcrypt.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" \
     -Djava.rmi.server.hostname=localhost \
     servidor.ServidorSubastas $PUERTO
```

**Salida esperada:**
```
========================================
   INICIANDO SERVIDOR DE SUBASTAS
   Puerto: 1099
   Classpath: bin:lib/...
========================================

[DB] Directorio creado: db
[DB] Conectado a SQLite: db/subastas.db
[DB] Tablas 'usuarios' y 'subastas_completadas' verificadas/creadas
[DB] Script de inicialización ejecutado
[DB] Inicialización completada
[Gestor] Servicio de usuarios inicializado con BD
[Gestor] Servidor inicializado
```

### 2.3 Ejecutar el Cliente

```bash
cd /Escritorio/SDSW/ProyectoSubastasRMI
./run-client.sh [puerto] [host]
```

**Opción manual:**

```bash
java -cp "bin:lib/sqlite-jdbc.jar:lib/jbcrypt.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar" \
     cliente.ClienteSubastas 1099 localhost
```

---

## 3. Inicialización de la Base de Datos

### Proceso Automático

La inicialización de la base de datos es **completamente automática**. No se requiere intervención manual.

### Flujo de Inicialización

1. **Al arrancar el servidor**, la clase `DatabaseManager` (patrón Singleton) se inicializa
2. **Crea el directorio** `db/` si no existe
3. **Conecta a SQLite** (crea el archivo `subastas.db` si no existe)
4. **Ejecuta `crearTablas()`**:
   - Crea `usuarios` si no existe
   - Crea `subastas_completadas` si no existe
   - Crea los índices necesarios
5. **Ejecuta el script** `db/init.sql` si existe (datos iniciales)

### Código de Inicialización

Ver `servidor/dao/DatabaseManager.java`:

```java
private void inicializarBaseDeDatos() {
    // 1. Crear directorio
    Path dbDir = Paths.get(DB_DIR);
    if (!Files.exists(dbDir)) {
        Files.createDirectories(dbDir);
    }

    // 2. Conectar a SQLite
    Class.forName("org.sqlite.JDBC");
    String url = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;
    conexion = DriverManager.getConnection(url);

    // 3. Crear tablas
    crearTablas();

    // 4. Ejecutar script de inicialización
    ejecutarScriptInicializacion();
}
```

### Reinicialización Manual (Opcional)

Si se necesita resetear la base de datos:

```bash
# Eliminar la base de datos existente
rm db/subastas.db

# Reiniciar el servidor (se recreará automáticamente)
./run-server.sh
```

---

## 4. Uso de SQLite desde Consola

### Acceder a la Base de Datos

```bash
cd /Escritorio/SDSW/ProyectoSubastasRMI
sqlite3 db/subastas.db
```

### Comandos Útiles

#### Mostrar todas las tablas
```sql
.tables
```

#### Ver esquema de una tabla
```sql
.schema subastas_completadas
.schema usuarios
```

#### Ver todas las subastas completadas
```sql
SELECT * FROM subastas_completadas ORDER BY fecha_finalizacion DESC;
```

#### Buscar subastas por comprador
```sql
SELECT * FROM subastas_completadas
WHERE comprador = 'usuario1'
ORDER BY fecha_finalizacion DESC;
```

#### Contar subastas por comprador
```sql
SELECT comprador, COUNT(*) as total, SUM(precio_final) as gastado
FROM subastas_completadas
GROUP BY comprador
ORDER BY total DESC;
```

#### Ver subastas en un rango de fechas
```sql
SELECT * FROM subastas_completadas
WHERE fecha_finalizacion BETWEEN '2026-01-01' AND '2026-12-31'
ORDER BY fecha_finalizacion;
```

#### Ver el precio promedio por artículo
```sql
SELECT nombre_articulo, AVG(precio_final) as precio_promedio
FROM subastas_completadas
GROUP BY nombre_articulo
ORDER BY precio_promedio DESC;
```

#### Exportar datos a CSV
```bash
sqlite3 -header -csv db/subastas.db "SELECT * FROM subastas_completadas;" > subastas_export.csv
```

### Comandos de SQLite (.commands)

| Comando | Descripción |
|---------|-------------|
| `.tables` | Listar todas las tablas |
| `.schema [tabla]` | Mostrar esquema de una tabla |
| `.headers on` | Mostrar encabezados en resultados |
| `.mode column` | Formato de columnas alineadas |
| `.mode csv` | Formato CSV para exportar |
| `.quit` o `.exit` | Salir de sqlite3 |
| `.import archivo.csv tabla` | Importar CSV a una tabla |
| `.backup archivo.db` | Crear copia de seguridad |

### Ejemplo de Sesión Completa

```bash
$ sqlite3 db/subastas.db

SQLite version 3.40.0 2022-12-28 14:03:47
Enter ".help" for usage hints.

sqlite> .headers on
sqlite> .mode column
sqlite> .tables
subastas_completadas  usuarios

sqlite> SELECT * FROM subastas_completadas;
id_subasta                            nombre_articulo  precio_final  foto                   comprador   fecha_finalizacion
------------------------------------  ---------------  ------------  ---------------------  ----------  -------------------
550e8400-e29b-41d4-a716-446655440000  Laptop Dell      850.50        /img/laptop-dell.jpg   usuario1    2026-04-27 10:30:00
550e8400-e29b-41d4-a716-446655440001  iPhone 15        720.00        /img/iphone15.jpg      cliente     2026-04-27 11:15:00

sqlite> SELECT comprador, COUNT(*) as subastas_ganadas FROM subastas_completadas GROUP BY comprador;
comprador   subastas_ganadas
----------  ----------------
cliente     1
usuario1    1

sqlite> .exit
```

---

## 5. Clases Implementadas

### Estructura del Proyecto

```
ProyectoSubastasRMI/
├── comun/
│   ├── SubastaCompletada.java    # Modelo de subasta completada
│   ├── IGestorSubastas.java      # Interfaz RMI (actualizada)
│   └── ...
├── servidor/
│   ├── dao/
│   │   ├── DatabaseManager.java      # Singleton de conexión (actualizado)
│   │   ├── SubastaCompletadaDAO.java # DAO nuevo para subastas
│   │   └── UsuarioDAO.java
│   ├── GestorSubastasImpl.java   # Implementación (actualizada)
│   ├── SubastaImpl.java          # Objeto remoto (actualizado)
│   └── ...
├── db/
│   ├── init.sql                  # Script de inicialización (actualizado)
│   └── subastas.db               # Base de datos SQLite
└── ...
```

### Clases Nuevas

| Clase | Paquete | Responsabilidad |
|-------|---------|-----------------|
| `SubastaCompletada` | `comun` | Modelo de datos serializable |
| `SubastaCompletadaDAO` | `servidor.dao` | Acceso a datos CRUD |

### Clases Actualizadas

| Clase | Cambios |
|-------|---------|
| `IGestorSubastas` | 4 nuevos métodos para consultar subastas completadas |
| `GestorSubastasImpl` | Implementación de los 4 métodos |
| `SubastaImpl` | Registro automático en BD al finalizar |
| `DatabaseManager` | Creación de tabla y método `crearSubastaCompletadaDAO()` |
| `db/init.sql` | Script de creación de tabla e índices |

---

## 6. Métodos RMI Disponibles

### Para Subastas Completadas

Desde `IGestorSubastas`:

```java
// Obtener todas las subastas completadas
List<SubastaCompletada> obtenerHistorialSubastas()

// Obtener subastas de un comprador específico
List<SubastaCompletada> obtenerSubastasPorComprador(String comprador)

// Obtener una subasta por ID
SubastaCompletada obtenerSubastaPorId(String idSubasta)

// Contar total de subastas completadas
int getNumeroSubastasCompletadas()
```

### Ejemplo de Uso desde el Cliente

```java
// Conectar al registry
IGestorSubastas gestor = (IGestorSubastas) registry.lookup("//localhost:1099/GestorSubastas");

// Obtener historial completo
List<SubastaCompletada> historial = gestor.obtenerHistorialSubastas();
for (SubastaCompletada s : historial) {
    System.out.println(s.getNombreArticulo() + " -> " + s.getComprador());
}

// Obtener subastas de un usuario
List<SubastaCompletada> misSubastas = gestor.obtenerSubastasPorComprador("usuario1");

// Contar subastas completadas
int total = gestor.getNumeroSubastasCompletadas();
System.out.println("Total subastas completadas: " + total);
```

---

## 7. Registro Automático al Finalizar

El registro en la base de datos se realiza **automáticamente** cuando:

1. **La subasta expira por tiempo** (`finalizarAutomaticamente()`)
2. **La subasta se finaliza manualmente** (`finalizar()`)

### Condiciones de Registro

- Solo se registra si **hay un comprador** (si hubo pujas)
- Si no hubo pujas, la subasta no se registra (se loguea pero no persiste)

### Código en `SubastaImpl.java`

```java
private void registrarEnBaseDeDatos() {
    DatabaseManager dbManager = DatabaseManager.getInstancia();
    SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();

    String nombreArticulo = articulo.getNombre();
    String foto = articulo.getUrlImagen();
    double precioFinal = precioActual;
    String comprador = liderActual;

    if (comprador != null) {
        SubastaCompletada subasta = new SubastaCompletada(
            idSubasta, nombreArticulo, precioFinal, foto,
            comprador, new Timestamp(System.currentTimeMillis())
        );
        dao.insertarSubasta(subasta);
    }
}
```

---


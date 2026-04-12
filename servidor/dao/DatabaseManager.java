package servidor.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de la conexión a la base de datos SQLite.
 * Se encarga de:
 * - Inicializar la base de datos si no existe
 * - Crear las tablas necesarias
 * - Proporcionar conexiones
 *
 * Patrón: Singleton por aplicación
 */
public class DatabaseManager {

    private static final String DB_DIR = "db";
    private static final String DB_FILE = "subastas.db";
    private static final String INIT_SQL_FILE = "db/init.sql";

    private static DatabaseManager instancia;
    private Connection conexion;

    /**
     * Constructor privado para patrón Singleton.
     * Inicializa la base de datos al crear la instancia.
     */
    private DatabaseManager() {
        inicializarBaseDeDatos();
    }

    /**
     * Obtiene la instancia única del gestor de base de datos.
     * @return Instancia singleton
     */
    public static synchronized DatabaseManager getInstancia() {
        if (instancia == null) {
            instancia = new DatabaseManager();
        }
        return instancia;
    }

    /**
     * Inicializa la base de datos: crea directorio, archivo y tablas.
     */
    private void inicializarBaseDeDatos() {
        try {
            // Crear directorio si no existe
            Path dbDir = Paths.get(DB_DIR);
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
                System.out.println("[DB] Directorio creado: " + DB_DIR);
            }

            // Cargar driver SQLite
            Class.forName("org.sqlite.JDBC");

            // Conectar (crea el archivo si no existe)
            String url = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;
            conexion = DriverManager.getConnection(url);
            System.out.println("[DB] Conectado a SQLite: " + DB_DIR + "/" + DB_FILE);

            // Crear tablas
            crearTablas();

            // Ejecutar script de inicialización si existe
            ejecutarScriptInicializacion();

            System.out.println("[DB] Inicialización completada");

        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ERROR: Driver de SQLite no encontrado. " +
                    "Asegúrate de incluir sqlite-jdbc.jar en el classpath.");
            throw new RuntimeException("Driver de SQLite no disponible", e);
        } catch (SQLException e) {
            System.err.println("[DB] ERROR de SQL: " + e.getMessage());
            throw new RuntimeException("Error de base de datos", e);
        } catch (IOException e) {
            System.err.println("[DB] ERROR de E/S: " + e.getMessage());
            throw new RuntimeException("Error al crear directorio de BD", e);
        }
    }

    /**
     * Crea las tablas necesarias en la base de datos.
     */
    private void crearTablas() throws SQLException {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String sqlIndex = """
            CREATE INDEX IF NOT EXISTS idx_usuarios_username
            ON usuarios(username)
        """;

        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlIndex);
            System.out.println("[DB] Tabla 'usuarios' verificada/creada");
        }
    }

    /**
     * Ejecuta el script de inicialización si existe.
     */
    private void ejecutarScriptInicializacion() {
        File scriptFile = new File(INIT_SQL_FILE);
        if (!scriptFile.exists()) {
            System.out.println("[DB] No se encontró script de inicialización, se usa estructura por defecto");
            return;
        }

        try {
            String sql = Files.readString(scriptFile.toPath());
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute(sql);
                System.out.println("[DB] Script de inicialización ejecutado");
            }
        } catch (SQLException | IOException e) {
            System.err.println("[DB] Error al ejecutar script de inicialización: " + e.getMessage());
            // No es crítico, continuar
        }
    }

    /**
     * Obtiene una conexión activa a la base de datos.
     * @return Conexión JDBC
     * @throws SQLException Si la conexión está cerrada
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("La conexión a la base de datos está cerrada");
        }
        return conexion;
    }

    /**
     * Crea un nuevo DAO de usuarios usando esta conexión.
     * @return Nueva instancia de UsuarioDAO
     * @throws SQLException Si la conexión no está disponible
     */
    public UsuarioDAO crearUsuarioDAO() throws SQLException {
        return new UsuarioDAO(getConexion());
    }

    /**
     * Cierra la conexión a la base de datos.
     * Debe llamarse al apagar la aplicación.
     */
    public void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("[DB] Conexión cerrada");
            } catch (SQLException e) {
                System.err.println("[DB] Error al cerrar: " + e.getMessage());
            }
        }
    }
}

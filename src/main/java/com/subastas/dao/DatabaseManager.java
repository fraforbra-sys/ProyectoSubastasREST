package com.subastas.dao;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseManager {
    private static final String DB_DIR = "db";
    private static final String DB_FILE = "subastas.db";

    private Connection conexion;

    @PostConstruct
    public void inicializar() {
        try {
            Path dbDir = Paths.get(DB_DIR);
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
            }
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;
            conexion = DriverManager.getConnection(url);
            System.out.println("[DB] Conectado a SQLite: " + DB_DIR + "/" + DB_FILE);
            crearTablas();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite no encontrado", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error de base de datos", e);
        } catch (IOException e) {
            throw new RuntimeException("Error al crear directorio de BD", e);
        }
    }

    private void crearTablas() throws SQLException {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        String sqlSubastas = """
            CREATE TABLE IF NOT EXISTS subastas_completadas (
                id_subasta TEXT PRIMARY KEY,
                nombre_articulo TEXT NOT NULL,
                precio_final REAL NOT NULL,
                foto TEXT,
                comprador TEXT,
                fecha_finalizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlSubastas);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subastas_comprador ON subastas_completadas(comprador)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_subastas_fecha ON subastas_completadas(fecha_finalizacion)");
        }
        System.out.println("[DB] Tablas verificadas/creadas");
    }

    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("Conexión cerrada");
        }
        return conexion;
    }

    @PreDestroy
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

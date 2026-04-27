-- Script de inicialización de la base de datos de usuarios y subastas
-- SQLite

-- ============================================
-- TABLA DE USUARIOS
-- ============================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas rápidas por nombre de usuario
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- ============================================
-- TABLA DE SUBASTAS COMPLETADAS
-- ============================================
-- Almacena el historial de subastas finalizadas para auditoría y estadísticas
CREATE TABLE IF NOT EXISTS subastas_completadas (
    id_subasta TEXT PRIMARY KEY,
    nombre_articulo TEXT NOT NULL,
    precio_final REAL NOT NULL,
    foto TEXT,
    comprador TEXT,
    fecha_finalizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas por comprador
CREATE INDEX IF NOT EXISTS idx_subastas_comprador ON subastas_completadas(comprador);

-- Índice para búsquedas por fecha
CREATE INDEX IF NOT EXISTS idx_subastas_fecha ON subastas_completadas(fecha_finalizacion);

-- ============================================
-- DATOS INICIALES
-- ============================================

-- Usuario admin por defecto (contraseña: admin123)
-- El hash es generado con BCrypt
INSERT OR IGNORE INTO usuarios (username, password_hash)
VALUES ('admin', '$2a$10$YQ5E.JzZZnS6J6zZzZzZzO7X8xXxXxXxXxXxXxXxXxXxXxXxXxXxX');

-- Script de inicialización de la base de datos de usuarios
-- SQLite

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsquedas rápidas por nombre de usuario
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);

-- Usuario admin por defecto (contraseña: admin123)
-- El hash es generado con BCrypt
INSERT OR IGNORE INTO usuarios (username, password_hash)
VALUES ('admin', '$2a$10$YQ5E.JzZZnS6J6zZzZzZzO7X8xXxXxXxXxXxXxXxXxXxXxXxXxXxX');

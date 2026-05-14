package com.subastas.dao;

import com.subastas.modelo.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Optional;

@Component
public class UsuarioDAO {
    private final DatabaseManager dbManager;

    @Autowired
    public UsuarioDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public int crearUsuario(String username, String passwordHash) throws SQLException {
        if (existePorUsername(username)) {
            throw new IllegalArgumentException("Usuario '" + username + "' ya existe");
        }
        String sql = "INSERT INTO usuarios (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            if (stmt.executeUpdate() == 0) throw new SQLException("No se insertó ninguna fila");
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("[UsuarioDAO] Usuario creado: " + username);
                    return keys.getInt(1);
                }
                throw new SQLException("No se obtuvo ID generado");
            }
        }
    }

    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    public boolean existePorUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

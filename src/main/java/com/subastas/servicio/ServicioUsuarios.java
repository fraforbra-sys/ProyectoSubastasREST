package com.subastas.servicio;

import com.subastas.dao.UsuarioDAO;
import com.subastas.modelo.Usuario;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Service
public class ServicioUsuarios {
    private static final int BCRYPT_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;

    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_ROUNDS);

    @Autowired
    public ServicioUsuarios(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @PostConstruct
    public void migrarUsuariosIniciales() {
        Map<String, String> usuariosPrueba = Map.of(
            "admin", "admin123",
            "usuario1", "pass1",
            "usuario2", "pass2",
            "cliente", "cliente123"
        );
        for (Map.Entry<String, String> entry : usuariosPrueba.entrySet()) {
            try {
                if (!usuarioDAO.existePorUsername(entry.getKey())) {
                    registrarUsuario(entry.getKey(), entry.getValue());
                    System.out.println("[ServicioUsuarios] Usuario inicial creado: " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println("[ServicioUsuarios] Error al crear usuario inicial " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    public Usuario registrarUsuario(String username, String password) {
        validarUsername(username);
        validarPassword(password);
        try {
            if (usuarioDAO.existePorUsername(username)) {
                throw new IllegalArgumentException("Usuario '" + username + "' ya registrado");
            }
            String hash = passwordEncoder.encode(password);
            int id = usuarioDAO.crearUsuario(username, hash);
            return new Usuario(id, username);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Error de BD al registrar usuario", e);
        }
    }

    public boolean validarLogin(String username, String password) {
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorUsername(username);
            if (usuarioOpt.isEmpty()) return false;
            return passwordEncoder.matches(password, usuarioOpt.get().getPasswordHash());
        } catch (SQLException e) {
            throw new RuntimeException("Error de BD al validar login", e);
        }
    }

    public boolean existeUsuario(String username) {
        try {
            return usuarioDAO.existePorUsername(username);
        } catch (SQLException e) {
            throw new RuntimeException("Error de BD al verificar usuario", e);
        }
    }

    private void validarUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username no puede estar vacío");
        String u = username.trim();
        if (u.length() < MIN_USERNAME_LENGTH)
            throw new IllegalArgumentException("Username debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres");
        if (u.length() > MAX_USERNAME_LENGTH)
            throw new IllegalArgumentException("Username no puede exceder " + MAX_USERNAME_LENGTH + " caracteres");
        if (!u.matches("^[a-zA-Z0-9_-]+$"))
            throw new IllegalArgumentException("Username solo puede contener letras, números, guiones y guiones bajos");
    }

    private void validarPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password no puede estar vacío");
        if (password.length() < MIN_PASSWORD_LENGTH)
            throw new IllegalArgumentException("Password debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
    }
}

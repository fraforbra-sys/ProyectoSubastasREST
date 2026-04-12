package servidor.dao;

import comun.Usuario;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object (DAO) para la gestión de usuarios en la base de datos.
 * Esta clase maneja todas las operaciones CRUD relacionadas con usuarios.
 *
 * Capa de acceso a datos - solo se encarga de SQL, sin lógica de negocio.
 */
public class UsuarioDAO {

    private final Connection conexion;

    /**
     * Constructor que recibe una conexión activa a la base de datos.
     * @param conexion Conexión JDBC a SQLite
     */
    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param username Nombre de usuario
     * @param passwordHash Hash de la contraseña (ya procesado con BCrypt)
     * @return El ID autogenerado del nuevo usuario
     * @throws SQLException Si hay error de base de datos
     * @throws IllegalArgumentException Si el usuario ya existe
     */
    public int crearUsuario(String username, String passwordHash) throws SQLException {
        // Verificar primero si el usuario ya existe
        if (existePorUsername(username)) {
            throw new IllegalArgumentException("El usuario '" + username + "' ya existe");
        }

        String sql = "INSERT INTO usuarios (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("Error al crear el usuario: no se insertó ninguna fila");
            }

            // Obtener el ID autogenerado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    System.out.println("[DAO] Usuario creado: " + username + " (ID=" + id + ")");
                    return id;
                } else {
                    throw new SQLException("Error al obtener el ID del usuario creado");
                }
            }
        }
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * @param username Nombre de usuario a buscar
     * @return Optional con el usuario si existe, vacío si no
     * @throws SQLException Si hay error de base de datos
     */
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String user = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    return Optional.of(new Usuario(id, user, hash));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Verifica si existe un usuario con el nombre especificado.
     * @param username Nombre de usuario a verificar
     * @return true si el usuario existe, false en caso contrario
     * @throws SQLException Si hay error de base de datos
     */
    public boolean existePorUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Valida las credenciales de un usuario comparando el hash.
     * @param username Nombre de usuario
     * @param passwordHash Hash de la contraseña a validar
     * @return true si las credenciales son válidas, false en caso contrario
     * @throws SQLException Si hay error de base de datos
     */
    public boolean validarCredenciales(String username, String passwordHash) throws SQLException {
        return buscarPorUsername(username)
                .map(u -> u.getPasswordHash().equals(passwordHash))
                .orElse(false);
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     * @throws SQLException Si hay error de base de datos
     */
    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String user = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    return Optional.of(new Usuario(userId, user, hash));
                }
                return Optional.empty();
            }
        }
    }
}

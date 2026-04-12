package servidor.servicio;

import comun.Usuario;
import servidor.dao.DatabaseManager;
import servidor.dao.UsuarioDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Capa de servicio/lógica de negocio para la gestión de usuarios.
 *
 * Responsabilidades:
 * - Validación de datos de entrada
 * - Hash de contraseñas con BCrypt
 * - Coordinación con la capa DAO
 * - Manejo de excepciones de negocio
 *
 * Esta clase NO es remota, se usa internamente en el servidor.
 */
public class ServicioUsuarios {

    private static final int BCRYPT_ROUNDS = 12;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor que recibe el DAO de usuarios.
     * @param dao Instancia de UsuarioDAO
     */
    public ServicioUsuarios(UsuarioDAO dao) {
        this.usuarioDAO = dao;
    }

    /**
     * Factory method para crear un ServicioUsuarios desde el DatabaseManager.
     * @return Nueva instancia de ServicioUsuarios
     * @throws SQLException Si hay error de base de datos
     */
    public static ServicioUsuarios crearDesdeDatabaseManager() throws SQLException {
        DatabaseManager dbManager = DatabaseManager.getInstancia();
        return new ServicioUsuarios(dbManager.crearUsuarioDAO());
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param username Nombre de usuario deseado
     * @param password Contraseña en texto plano (será hasheada)
     * @return Usuario creado con su ID
     * @throws IllegalArgumentException Si los datos son inválidos o el usuario ya existe
     * @throws RuntimeException Si hay error técnico (BD, etc.)
     */
    public Usuario registrarUsuario(String username, String password) {
        // 1. Validar datos de entrada
        validarUsername(username);
        validarPassword(password);

        try {
            // 2. Verificar si el usuario ya existe
            if (usuarioDAO.existePorUsername(username)) {
                throw new IllegalArgumentException(
                    "El usuario '" + username + "' ya está registrado"
                );
            }

            // 3. Hashear la contraseña con BCrypt
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));

            // 4. Guardar en la base de datos
            int nuevoId = usuarioDAO.crearUsuario(username, passwordHash);

            // 5. Devolver el usuario creado (sin el hash por seguridad)
            return new Usuario(nuevoId, username);

        } catch (IllegalArgumentException e) {
            // Propagar excepciones de negocio
            throw e;
        } catch (SQLException e) {
            System.err.println("[ServicioUsuarios] Error de BD al registrar: " + e.getMessage());
            throw new RuntimeException("Error al registrar usuario en la base de datos", e);
        }
    }

    /**
     * Valida las credenciales de un usuario para login.
     *
     * @param username Nombre de usuario
     * @param password Contraseña en texto plano
     * @return true si las credenciales son válidas, false en caso contrario
     * @throws RuntimeException Si hay error técnico
     */
    public boolean validarLogin(String username, String password) {
        try {
            // 1. Obtener el usuario de la base de datos
            Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorUsername(username);

            if (usuarioOpt.isEmpty()) {
                // Usuario no encontrado
                System.out.println("[ServicioUsuarios] Login fallido: usuario no existe - " + username);
                return false;
            }

            Usuario usuario = usuarioOpt.get();

            // 2. Verificar el hash con BCrypt
            boolean passwordValida = BCrypt.checkpw(password, usuario.getPasswordHash());

            if (passwordValida) {
                System.out.println("[ServicioUsuarios] Login exitoso: " + username);
            } else {
                System.out.println("[ServicioUsuarios] Login fallido: contraseña incorrecta - " + username);
            }

            return passwordValida;

        } catch (SQLException e) {
            System.err.println("[ServicioUsuarios] Error de BD al validar login: " + e.getMessage());
            throw new RuntimeException("Error al validar credenciales", e);
        }
    }

    /**
     * Verifica si un nombre de usuario ya está registrado.
     *
     * @param username Nombre de usuario a verificar
     * @return true si el usuario ya existe, false en caso contrario
     * @throws RuntimeException Si hay error técnico
     */
    public boolean existeUsuario(String username) {
        try {
            return usuarioDAO.existePorUsername(username);
        } catch (SQLException e) {
            System.err.println("[ServicioUsuarios] Error de BD al verificar existencia: " + e.getMessage());
            throw new RuntimeException("Error al verificar existencia de usuario", e);
        }
    }

    /**
     * Valida que el nombre de usuario cumpla los requisitos.
     *
     * @param username Nombre de usuario a validar
     * @throws IllegalArgumentException Si el username es inválido
     */
    private void validarUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }

        String usernameLimpio = username.trim();

        if (usernameLimpio.length() < MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                "El nombre de usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres"
            );
        }

        if (usernameLimpio.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                "El nombre de usuario no puede exceder " + MAX_USERNAME_LENGTH + " caracteres"
            );
        }

        // Validar caracteres permitidos (alfanuméricos, guión bajo, guión)
        if (!usernameLimpio.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(
                "El nombre de usuario solo puede contener letras, números, guiones y guiones bajos"
            );
        }
    }

    /**
     * Valida que la contraseña cumpla los requisitos mínimos.
     *
     * @param password Contraseña a validar
     * @throws IllegalArgumentException Si la contraseña es inválida
     */
    private void validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres"
            );
        }
    }
}

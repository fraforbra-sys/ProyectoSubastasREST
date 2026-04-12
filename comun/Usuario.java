package comun;

import java.io.Serializable;

/**
 * Modelo de datos que representa un usuario del sistema.
 * Esta clase es inmutable y segura para usar en transferencias RMI.
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L; // Versión de serialización para compatibilidad entre versiones

    private final int id;
    private final String username;
    private final String passwordHash;

    /**
     * Constructor completo para crear un usuario desde la base de datos.
     * @param id ID único autogenerado
     * @param username Nombre de usuario
     * @param passwordHash Hash de la contraseña (BCrypt)
     */
    public Usuario(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Constructor simplificado para transferencias (sin exponer el hash).
     * @param id ID único
     * @param username Nombre de usuario
     */
    public Usuario(int id, String username) {
        this(id, username, null);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Obtiene el hash de la contraseña.
     * Solo debe usarse internamente para validación.
     * @return Hash BCrypt de la contraseña
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "'}";
    }

/**
 * Los métodos equals y hashCode se basan únicamente en el ID del usuario, ya que es un identificador único.
 * Esto permite comparar usuarios de manera eficiente y usar objetos Usuario en colecciones como HashSet o HashMap sin problemas.
 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz remota del gestor principal de subastas (Servicio Factory).
 * Actúa como punto de entrada principal y fábrica de objetos Subasta.
 * Es un servicio Stateless que gestiona el registro y búsqueda de subastas.
 */
public interface IGestorSubastas extends Remote {
    /**
     * Autentica a un usuario en el sistema.
     * @param usuario Nombre de usuario
     * @param password Contraseña
     * @return true si las credenciales son válidas
     * @throws RemoteException si hay error de comunicación
     */
    boolean autenticar(String usuario, String password) throws RemoteException;

    /**
     * Crea una nueva subasta para el artículo especificado.
     * Método Factory que devuelve una referencia remota a la nueva subasta.
     * @param articulo El artículo a subastar
     * @param duracionSegundos Duración de la subasta en segundos
     * @return Referencia remota a la nueva subasta creada
     * @throws RemoteException si hay error de comunicación
     */
    ISubasta crearSubasta(Articulo articulo, long duracionSegundos) throws RemoteException;

    /**
     * Busca subastas activas que coincidan con el filtro.
     * @param filtro Texto a buscar en el nombre del artículo (case-insensitive)
     * @return Lista de referencias a subastas activas que coinciden
     * @throws RemoteException si hay error de comunicación
     */
    List<ISubasta> buscarSubastas(String filtro) throws RemoteException;

    /**
     * Obtiene todas las subastas activas.
     * @return Lista de referencias a todas las subastas activas
     * @throws RemoteException si hay error de comunicación
     */
    List<ISubasta> listarSubastasActivas() throws RemoteException;

    /**
     * Obtiene el número total de subastas activas.
     * @return Número de subastas activas
     * @throws RemoteException si hay error de comunicación
     */
    int getNumeroSubastasActivas() throws RemoteException;
}

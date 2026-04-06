package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz remota que representa una subasta activa (objeto Stateful).
 * Cada subasta mantiene su propio estado: precio actual, líder, tiempo restante,
 * lista de observadores y historial de pujas.
 */
public interface ISubasta extends Remote {
    /**
     * Obtiene el identificador único de esta subasta.
     */
    String getIdSubasta() throws RemoteException;

    /**
     * Obtiene el artículo que se está subastando.
     */
    Articulo getArticulo() throws RemoteException;

    /**
     * Obtiene el precio actual de la subasta.
     */
    double getPrecioActual() throws RemoteException;

    /**
     * Obtiene el usuario que lidera actualmente la subasta.
     * @return null si no hay pujas aún
     */
    String getLiderActual() throws RemoteException;

    /**
     * Obtiene el tiempo restante en segundos.
     */
    long getTiempoRestante() throws RemoteException;

    /**
     * Verifica si la subasta aún está activa.
     */
    boolean isActiva() throws RemoteException;

    /**
     * Realiza una puja en esta subasta.
     * @param usuario Nombre del usuario que puja
     * @param cantidad Cantidad ofrecida
     * @return true si la puja fue aceptada, false si fue rechazada
     * @throws RemoteException si hay error de comunicación
     */
    boolean pujar(String usuario, double cantidad) throws RemoteException;

    /**
     * Registra un observador para recibir callbacks cuando haya nuevas pujas.
     * @param observador Referencia remota al cliente observador
     * @throws RemoteException si hay error de comunicación
     */
    void registrarObservador(IObservadorCliente observador) throws RemoteException;

    /**
     * Elimina un observador de la lista.
     * @param observador Referencia remota al cliente a eliminar
     * @throws RemoteException si hay error de comunicación
     */
    void eliminarObservador(IObservadorCliente observador) throws RemoteException;

    /**
     * Obtiene el historial completo de la subasta.
     * Devuelve un objeto Serializable con toda la información.
     * @throws RemoteException si hay error de comunicación
     */
    HistorialSubasta obtenerDetalles() throws RemoteException;

    /**
     * Finaliza la subasta anticipadamente.
     * @throws RemoteException si hay error de comunicación
     */
    void finalizar() throws RemoteException;
}

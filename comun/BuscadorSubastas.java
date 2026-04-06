package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz remota para un servicio de búsqueda Stateless.
 * Este servicio permite buscar subastas sin mantener estado del cliente.
 * Se diferencia del Gestor principal en que es puramente funcional:
 * recibe filtros y devuelve resultados sin crear objetos ni mantener estado.
 */
public interface BuscadorSubastas extends Remote {
    /**
     * Busca subastas activas por rango de precios.
     * @param precioMinimo Precio mínimo actual de la subasta
     * @param precioMaximo Precio máximo actual de la subasta
     * @return Lista de subastas dentro del rango de precios
     * @throws RemoteException si hay error de comunicación
     */
    List<ISubasta> buscarPorPrecio(double precioMinimo, double precioMaximo)
            throws RemoteException;

    /**
     * Busca subastas activas por texto en el nombre del artículo.
     * @param texto Texto a buscar (case-insensitive)
     * @return Lista de subastas cuyo artículo contiene el texto
     * @throws RemoteException si hay error de comunicación
     */
    List<ISubasta> buscarPorNombre(String texto) throws RemoteException;

    /**
     * Obtiene las subastas que están a punto de finalizar.
     * @param segundos Umbral de segundos restantes
     * @return Lista de subastas que finalizan en menos de 'segundos' segundos
     * @throws RemoteException si hay error de comunicación
     */
    List<ISubasta> buscarPorTiempoRestante(long segundos) throws RemoteException;
}

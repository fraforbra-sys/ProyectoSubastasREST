package comun;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz remota para el patrón Observer con callbacks.
 * Los clientes implementan esta interfaz para recibir notificaciones
 * cuando se realiza una nueva puja en una subasta.
 */
public interface IObservadorCliente extends Remote {
    /**
     * Método callback que invoca el servidor cuando hay una nueva puja.
     * @param idSubasta Identificador de la subasta donde se realizó la puja
     * @param nuevoPrecio El nuevo precio actual de la subasta
     * @param usuarioPuja El usuario que realizó la puja
     * @throws RemoteException si hay error de comunicación con el cliente
     */
    void notificarNuevaPuja(String idSubasta, double nuevoPrecio, String usuarioPuja)
            throws RemoteException;
}

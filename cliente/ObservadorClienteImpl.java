package cliente;

import comun.IObservadorCliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementación del observador cliente para recibir callbacks.
 * Cuando el servidor notifica una nueva puja, este objeto recibe la llamada.
 *
 * Debe extender UnicastRemoteObject para poder ser exportado como objeto RMI.
 */
public class ObservadorClienteImpl extends UnicastRemoteObject implements IObservadorCliente {
    private static final long serialVersionUID = 1L;

    private final String nombreCliente;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public ObservadorClienteImpl(String nombreCliente) throws RemoteException {
        super();
        this.nombreCliente = nombreCliente;
    }

    @Override
    public void notificarNuevaPuja(String idSubasta, double nuevoPrecio, String usuarioPuja)
            throws RemoteException {
        String hora = sdf.format(new Date());

        // Mostrar la notificación recibida del servidor
        System.out.println("[" + hora + "] *** NOTIFICACIÓN *** " +
                           "Subasta " + idSubasta.substring(0, 8) + "... : " +
                           "Nueva puja de '" + usuarioPuja + "' por " + nuevoPrecio + "€");

        // Aquí el cliente podría actualizar su UI, guardar en base de datos, etc.
    }

    public String getNombreCliente() {
        return nombreCliente;
    }
}

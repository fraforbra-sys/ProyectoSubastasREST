package servidor;

import comun.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de búsqueda (Stateless).
 * Este servicio no mantiene estado del cliente, simplemente recibe
 * filtros y devuelve resultados de subastas activas.
 *
 * Se diferencia del Gestor en que es puramente funcional.
 */
public class BuscadorSubastasImpl extends UnicastRemoteObject implements BuscadorSubastas {
    private static final long serialVersionUID = 1L;

    // Referencia al almacén de subastas (solo lectura, no modifica el estado)
    private final Map<String, SubastaImpl> subastasActivas;

    public BuscadorSubastasImpl(Map<String, SubastaImpl> subastasActivas) throws RemoteException {
        super();
        this.subastasActivas = subastasActivas;
    }

    @Override
    public List<ISubasta> buscarPorPrecio(double precioMinimo, double precioMaximo)
            throws RemoteException {
        if (precioMinimo > precioMaximo) {
            throw new RemoteException("precioMinimo no puede ser mayor que precioMaximo");
        }

        List<ISubasta> resultado = new ArrayList<>();
        for (SubastaImpl subasta : subastasActivas.values()) {
            try {
                if (subasta.isActiva()) {
                    double precioActual = subasta.getPrecioActual();
                    if (precioActual >= precioMinimo && precioActual <= precioMaximo) {
                        resultado.add(subasta);
                    }
                }
            } catch (RemoteException e) {
                // Ignorar subastas con errores
            }
        }

        System.out.println("[Buscador] Búsqueda por precio [" + precioMinimo + " - " +
                           precioMaximo + "]: " + resultado.size() + " resultados");
        return resultado;
    }

    @Override
    public List<ISubasta> buscarPorNombre(String texto) throws RemoteException {
        if (texto == null || texto.trim().isEmpty()) {
            // Si no hay filtro, devolver todas
            return listarTodasActivas();
        }

        String filtro = texto.toLowerCase().trim();
        List<ISubasta> resultado = new ArrayList<>();

        for (SubastaImpl subasta : subastasActivas.values()) {
            try {
                if (subasta.isActiva()) {
                    String nombreArticulo = subasta.getArticulo().getNombre().toLowerCase();
                    if (nombreArticulo.contains(filtro)) {
                        resultado.add(subasta);
                    }
                }
            } catch (RemoteException e) {
                // Ignorar subastas con errores
            }
        }

        System.out.println("[Buscador] Búsqueda por nombre '" + texto + "': " +
                           resultado.size() + " resultados");
        return resultado;
    }

    @Override
    public List<ISubasta> buscarPorTiempoRestante(long segundos) throws RemoteException {
        List<ISubasta> resultado = new ArrayList<>();

        for (SubastaImpl subasta : subastasActivas.values()) {
            try {
                if (subasta.isActiva()) {
                    long tiempoRestante = subasta.getTiempoRestante();
                    if (tiempoRestante <= segundos) {
                        resultado.add(subasta);
                    }
                }
            } catch (RemoteException e) {
                // Ignorar subastas con errores
            }
        }

        System.out.println("[Buscador] Búsqueda por tiempo restante (<= " + segundos +
                           "s): " + resultado.size() + " resultados");
        return resultado;
    }

    /**
     * Lista todas las subastas activas.
     */
    private List<ISubasta> listarTodasActivas() throws RemoteException {
        List<ISubasta> resultado = new ArrayList<>();
        for (SubastaImpl subasta : subastasActivas.values()) {
            try {
                if (subasta.isActiva()) {
                    resultado.add(subasta);
                }
            } catch (RemoteException e) {
                // Ignorar subastas con errores
            }
        }
        return resultado;
    }
}

package servidor;

import comun.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación del gestor principal de subastas (Servicio Factory).
 * Actúa como punto de entrada único y fábrica de objetos Subasta.
 * Mantiene el registro de todas las subastas activas.
 */
public class GestorSubastasImpl extends UnicastRemoteObject implements IGestorSubastas {
    private static final long serialVersionUID = 1L;

    // Mapa de usuarios válidos (en producción esto iría a una base de datos)
    private final Map<String, String> usuariosValidos;

    // Almacén de subastas activas (ConcurrentHashMap para acceso thread-safe)
    private final ConcurrentHashMap<String, SubastaImpl> subastasActivas;

    // Instancia única del buscador (servicio Stateless)
    private final BuscadorSubastasImpl buscador;

    public GestorSubastasImpl() throws RemoteException {
        super();
        this.usuariosValidos = new HashMap<>();
        this.subastasActivas = new ConcurrentHashMap<>();
        this.buscador = new BuscadorSubastasImpl(subastasActivas);

        // Usuarios de prueba (en producción usar autenticación real)
        usuariosValidos.put("admin", "admin123");
        usuariosValidos.put("usuario1", "pass1");
        usuariosValidos.put("usuario2", "pass2");
        usuariosValidos.put("cliente", "cliente123");

        System.out.println("[Gestor] Servidor inicializado con " + usuariosValidos.size() + " usuarios");
    }

    @Override
    public boolean autenticar(String usuario, String password) throws RemoteException {
        if (usuario == null || password == null) {
            return false;
        }
        String passwordAlmacenado = usuariosValidos.get(usuario);
        boolean autenticado = passwordAlmacenado != null && passwordAlmacenado.equals(password);

        if (autenticado) {
            System.out.println("[Gestor] Usuario '" + usuario + "' autenticado correctamente");
        } else {
            System.out.println("[Gestor] Intento de autenticación fallido para usuario: " + usuario);
        }

        return autenticado;
    }

    @Override
    public ISubasta crearSubasta(Articulo articulo, long duracionSegundos) throws RemoteException {
        if (articulo == null || duracionSegundos <= 0) {
            throw new RemoteException("Parámetros inválidos para crear subasta");
        }

        SubastaImpl nuevaSubasta = new SubastaImpl(articulo, duracionSegundos);
        subastasActivas.put(nuevaSubasta.getIdSubasta(), nuevaSubasta);

        System.out.println("[Gestor] Creada nueva subasta: " + articulo.getNombre() +
                           " (ID: " + nuevaSubasta.getIdSubasta().substring(0, 8) + "...)");

        return nuevaSubasta;
    }

    @Override
    public List<ISubasta> buscarSubastas(String filtro) throws RemoteException {
        return buscador.buscarPorNombre(filtro);
    }

    /**
     * Método adicional para búsqueda por precio (expuesto desde el buscador).
     */
    public List<ISubasta> buscarSubastasPorPrecio(double precioMinimo, double precioMaximo)
            throws RemoteException {
        return buscador.buscarPorPrecio(precioMinimo, precioMaximo);
    }

    @Override
    public List<ISubasta> listarSubastasActivas() throws RemoteException {
        limpiarSubastasFinalizadas();
        return new ArrayList<>(subastasActivas.values());
    }

    @Override
    public int getNumeroSubastasActivas() throws RemoteException {
        limpiarSubastasFinalizadas();
        return subastasActivas.size();
    }

    /**
     * Limpia las subastas que ya han finalizado para liberar memoria.
     */
    private void limpiarSubastasFinalizadas() {
        subastasActivas.entrySet().removeIf(entry -> {
            try {
                return !entry.getValue().isActiva();
            } catch (RemoteException e) {
                return true; // Eliminar si hay error
            }
        });
    }

    /**
     * Obtiene el buscador asociado para búsquedas avanzadas.
     */
    public BuscadorSubastasImpl getBuscador() {
        return buscador;
    }
}

package servidor;

import comun.*;
import servidor.dao.DatabaseManager;
import servidor.dao.SubastaCompletadaDAO;
import servidor.dao.UsuarioDAO;
import servidor.servicio.ServicioUsuarios;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
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

    // Servicio de gestión de usuarios (con base de datos)
    private final ServicioUsuarios servicioUsuarios;

    // Mapa de usuarios válidos en memoria para caché de usuarios cargados desde BD
    private final Map<String, String> usuariosValidos;

    // Almacén de subastas activas (ConcurrentHashMap para acceso thread-safe)
    private final ConcurrentHashMap<String, SubastaImpl> subastasActivas;

    // Instancia única del buscador (servicio Stateless)
    private final BuscadorSubastasImpl buscador;

    public GestorSubastasImpl() throws RemoteException {
        super();

        // Inicializar la base de datos y el servicio de usuarios
        ServicioUsuarios su;
        try {
            su = ServicioUsuarios.crearDesdeDatabaseManager();
            System.out.println("[Gestor] Servicio de usuarios inicializado con BD");
        } catch (SQLException e) {
            System.err.println("[Gestor] ERROR al inicializar BD de usuarios: " + e.getMessage());
            throw new RemoteException("No se pudo inicializar el sistema de usuarios", e);
        }
        this.servicioUsuarios = su;

        this.usuariosValidos = new HashMap<>();
        this.subastasActivas = new ConcurrentHashMap<>();
        this.buscador = new BuscadorSubastasImpl(subastasActivas);

        // Usuarios de prueba iniciales (se migran a BD si no existen)
        // En producción, estos usuarios deberían estar solo en BD
        migrarUsuariosSiEsNecesario();

        System.out.println("[Gestor] Servidor inicializado");
    }

    /**
     * Migra los usuarios de prueba a la base de datos si no existen.
     * Método temporal para compatibilidad.
     */
    private void migrarUsuariosSiEsNecesario() {
        Map<String, String> usuariosPrueba = new HashMap<>();
        usuariosPrueba.put("admin", "admin123");
        usuariosPrueba.put("usuario1", "pass1");
        usuariosPrueba.put("usuario2", "pass2");
        usuariosPrueba.put("cliente", "cliente123");

        for (Map.Entry<String, String> entry : usuariosPrueba.entrySet()) {
            try {
                if (!servicioUsuarios.existeUsuario(entry.getKey())) {
                    servicioUsuarios.registrarUsuario(entry.getKey(), entry.getValue());
                    System.out.println("[Gestor] Usuario migrado: " + entry.getKey());
                }
                // Cargar a memoria para caché
                usuariosValidos.put(entry.getKey(), entry.getValue());
            } catch (IllegalArgumentException e) {
                // Ya existe, ignorar
            } catch (Exception e) {
                System.err.println("[Gestor] Error al migrar usuario " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public boolean autenticar(String usuario, String password) throws RemoteException {
        if (usuario == null || password == null) {
            return false;
        }

        // Usar el servicio de usuarios para validar contra la base de datos
        try {
            return servicioUsuarios.validarLogin(usuario, password);
        } catch (Exception e) {
            System.err.println("[Gestor] Error en autenticación: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean registrarUsuario(String username, String password) throws RemoteException {
        try {
            servicioUsuarios.registrarUsuario(username, password);
            System.out.println("[Gestor] Usuario registrado exitosamente: " + username);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("[Gestor] Registro fallido: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[Gestor] Error al registrar usuario: " + e.getMessage());
            throw new RemoteException("Error al registrar usuario: " + e.getMessage());
        }
    }

    @Override
    public boolean existeUsuario(String username) throws RemoteException {
        try {
            return servicioUsuarios.existeUsuario(username);
        } catch (Exception e) {
            System.err.println("[Gestor] Error al verificar existencia de usuario: " + e.getMessage());
            throw new RemoteException("Error al verificar existencia de usuario", e);
        }
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

    // ============================================
    // IMPLEMENTACIÓN DE MÉTODOS PARA SUBASTAS COMPLETADAS
    // ============================================

    @Override
    public List<SubastaCompletada> obtenerHistorialSubastas() throws RemoteException {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstancia();
            SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();
            return dao.obtenerTodas();
        } catch (SQLException e) {
            System.err.println("[Gestor] Error al obtener historial de subastas: " + e.getMessage());
            throw new RemoteException("Error al obtener historial de subastas", e);
        }
    }

    @Override
    public List<SubastaCompletada> obtenerSubastasPorComprador(String comprador) throws RemoteException {
        if (comprador == null || comprador.trim().isEmpty()) {
            throw new RemoteException("Nombre de comprador inválido");
        }

        try {
            DatabaseManager dbManager = DatabaseManager.getInstancia();
            SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();
            return dao.obtenerPorComprador(comprador);
        } catch (SQLException e) {
            System.err.println("[Gestor] Error al obtener subastas por comprador: " + e.getMessage());
            throw new RemoteException("Error al obtener subastas por comprador", e);
        }
    }

    @Override
    public SubastaCompletada obtenerSubastaPorId(String idSubasta) throws RemoteException {
        if (idSubasta == null || idSubasta.trim().isEmpty()) {
            throw new RemoteException("ID de subasta inválido");
        }

        try {
            DatabaseManager dbManager = DatabaseManager.getInstancia();
            SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();
            return dao.obtenerPorId(idSubasta);
        } catch (SQLException e) {
            System.err.println("[Gestor] Error al obtener subasta por ID: " + e.getMessage());
            throw new RemoteException("Error al obtener subasta por ID", e);
        }
    }

    @Override
    public int getNumeroSubastasCompletadas() throws RemoteException {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstancia();
            SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();
            return dao.contarTotal();
        } catch (SQLException e) {
            System.err.println("[Gestor] Error al contar subastas completadas: " + e.getMessage());
            throw new RemoteException("Error al contar subastas completadas", e);
        }
    }
}

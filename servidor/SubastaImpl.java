package servidor;

import comun.*;
import servidor.dao.DatabaseManager;
import servidor.dao.SubastaCompletadaDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementación del objeto remoto Subasta (Stateful).
 * Mantiene el estado interno de una subasta: precio, líder, tiempo, observadores.
 * Cada instancia representa una subasta concreta y activa.
 */
public class SubastaImpl extends UnicastRemoteObject implements ISubasta {
    private static final long serialVersionUID = 1L;

    private final String idSubasta;
    private final Articulo articulo;
    private double precioActual;
    private String liderActual;
    private final List<Puja> historialPujas;
    private final List<IObservadorCliente> observadores;
    private long tiempoFin;
    private boolean activa;
    private final Object bloqueo = new Object();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SubastaImpl(Articulo articulo, long duracionSegundos) throws RemoteException {
        super();
        this.idSubasta = UUID.randomUUID().toString();
        this.articulo = articulo;
        this.precioActual = articulo.getPrecioSalida();
        this.liderActual = null;
        this.historialPujas = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.tiempoFin = System.currentTimeMillis() + (duracionSegundos * 1000);
        this.activa = true;

        // Programar finalización automática
        scheduler.schedule(this::finalizarAutomaticamente, duracionSegundos, TimeUnit.SECONDS);
    }

    private void finalizarAutomaticamente() {
        synchronized (bloqueo) {
            if (activa) {
                System.out.println("[Subasta " + idSubasta + "] Tiempo agotado. Finalizando...");
                activa = false;
                registrarEnBaseDeDatos();
            }
        }
    }

    @Override
    public String getIdSubasta() throws RemoteException {
        return idSubasta;
    }

    @Override
    public Articulo getArticulo() throws RemoteException {
        return articulo;
    }

    @Override
    public double getPrecioActual() throws RemoteException {
        synchronized (bloqueo) {
            return precioActual;
        }
    }

    @Override
    public String getLiderActual() throws RemoteException {
        synchronized (bloqueo) {
            return liderActual;
        }
    }

    @Override
    public long getTiempoRestante() throws RemoteException {
        synchronized (bloqueo) {
            long restante = (tiempoFin - System.currentTimeMillis()) / 1000;
            return Math.max(0, restante);
        }
    }

    @Override
    public boolean isActiva() throws RemoteException {
        synchronized (bloqueo) {
            return activa && (tiempoFin > System.currentTimeMillis());
        }
    }

    @Override
    public boolean pujar(String usuario, double cantidad) throws RemoteException {
        synchronized (bloqueo) {
            // Verificar si la subasta está activa
            if (!activa || tiempoFin <= System.currentTimeMillis()) {
                System.out.println("[Subasta " + idSubasta + "] Puja rechazada: subasta finalizada");
                return false;
            }

            // Verificar si la puja es válida (mayor que el precio actual)
            if (cantidad <= precioActual) {
                System.out.println("[Subasta " + idSubasta + "] Puja rechazada de " + usuario +
                                   ": cantidad " + cantidad + " <= precio actual " + precioActual);
                return false;
            }

            // Aceptar la puja
            Puja nuevaPuja = new Puja(usuario, cantidad);
            historialPujas.add(nuevaPuja);
            precioActual = cantidad;
            liderActual = usuario;

            System.out.println("[Subasta " + idSubasta + "] Nueva puja de " + usuario +
                               ": " + cantidad + "€ (líder actual)");

            // Notificar a todos los observadores (callback)
            notificarObservadores(usuario, cantidad);

            return true;
        }
    }

    /**
     * Notifica a todos los observadores registrados sobre la nueva puja.
     * Captura excepciones individualmente para evitar que un cliente desconectado
     * bloquee las notificaciones a otros clientes.
     */
    private void notificarObservadores(String usuarioPuja, double nuevoPrecio) {
        List<IObservadorCliente> observadoresAEliminar = new ArrayList<>();

        for (IObservadorCliente observador : observadores) {
            try {
                observador.notificarNuevaPuja(idSubasta, nuevoPrecio, usuarioPuja);
            } catch (RemoteException e) {
                // El cliente probablemente se desconectó
                System.out.println("[Subasta " + idSubasta + "] Error al notificar observador: " +
                                   e.getMessage());
                observadoresAEliminar.add(observador);
            } catch (Exception e) {
                // Capturar cualquier otra excepción para no bloquear el sistema
                System.out.println("[Subasta " + idSubasta + "] Excepción al notificar: " +
                                   e.getMessage());
            }
        }

        // Eliminar observadores desconectados
        if (!observadoresAEliminar.isEmpty()) {
            observadores.removeAll(observadoresAEliminar);
            System.out.println("[Subasta " + idSubasta + "] Eliminados " +
                               observadoresAEliminar.size() + " observadores desconectados");
        }
    }

    @Override
    public void registrarObservador(IObservadorCliente observador) throws RemoteException {
        synchronized (bloqueo) {
            if (!observadores.contains(observador)) {
                observadores.add(observador);
                System.out.println("[Subasta " + idSubasta + "] Nuevo observador registrado. Total: " +
                                   observadores.size());
            }
        }
    }

    @Override
    public void eliminarObservador(IObservadorCliente observador) throws RemoteException {
        synchronized (bloqueo) {
            if (observadores.remove(observador)) {
                System.out.println("[Subasta " + idSubasta + "] Observador eliminado. Total: " +
                                   observadores.size());
            }
        }
    }

    @Override
    public HistorialSubasta obtenerDetalles() throws RemoteException {
        synchronized (bloqueo) {
            return new HistorialSubasta(
                articulo,
                historialPujas,
                precioActual,
                liderActual,
                activa && (tiempoFin > System.currentTimeMillis())
            );
        }
    }

    @Override
    public void finalizar() throws RemoteException {
        synchronized (bloqueo) {
            if (activa) {
                activa = false;
                System.out.println("[Subasta " + idSubasta + "] Finalizada manualmente");
                scheduler.shutdown();
                registrarEnBaseDeDatos();
            }
        }
    }

    /**
     * Registra la subasta completada en la base de datos.
     * Se llama automáticamente cuando la subasta finaliza (por tiempo o manualmente).
     */
    private void registrarEnBaseDeDatos() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstancia();
            SubastaCompletadaDAO dao = dbManager.crearSubastaCompletadaDAO();

            // Obtener datos de la subasta
            String nombreArticulo = articulo.getNombre();
            String foto = articulo.getUrlImagen();
            double precioFinal = precioActual;
            String comprador = liderActual; // Puede ser null si no hubo pujas

            // Solo registrar si hay un comprador (si hubo pujas)
            if (comprador != null) {
                SubastaCompletada subasta = new SubastaCompletada(
                    idSubasta,
                    nombreArticulo,
                    precioFinal,
                    foto,
                    comprador,
                    new Timestamp(System.currentTimeMillis())
                );

                dao.insertarSubasta(subasta);
                System.out.println("[Subasta " + idSubasta + "] Registrada en BD: " +
                                   nombreArticulo + " -> " + comprador + " (" + precioFinal + "€)");
            } else {
                System.out.println("[Subasta " + idSubasta + "] No se registra en BD: sin pujas");
            }

        } catch (SQLException e) {
            System.err.println("[Subasta " + idSubasta + "] Error al registrar en BD: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Subasta " + idSubasta + "] Excepción al registrar en BD: " + e.getMessage());
        }
    }

    /**
     * Limpia recursos cuando el garbage collector recoge el objeto.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            scheduler.shutdown();
        } finally {
            super.finalize();
        }
    }
}

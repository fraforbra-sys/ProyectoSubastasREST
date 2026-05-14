package com.subastas.servicio;

import com.subastas.dao.SubastaCompletadaDAO;
import com.subastas.modelo.Articulo;
import com.subastas.modelo.SubastaActiva;
import com.subastas.modelo.SubastaCompletada;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ServicioSubastas {
    private final ConcurrentHashMap<String, SubastaActiva> subastas = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final SubastaCompletadaDAO subastaCompletadaDAO;

    @Autowired
    public ServicioSubastas(SubastaCompletadaDAO subastaCompletadaDAO) {
        this.subastaCompletadaDAO = subastaCompletadaDAO;
    }

    public SubastaActiva crearSubasta(Articulo articulo, long duracionSegundos) {
        String id = UUID.randomUUID().toString();
        SubastaActiva subasta = new SubastaActiva(id, articulo, duracionSegundos);
        subastas.put(id, subasta);
        scheduler.schedule(() -> finalizarYPersistir(id), duracionSegundos, TimeUnit.SECONDS);
        System.out.println("[ServicioSubastas] Creada subasta: " + articulo.getNombre() +
                           " (ID: " + id.substring(0, 8) + "...) duración: " + duracionSegundos + "s");
        return subasta;
    }

    public List<SubastaActiva> listarActivas() {
        return new ArrayList<>(subastas.values());
    }

    public List<SubastaActiva> buscarPorFiltro(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) return listarActivas();
        String filtroLower = filtro.toLowerCase();
        return subastas.values().stream()
            .filter(s -> s.getArticulo().getNombre().toLowerCase().contains(filtroLower))
            .collect(Collectors.toList());
    }

    public Optional<SubastaActiva> obtenerPorId(String id) {
        return Optional.ofNullable(subastas.get(id));
    }

    public boolean pujar(String idSubasta, String usuario, double cantidad) {
        SubastaActiva subasta = subastas.get(idSubasta);
        if (subasta == null) return false;
        return subasta.pujar(usuario, cantidad);
    }

    public boolean finalizarSubasta(String idSubasta) {
        SubastaActiva subasta = subastas.get(idSubasta);
        if (subasta == null) return false;
        if (subasta.finalizar()) {
            subastas.remove(idSubasta);
            persistirSubastaCompletada(subasta);
            return true;
        }
        return false;
    }

    public List<SubastaActiva> buscarPorRangoPrecio(double precioMin, double precioMax) {
        return subastas.values().stream()
            .filter(s -> s.getPrecioActual() >= precioMin && s.getPrecioActual() <= precioMax)
            .collect(Collectors.toList());
    }

    public List<SubastaActiva> buscarPorTiempoRestante(long segundos) {
        return subastas.values().stream()
            .filter(s -> s.getTiempoRestanteSegundos() <= segundos)
            .collect(Collectors.toList());
    }

    private void finalizarYPersistir(String id) {
        SubastaActiva subasta = subastas.get(id);
        if (subasta == null) return;
        if (subasta.finalizar()) {
            subastas.remove(id);
            persistirSubastaCompletada(subasta);
            System.out.println("[ServicioSubastas] Subasta expirada y procesada: " + id.substring(0, 8) + "...");
        }
    }

    private void persistirSubastaCompletada(SubastaActiva subasta) {
        if (subasta.getLiderActual() == null) {
            System.out.println("[ServicioSubastas] Subasta sin pujas, no se persiste: " + subasta.getId());
            return;
        }
        try {
            SubastaCompletada completada = new SubastaCompletada(
                subasta.getId(),
                subasta.getArticulo().getNombre(),
                subasta.getPrecioActual(),
                subasta.getArticulo().getUrlImagen(),
                subasta.getLiderActual(),
                new Timestamp(System.currentTimeMillis())
            );
            subastaCompletadaDAO.insertarSubasta(completada);
        } catch (SQLException e) {
            System.err.println("[ServicioSubastas] Error al persistir subasta: " + e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }
}

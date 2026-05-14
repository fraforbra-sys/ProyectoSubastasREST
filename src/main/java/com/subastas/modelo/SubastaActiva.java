package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public class SubastaActiva {
    private final String id;
    private final Articulo articulo;
    private final List<Puja> pujas = new ArrayList<>();
    private volatile double precioActual;
    private volatile String liderActual;
    private final long tiempoFin;
    private volatile boolean activa = true;

    @JsonIgnore
    private final Object bloqueo = new Object();

    public SubastaActiva(String id, Articulo articulo, long duracionSegundos) {
        this.id = id;
        this.articulo = articulo;
        this.precioActual = articulo.getPrecioSalida();
        this.liderActual = null;
        this.tiempoFin = System.currentTimeMillis() + (duracionSegundos * 1000L);
    }

    public boolean pujar(String usuario, double cantidad) {
        synchronized (bloqueo) {
            if (!activa || tiempoFin <= System.currentTimeMillis()) return false;
            if (cantidad <= precioActual) return false;
            pujas.add(new Puja(usuario, cantidad));
            precioActual = cantidad;
            liderActual = usuario;
            return true;
        }
    }

    public boolean finalizar() {
        synchronized (bloqueo) {
            if (!activa) return false;
            activa = false;
            return true;
        }
    }

    public String getId() { return id; }
    public Articulo getArticulo() { return articulo; }

    public List<Puja> getPujas() {
        synchronized (bloqueo) {
            return new ArrayList<>(pujas);
        }
    }

    public double getPrecioActual() { return precioActual; }
    public String getLiderActual() { return liderActual; }
    public boolean isActiva() { return activa; }

    public long getTiempoRestanteSegundos() {
        long restante = (tiempoFin - System.currentTimeMillis()) / 1000L;
        return Math.max(0, restante);
    }
}

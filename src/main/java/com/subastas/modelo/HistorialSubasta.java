package com.subastas.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistorialSubasta {
    private final Articulo articulo;
    private final List<Puja> pujas;
    private final double precioActual;
    private final String liderActual;
    private final boolean activa;

    public HistorialSubasta(Articulo articulo, List<Puja> pujas,
                            double precioActual, String liderActual, boolean activa) {
        this.articulo = articulo;
        this.pujas = new ArrayList<>(pujas);
        this.precioActual = precioActual;
        this.liderActual = liderActual;
        this.activa = activa;
    }

    public Articulo getArticulo() { return articulo; }
    public List<Puja> getPujas() { return Collections.unmodifiableList(pujas); }
    public double getPrecioActual() { return precioActual; }
    public String getLiderActual() { return liderActual; }
    public boolean isActiva() { return activa; }
    public int getNumeroPujas() { return pujas.size(); }
}

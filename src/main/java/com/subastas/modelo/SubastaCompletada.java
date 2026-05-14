package com.subastas.modelo;

import java.sql.Timestamp;

public class SubastaCompletada {
    private final String idSubasta;
    private final String nombreArticulo;
    private final double precioFinal;
    private final String foto;
    private final String comprador;
    private final Timestamp fechaFinalizacion;

    public SubastaCompletada(String idSubasta, String nombreArticulo, double precioFinal,
                             String foto, String comprador, Timestamp fechaFinalizacion) {
        this.idSubasta = idSubasta;
        this.nombreArticulo = nombreArticulo;
        this.precioFinal = precioFinal;
        this.foto = foto;
        this.comprador = comprador;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public SubastaCompletada(String idSubasta, String nombreArticulo, double precioFinal,
                             String foto, String comprador) {
        this(idSubasta, nombreArticulo, precioFinal, foto, comprador,
             new Timestamp(System.currentTimeMillis()));
    }

    public String getIdSubasta() { return idSubasta; }
    public String getNombreArticulo() { return nombreArticulo; }
    public double getPrecioFinal() { return precioFinal; }
    public String getFoto() { return foto; }
    public String getComprador() { return comprador; }
    public Timestamp getFechaFinalizacion() { return fechaFinalizacion; }
}

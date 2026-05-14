package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PujaRequest {
    private String usuario;
    private double cantidad;

    public PujaRequest() {}

    public String getUsuario() { return usuario; }

    @JsonProperty("usuario")
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public double getCantidad() { return cantidad; }

    @JsonProperty("cantidad")
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
}

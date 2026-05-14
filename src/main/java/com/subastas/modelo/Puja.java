package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Puja {
    private final String usuario;
    private final double cantidad;
    private final Date timestamp;

    @JsonCreator
    public Puja(
            @JsonProperty("usuario") String usuario,
            @JsonProperty("cantidad") double cantidad) {
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.timestamp = new Date();
    }

    public String getUsuario() { return usuario; }
    public double getCantidad() { return cantidad; }
    public Date getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Puja{usuario='" + usuario + "', cantidad=" + cantidad + "}";
    }
}

package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Articulo {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String urlImagen;
    private final double precioSalida;

    @JsonCreator
    public Articulo(
            @JsonProperty("id") String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("descripcion") String descripcion,
            @JsonProperty("urlImagen") String urlImagen,
            @JsonProperty("precioSalida") double precioSalida) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.precioSalida = precioSalida;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public double getPrecioSalida() { return precioSalida; }

    @Override
    public String toString() {
        return "Articulo{id='" + id + "', nombre='" + nombre + "', precioSalida=" + precioSalida + "}";
    }
}

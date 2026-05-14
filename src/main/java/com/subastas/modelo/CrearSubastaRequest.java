package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearSubastaRequest {
    private Articulo articulo;
    private long duracionSegundos;

    public CrearSubastaRequest() {}

    public Articulo getArticulo() { return articulo; }

    @JsonProperty("articulo")
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public long getDuracionSegundos() { return duracionSegundos; }

    @JsonProperty("duracionSegundos")
    public void setDuracionSegundos(long duracionSegundos) { this.duracionSegundos = duracionSegundos; }
}

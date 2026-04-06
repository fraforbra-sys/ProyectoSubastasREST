package comun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase que representa el historial completo de una subasta.
 * Contiene el artículo subastado y todas las pujas realizadas.
 * Implementa Serializable para ser devuelto como objeto complejo en las llamadas RMI.
 */
public class HistorialSubasta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Articulo articulo;
    private final List<Puja> pujas;
    private final double precioActual;
    private final String liderActual;
    private final boolean activa;

    public HistorialSubasta(Articulo articulo, List<Puja> pujas,
                           double precioActual, String liderActual, boolean activa) {
        this.articulo = articulo;
        this.pujas = new ArrayList<>(pujas); // Copia defensiva
        this.precioActual = precioActual;
        this.liderActual = liderActual;
        this.activa = activa;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public List<Puja> getPujas() {
        return Collections.unmodifiableList(pujas);
    }

    public double getPrecioActual() {
        return precioActual;
    }

    public String getLiderActual() {
        return liderActual;
    }

    public boolean isActiva() {
        return activa;
    }

    public int getNumeroPujas() {
        return pujas.size();
    }

    @Override
    public String toString() {
        return "HistorialSubasta{articulo=" + articulo +
               ", numPujas=" + pujas.size() +
               ", precioActual=" + precioActual +
               ", liderActual='" + liderActual + "'" +
               ", activa=" + activa + "}";
    }
}

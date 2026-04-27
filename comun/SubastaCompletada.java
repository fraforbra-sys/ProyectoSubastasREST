package comun;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Clase que representa una subasta completada y almacenada en la base de datos.
 * Implementa Serializable para ser devuelto como objeto en las llamadas RMI.
 */
public class SubastaCompletada implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String idSubasta;
    private final String nombreArticulo;
    private final double precioFinal;
    private final String foto;      // URL o ruta de la imagen (almacenada como TEXT en BD)
    private final String comprador;
    private final Timestamp fechaFinalizacion;

    /**
     * Constructor completo para crear una subasta completada.
     * @param idSubasta Identificador único de la subasta
     * @param nombreArticulo Nombre del artículo subastado
     * @param precioFinal Precio final al que se adjudicó
     * @param foto URL o ruta de la imagen del artículo
     * @param comprador Usuario que ganó la subasta
     * @param fechaFinalizacion Fecha y hora de finalización
     */
    public SubastaCompletada(String idSubasta, String nombreArticulo, double precioFinal,
                             String foto, String comprador, Timestamp fechaFinalizacion) {
        this.idSubasta = idSubasta;
        this.nombreArticulo = nombreArticulo;
        this.precioFinal = precioFinal;
        this.foto = foto;
        this.comprador = comprador;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    /**
     * Constructor simplificado sin fecha (se usa CURRENT_TIMESTAMP en BD).
     */
    public SubastaCompletada(String idSubasta, String nombreArticulo, double precioFinal,
                             String foto, String comprador) {
        this(idSubasta, nombreArticulo, precioFinal, foto, comprador, new Timestamp(System.currentTimeMillis()));
    }

    // Getters

    public String getIdSubasta() {
        return idSubasta;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public double getPrecioFinal() {
        return precioFinal;
    }

    public String getFoto() {
        return foto;
    }

    public String getComprador() {
        return comprador;
    }

    public Timestamp getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    @Override
    public String toString() {
        return "SubastaCompletada{" +
               "idSubasta='" + idSubasta + '\'' +
               ", nombreArticulo='" + nombreArticulo + '\'' +
               ", precioFinal=" + precioFinal + "€" +
               ", comprador='" + comprador + '\'' +
               ", fechaFinalizacion=" + fechaFinalizacion +
               '}';
    }
}

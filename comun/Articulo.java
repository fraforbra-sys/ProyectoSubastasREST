package comun;

import java.io.Serializable;

/**
 * Clase que representa un artículo que se va a subastar.
 * Contiene metadatos, descripciones y URL de imagen.
 * Implementa Serializable para ser enviado por valor en las llamadas RMI.
 */
public class Articulo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String urlImagen;
    private final double precioSalida;

    public Articulo(String id, String nombre, String descripcion,
                    String urlImagen, double precioSalida) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.urlImagen = urlImagen;
        this.precioSalida = precioSalida;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public double getPrecioSalida() {
        return precioSalida;
    }

    @Override
    public String toString() {
        return "Articulo{id='" + id + "', nombre='" + nombre +
               "', precioSalida=" + precioSalida + "}";
    }
}

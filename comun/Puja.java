package comun;

import java.io.Serializable;
import java.util.Date;

/**
 * Clase que representa una puja realizada en una subasta.
 * Implementa Serializable para poder ser enviada por valor a través de RMI.
 */
public class Puja implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String usuario;
    private final double cantidad;
    private final Date timestamp;

    public Puja(String usuario, double cantidad) {
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.timestamp = new Date();
    }

    public String getUsuario() {
        return usuario;
    }

    public double getCantidad() {
        return cantidad;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Puja{usuario='" + usuario + "', cantidad=" + cantidad +
               ", timestamp=" + timestamp + "}";
    }
}

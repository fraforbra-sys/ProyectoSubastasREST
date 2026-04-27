package servidor.dao;

import comun.SubastaCompletada;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de subastas completadas en la base de datos.
 * Esta clase maneja todas las operaciones CRUD relacionadas con subastas finalizadas.
 *
 * Capa de acceso a datos - solo se encarga de SQL, sin lógica de negocio.
 */
public class SubastaCompletadaDAO {

    private final Connection conexion;

    /**
     * Constructor que recibe una conexión activa a la base de datos.
     * @param conexion Conexión JDBC a SQLite
     */
    public SubastaCompletadaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Inserta una nueva subasta completada en la base de datos.
     * @param subasta Objeto con los datos de la subasta completada
     * @throws SQLException Si hay error de base de datos
     */
    public void insertarSubasta(SubastaCompletada subasta) throws SQLException {
        String sql = """
            INSERT INTO subastas_completadas
            (id_subasta, nombre_articulo, precio_final, foto, comprador, fecha_finalizacion)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, subasta.getIdSubasta());
            stmt.setString(2, subasta.getNombreArticulo());
            stmt.setDouble(3, subasta.getPrecioFinal());
            stmt.setString(4, subasta.getFoto());
            stmt.setString(5, subasta.getComprador());
            stmt.setTimestamp(6, subasta.getFechaFinalizacion());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar la subasta completada");
            }

            System.out.println("[DAO] Subasta completada insertada: " + subasta.getIdSubasta() +
                               " - " + subasta.getNombreArticulo() + " (" + subasta.getPrecioFinal() + "€)");
        }
    }

    /**
     * Obtiene todas las subastas completadas almacenadas.
     * @return Lista de subastas completadas
     * @throws SQLException Si hay error de base de datos
     */
    public List<SubastaCompletada> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM subastas_completadas ORDER BY fecha_finalizacion DESC";
        List<SubastaCompletada> subastas = new ArrayList<>();

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                subastas.add(mapearResultSet(rs));
            }
        }

        return subastas;
    }

    /**
     * Obtiene una subasta completada por su ID.
     * @param idSubasta Identificador de la subasta
     * @return La subasta si existe, null en caso contrario
     * @throws SQLException Si hay error de base de datos
     */
    public SubastaCompletada obtenerPorId(String idSubasta) throws SQLException {
        String sql = "SELECT * FROM subastas_completadas WHERE id_subasta = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, idSubasta);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Obtiene todas las subastas completadas por un comprador específico.
     * @param comprador Nombre del comprador
     * @return Lista de subastas ganadas por ese usuario
     * @throws SQLException Si hay error de base de datos
     */
    public List<SubastaCompletada> obtenerPorComprador(String comprador) throws SQLException {
        String sql = """
            SELECT * FROM subastas_completadas
            WHERE comprador = ?
            ORDER BY fecha_finalizacion DESC
        """;
        List<SubastaCompletada> subastas = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, comprador);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subastas.add(mapearResultSet(rs));
                }
            }
        }

        return subastas;
    }

    /**
     * Obtiene subastas completadas en un rango de fechas.
     * @param inicio Fecha de inicio
     * @param fin Fecha de fin
     * @return Lista de subastas en ese rango
     * @throws SQLException Si hay error de base de datos
     */
    public List<SubastaCompletada> obtenerPorRangoFechas(Timestamp inicio, Timestamp fin) throws SQLException {
        String sql = """
            SELECT * FROM subastas_completadas
            WHERE fecha_finalizacion BETWEEN ? AND ?
            ORDER BY fecha_finalizacion DESC
        """;
        List<SubastaCompletada> subastas = new ArrayList<>();

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setTimestamp(1, inicio);
            stmt.setTimestamp(2, fin);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subastas.add(mapearResultSet(rs));
                }
            }
        }

        return subastas;
    }

    /**
     * Cuenta el total de subastas completadas.
     * @return Número total de subastas
     * @throws SQLException Si hay error de base de datos
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM subastas_completadas";

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }

    /**
     * Cuenta las subastas completadas por un comprador.
     * @param comprador Nombre del comprador
     * @return Número de subastas ganadas
     * @throws SQLException Si hay error de base de datos
     */
    public int contarPorComprador(String comprador) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM subastas_completadas WHERE comprador = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, comprador);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto SubastaCompletada.
     * @param rs ResultSet de SQLite
     * @return Objeto SubastaCompletada
     * @throws SQLException Si hay error al leer
     */
    private SubastaCompletada mapearResultSet(ResultSet rs) throws SQLException {
        return new SubastaCompletada(
            rs.getString("id_subasta"),
            rs.getString("nombre_articulo"),
            rs.getDouble("precio_final"),
            rs.getString("foto"),
            rs.getString("comprador"),
            rs.getTimestamp("fecha_finalizacion")
        );
    }
}

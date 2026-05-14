package com.subastas.dao;

import com.subastas.modelo.SubastaCompletada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubastaCompletadaDAO {
    private final DatabaseManager dbManager;

    @Autowired
    public SubastaCompletadaDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void insertarSubasta(SubastaCompletada subasta) throws SQLException {
        String sql = """
            INSERT INTO subastas_completadas
            (id_subasta, nombre_articulo, precio_final, foto, comprador, fecha_finalizacion)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql)) {
            stmt.setString(1, subasta.getIdSubasta());
            stmt.setString(2, subasta.getNombreArticulo());
            stmt.setDouble(3, subasta.getPrecioFinal());
            stmt.setString(4, subasta.getFoto());
            stmt.setString(5, subasta.getComprador());
            stmt.setTimestamp(6, subasta.getFechaFinalizacion());
            if (stmt.executeUpdate() == 0) throw new SQLException("No se insertó ninguna fila");
            System.out.println("[SubastaCompletadaDAO] Insertada: " + subasta.getIdSubasta());
        }
    }

    public List<SubastaCompletada> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM subastas_completadas ORDER BY fecha_finalizacion DESC";
        List<SubastaCompletada> lista = new ArrayList<>();
        try (Statement stmt = dbManager.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public SubastaCompletada obtenerPorId(String idSubasta) throws SQLException {
        String sql = "SELECT * FROM subastas_completadas WHERE id_subasta = ?";
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql)) {
            stmt.setString(1, idSubasta);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<SubastaCompletada> obtenerPorComprador(String comprador) throws SQLException {
        String sql = "SELECT * FROM subastas_completadas WHERE comprador = ? ORDER BY fecha_finalizacion DESC";
        List<SubastaCompletada> lista = new ArrayList<>();
        try (PreparedStatement stmt = dbManager.getConexion().prepareStatement(sql)) {
            stmt.setString(1, comprador);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM subastas_completadas";
        try (Statement stmt = dbManager.getConexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private SubastaCompletada mapear(ResultSet rs) throws SQLException {
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

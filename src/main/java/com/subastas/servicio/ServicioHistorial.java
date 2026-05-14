package com.subastas.servicio;

import com.subastas.dao.SubastaCompletadaDAO;
import com.subastas.modelo.SubastaCompletada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ServicioHistorial {
    private final SubastaCompletadaDAO dao;

    @Autowired
    public ServicioHistorial(SubastaCompletadaDAO dao) {
        this.dao = dao;
    }

    public List<SubastaCompletada> obtenerTodas() {
        try {
            return dao.obtenerTodas();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener historial", e);
        }
    }

    public SubastaCompletada obtenerPorId(String id) {
        try {
            return dao.obtenerPorId(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener subasta por ID", e);
        }
    }

    public List<SubastaCompletada> obtenerPorComprador(String comprador) {
        try {
            return dao.obtenerPorComprador(comprador);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener subastas por comprador", e);
        }
    }

    public int contarTotal() {
        try {
            return dao.contarTotal();
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar subastas", e);
        }
    }
}

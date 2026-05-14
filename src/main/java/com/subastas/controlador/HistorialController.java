package com.subastas.controlador;

import com.subastas.modelo.SubastaCompletada;
import com.subastas.servicio.ServicioHistorial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/historial")
public class HistorialController {
    private final ServicioHistorial servicioHistorial;

    @Autowired
    public HistorialController(ServicioHistorial servicioHistorial) {
        this.servicioHistorial = servicioHistorial;
    }

    @GetMapping
    public ResponseEntity<List<SubastaCompletada>> obtenerTodas() {
        return ResponseEntity.ok(servicioHistorial.obtenerTodas());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> count() {
        return ResponseEntity.ok(Map.of("total", servicioHistorial.contarTotal()));
    }

    @GetMapping("/comprador/{username}")
    public ResponseEntity<List<SubastaCompletada>> porComprador(@PathVariable String username) {
        return ResponseEntity.ok(servicioHistorial.obtenerPorComprador(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaCompletada> obtenerPorId(@PathVariable String id) {
        SubastaCompletada subasta = servicioHistorial.obtenerPorId(id);
        if (subasta != null) return ResponseEntity.ok(subasta);
        return ResponseEntity.notFound().build();
    }
}

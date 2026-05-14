package com.subastas.controlador;

import com.subastas.modelo.CrearSubastaRequest;
import com.subastas.modelo.PujaRequest;
import com.subastas.modelo.SubastaActiva;
import com.subastas.servicio.ServicioSubastas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/subasta")
public class SubastaController {
    private final ServicioSubastas servicioSubastas;

    @Autowired
    public SubastaController(ServicioSubastas servicioSubastas) {
        this.servicioSubastas = servicioSubastas;
    }

    @GetMapping
    public ResponseEntity<List<SubastaActiva>> listar(
            @RequestParam(required = false) String filtro) {
        return ResponseEntity.ok(servicioSubastas.buscarPorFiltro(filtro));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<SubastaActiva>> buscarPorPrecio(
            @RequestParam(defaultValue = "0") double precioMin,
            @RequestParam(defaultValue = "999999999") double precioMax) {
        return ResponseEntity.ok(servicioSubastas.buscarPorRangoPrecio(precioMin, precioMax));
    }

    @GetMapping("/porTiempo")
    public ResponseEntity<List<SubastaActiva>> buscarPorTiempo(
            @RequestParam long segundos) {
        return ResponseEntity.ok(servicioSubastas.buscarPorTiempoRestante(segundos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaActiva> obtener(@PathVariable String id) {
        Optional<SubastaActiva> subasta = servicioSubastas.obtenerPorId(id);
        return subasta.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> crear(@RequestBody CrearSubastaRequest request) {
        if (request.getArticulo() == null || request.getDuracionSegundos() <= 0) {
            return ResponseEntity.badRequest().body("Artículo y duracionSegundos > 0 requeridos");
        }
        SubastaActiva subasta = servicioSubastas.crearSubasta(
            request.getArticulo(), request.getDuracionSegundos());
        return ResponseEntity.status(HttpStatus.CREATED).body(subasta.getId());
    }

    @PostMapping("/{id}/pujar")
    public ResponseEntity<String> pujar(
            @PathVariable String id, @RequestBody PujaRequest request) {
        if (request.getUsuario() == null || request.getCantidad() <= 0) {
            return ResponseEntity.badRequest().body("Usuario y cantidad > 0 requeridos");
        }
        boolean aceptada = servicioSubastas.pujar(id, request.getUsuario(), request.getCantidad());
        if (aceptada) {
            return ResponseEntity.ok("Puja aceptada: " + request.getCantidad() + "€ por " + request.getUsuario());
        }
        return ResponseEntity.badRequest().body("Puja rechazada: precio insuficiente o subasta finalizada");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> finalizar(@PathVariable String id) {
        boolean finalizada = servicioSubastas.finalizarSubasta(id);
        if (finalizada) {
            return ResponseEntity.ok("Subasta finalizada: " + id);
        }
        return ResponseEntity.notFound().build();
    }
}

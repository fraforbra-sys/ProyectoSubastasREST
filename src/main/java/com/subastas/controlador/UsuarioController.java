package com.subastas.controlador;

import com.subastas.servicio.ServicioUsuarios;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final ServicioUsuarios servicioUsuarios;

    @Autowired
    public UsuarioController(ServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = servicioUsuarios;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username y password requeridos");
        }
        boolean valido = servicioUsuarios.validarLogin(username, password);
        if (valido) {
            return ResponseEntity.ok("Login exitoso: " + username);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username y password requeridos");
        }
        try {
            servicioUsuarios.registrarUsuario(username, password);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado: " + username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<String> existeUsuario(@PathVariable String username) {
        if (servicioUsuarios.existeUsuario(username)) {
            return ResponseEntity.ok("Usuario existe: " + username);
        }
        return ResponseEntity.notFound().build();
    }
}

package com.subastas.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales de usuario")
public class LoginRequest {

    @Schema(description = "Nombre de usuario", example = "admin")
    private String username;

    @Schema(description = "Contraseña", example = "admin123")
    private String password;

    public LoginRequest() {}

    public String getUsername() { return username; }

    @JsonProperty("username")
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    @JsonProperty("password")
    public void setPassword(String password) { this.password = password; }
}

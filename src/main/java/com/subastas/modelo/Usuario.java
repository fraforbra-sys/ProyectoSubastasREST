package com.subastas.modelo;

public class Usuario {
    private final int id;
    private final String username;
    private final String passwordHash;

    public Usuario(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Usuario(int id, String username) {
        this(id, username, null);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "'}";
    }
}

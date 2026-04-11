package com.pos.model;

import java.time.LocalDateTime;

public class User {
    private int idUser;
    private String username;
    private String password;
    private LocalDateTime createdAt;

    public User() {}

    public User(int idUser, String username, String password, LocalDateTime createdAt) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

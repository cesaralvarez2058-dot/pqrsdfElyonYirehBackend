package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.Data;

@Data
public class RegistrarAdminRequestDTO {
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String rol; // "ADMIN" o "SUPER_ADMIN"
}

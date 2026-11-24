package com.aula.exemplo.demo.dto;

public record LoginResponse(String token, long expiraEm, String username, String nome, String role) {
}

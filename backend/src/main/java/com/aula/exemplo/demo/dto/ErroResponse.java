package com.aula.exemplo.demo.dto;

import java.time.Instant;

public record ErroResponse(int status, String mensagem, Instant timestamp) {

    public ErroResponse(int status, String mensagem) {
        this(status, mensagem, Instant.now());
    }
}

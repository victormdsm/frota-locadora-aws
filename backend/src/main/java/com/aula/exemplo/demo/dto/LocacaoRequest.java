package com.aula.exemplo.demo.dto;

import java.time.LocalDate;

// Corpo do POST /locacoes. usuarioId só é considerado quando quem pede é ADMIN.
public record LocacaoRequest(Long carroId, LocalDate dataInicio, LocalDate dataFimPrevista, Long usuarioId) {
}

package com.aula.exemplo.demo.service;

import org.springframework.stereotype.Service;

// Regras de multa por excesso de velocidade (CTB, art. 218).
// Só lógica: não usa repository nem outra classe -> teste unitário puro, sem Mockito.
@Service
public class MultaService {

    // Quantos % a velocidade medida passou da permitida (0 se não passou).
    public double calcularExcesso(int velocidadePermitida, int velocidadeMedida) {
        validar(velocidadePermitida, velocidadeMedida);
        if (velocidadeMedida <= velocidadePermitida) {
            return 0;
        }
        return (velocidadeMedida - velocidadePermitida) * 100.0 / velocidadePermitida;
    }


    // Até 20%: MEDIA · de 20% até 50%: GRAVE · acima de 50%: GRAVISSIMA
    public String classificar(int velocidadePermitida, int velocidadeMedida) {
        double excesso = calcularExcesso(velocidadePermitida, velocidadeMedida);
        if (excesso == 0) {
            return "SEM_INFRACAO";
        }
        if (excesso <= 20) {
            return "MEDIA";
        }
        if (excesso <= 50) {
            return "GRAVE";
        }
        return "GRAVISSIMA";
    }


    public double calcularValor(int velocidadePermitida, int velocidadeMedida) {
        return switch (classificar(velocidadePermitida, velocidadeMedida)) {
            case "MEDIA" -> 130.16;
            case "GRAVE" -> 195.23;
            case "GRAVISSIMA" -> 880.41;
            default -> 0;
        };
    }


    public int calcularPontos(int velocidadePermitida, int velocidadeMedida) {
        return switch (classificar(velocidadePermitida, velocidadeMedida)) {
            case "MEDIA" -> 4;
            case "GRAVE" -> 5;
            case "GRAVISSIMA" -> 7;
            default -> 0;
        };
    }


    private void validar(int velocidadePermitida, int velocidadeMedida) {
        if (velocidadePermitida <= 0) {
            throw new IllegalArgumentException("A velocidade permitida deve ser maior que zero.");
        }
        if (velocidadeMedida < 0) {
            throw new IllegalArgumentException("A velocidade medida não pode ser negativa.");
        }
    }

}

package com.aula.exemplo.demo.exception;

// Violação de regra do negócio (ex: locar um carro que já está locado) -> 400
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

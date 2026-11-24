package com.aula.exemplo.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Saida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer soma;
    private Integer maiorNumero;

    public Saida() {
    }

    public Saida(Long id, Integer soma, Integer maiorNumero) {
        this.id = id;
        this.soma = soma;
        this.maiorNumero = maiorNumero;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSoma() {
        return soma;
    }

    public void setSoma(Integer soma) {
        this.soma = soma;
    }

    public Integer getMaiorNumero() {
        return maiorNumero;
    }

    public void setMaiorNumero(Integer maiorNumero) {
        this.maiorNumero = maiorNumero;
    }
}

package com.aula.exemplo.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

// Veículo da frota.
@Entity
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String modelo;

    @Column(unique = true)
    private String placa;

    private Integer ano;

    private BigDecimal valorDiaria;

    @Enumerated(EnumType.STRING)
    private StatusCarro status = StatusCarro.DISPONIVEL;

    @ManyToOne
    @JoinColumn(name = "marca_id")
    private Marca marca;

    public Carro() {
    }

    public Carro(Long id, String modelo, Marca marca) {
        this.id = id;
        this.modelo = modelo;
        this.marca = marca;
    }

    public Carro(Long id, String modelo, Marca marca, String placa, Integer ano, BigDecimal valorDiaria) {
        this.id = id;
        this.modelo = modelo;
        this.marca = marca;
        this.placa = placa;
        this.ano = ano;
        this.valorDiaria = valorDiaria;
        this.status = StatusCarro.DISPONIVEL;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public StatusCarro getStatus() {
        return status;
    }

    public void setStatus(StatusCarro status) {
        this.status = status;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }
}

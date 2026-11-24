package com.aula.exemplo.demo.model;

import java.util.List;

// Objeto que chega na requisição: {"lista": [2, 4, 5]}
public class Entrada {

    private List<Integer> lista;

    public Entrada() {
    }

    public Entrada(List<Integer> lista) {
        this.lista = lista;
    }


    public List<Integer> getLista() {
        return lista;
    }

    public void setLista(List<Integer> lista) {
        this.lista = lista;
    }
}

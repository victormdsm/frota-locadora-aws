package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.model.Entrada;
import com.aula.exemplo.demo.model.Saida;
import com.aula.exemplo.demo.repository.CalculoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculoService {

    private final CalculoRepository calculoRepository;

    public CalculoService(CalculoRepository calculoRepository) {
        this.calculoRepository = calculoRepository;
    }


    public Saida calcular(Entrada entrada) {
        Saida saida = new Saida();
        saida.setSoma(somar(entrada.getLista()));
        saida.setMaiorNumero(buscarMaiorNumero(entrada.getLista()));
        return saida;
    }


    public Integer somar(List<Integer> lista) {
        int soma = 0;
        for (int i = 0; i < lista.size(); i++) {
            soma += lista.get(i);
        }
        return soma;
    }


    public Integer buscarMaiorNumero(List<Integer> lista) {
        int maior = lista.get(0);
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i) > maior) {
                maior = lista.get(i);
            }
        }
        return maior;
    }


    public List<Saida> findAll() {
        return calculoRepository.findAll();
    }


    public Saida findById(Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("O id não pode ser negativo: " + id);
        }
        return calculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cálculo não encontrado com o id: " + id));
    }

}

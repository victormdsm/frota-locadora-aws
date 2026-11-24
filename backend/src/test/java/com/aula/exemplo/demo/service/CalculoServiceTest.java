package com.aula.exemplo.demo.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CalculoServiceTest {

    @Autowired
    CalculoService calculoService;

    @Test
    void cenario01() {
        List<Integer> lista = new ArrayList<>();
        lista.add(1);
        lista.add(2);
        lista.add(3);
        Integer retorno = calculoService.somar(lista);
        Assertions.assertEquals(6, retorno);
    }

    @Test
    void cenario02() {
        List<Integer> lista = new ArrayList<>();
        lista.add(null);
        lista.add(2);
        lista.add(3);

        Assertions.assertThrows(NullPointerException.class, () -> calculoService.somar(lista));

    }

}

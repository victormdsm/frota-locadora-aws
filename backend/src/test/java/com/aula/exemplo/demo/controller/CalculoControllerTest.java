package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.model.Saida;
import com.aula.exemplo.demo.repository.CalculoRepository;
import com.aula.exemplo.demo.service.CalculoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
public class CalculoControllerTest {

    @Autowired
    CalculoController calculoController;

    @MockitoBean
    private CalculoRepository calculoRepository;

    @BeforeEach
    void setUp() {
        List<Saida> lista = new ArrayList<>();
        lista.add(new Saida(1L, 10, 5));
        lista.add(new Saida(2L, 20, 8));
        lista.add(new Saida(3L, 30, 12));
        when(calculoRepository.findAll()).thenReturn(lista);

    }

    @Test
    public void cenario01() {
        ResponseEntity<List<Saida>> responseEntity = calculoController.findAll();
        Assertions.assertEquals(3, responseEntity.getBody().size());

    }

    @Test
    public void cenario02() {
        ResponseEntity<List<Saida>> responseEntity = calculoController.findAll();
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertEquals(1L, responseEntity.getBody().get(0).getId());

    }
}

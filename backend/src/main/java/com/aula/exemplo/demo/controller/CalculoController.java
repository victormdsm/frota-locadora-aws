package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.model.Entrada;
import com.aula.exemplo.demo.model.Saida;
import com.aula.exemplo.demo.service.CalculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calculo")
@CrossOrigin("*")
public class CalculoController {

    private final CalculoService calculoService;

    public CalculoController(CalculoService calculoService) {
        this.calculoService = calculoService;
    }


    @PostMapping
    public ResponseEntity<Saida> calcular(@RequestBody Entrada entrada) {
        try {
            return ResponseEntity.ok(calculoService.calcular(entrada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping
    public ResponseEntity<List<Saida>> findAll() {
        return ResponseEntity.ok(calculoService.findAll());
    }


    // A exceção não sobe: a controller captura e devolve 400 (Bad Request).
    @GetMapping("/{id}")
    public ResponseEntity<Saida> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(calculoService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

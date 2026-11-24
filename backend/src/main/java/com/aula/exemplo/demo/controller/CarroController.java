package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.model.Carro;
import com.aula.exemplo.demo.service.CarroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carros")
public class CarroController {

    private final CarroService carroService;

    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }


    @GetMapping
    public ResponseEntity<List<Carro>> findAll() {
        return ResponseEntity.ok(carroService.findAll());
    }


    // Carros livres para locar (ADMIN e USER)
    @GetMapping("/disponiveis")
    public ResponseEntity<List<Carro>> findDisponiveis() {
        return ResponseEntity.ok(carroService.findDisponiveis());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Carro> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carroService.findById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Carro> save(@RequestBody Carro carro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carroService.save(carro));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Carro> update(@PathVariable Long id, @RequestBody Carro carro) {
        return ResponseEntity.ok(carroService.update(id, carro));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carroService.delete(id);
        return ResponseEntity.noContent().build();
    }


}

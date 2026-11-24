package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.model.Marca;
import com.aula.exemplo.demo.service.MarcaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }


    @GetMapping
    public ResponseEntity<List<Marca>> findAll() {
        return ResponseEntity.ok(marcaService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Marca> findById(@PathVariable Long id) {
        return ResponseEntity.ok(marcaService.findById(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Marca> save(@RequestBody Marca marca) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.save(marca));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Marca> update(@PathVariable Long id, @RequestBody Marca marca) {
        return ResponseEntity.ok(marcaService.update(id, marca));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        marcaService.delete(id);
        return ResponseEntity.noContent().build();
    }


}

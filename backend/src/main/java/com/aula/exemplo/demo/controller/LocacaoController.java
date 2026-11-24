package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.dto.LocacaoRequest;
import com.aula.exemplo.demo.model.Locacao;
import com.aula.exemplo.demo.service.LocacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locacoes")
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }


    // Todas as locações: só ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Locacao>> findAll() {
        return ResponseEntity.ok(locacaoService.findAll());
    }


    // Locações do usuário logado: ADMIN ou USER
    @GetMapping("/minhas")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Locacao>> findMinhas(Authentication auth) {
        return ResponseEntity.ok(locacaoService.findMinhas(auth.getName()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Locacao> findById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(locacaoService.findById(id, auth.getName(), isAdmin(auth)));
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Locacao> locar(@RequestBody LocacaoRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.locar(request, auth.getName(), isAdmin(auth)));
    }


    // Dono da locação ou ADMIN
    @PutMapping("/{id}/devolver")
    public ResponseEntity<Locacao> devolver(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(locacaoService.devolver(id, auth.getName(), isAdmin(auth)));
    }


    // Só ADMIN cancela
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Locacao> cancelar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(locacaoService.cancelar(id, auth.getName(), true));
    }


    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

}

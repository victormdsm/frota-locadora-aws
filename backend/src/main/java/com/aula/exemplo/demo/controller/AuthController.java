package com.aula.exemplo.demo.controller;

import com.aula.exemplo.demo.dto.LoginRequest;
import com.aula.exemplo.demo.dto.LoginResponse;
import com.aula.exemplo.demo.model.Usuario;
import com.aula.exemplo.demo.service.AuthService;
import com.aula.exemplo.demo.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }


    // Público: recebe usuário/senha e devolve o JWT
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }


    // Autenticado: quem sou eu? (o username vem do "sub" do token)
    @GetMapping("/me")
    public ResponseEntity<Usuario> me(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.findByUsername(authentication.getName()));
    }

}

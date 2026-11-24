package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.dto.LoginRequest;
import com.aula.exemplo.demo.dto.LoginResponse;
import com.aula.exemplo.demo.model.Usuario;
import com.aula.exemplo.demo.repository.UsuarioRepository;
import com.aula.exemplo.demo.security.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }


    public LoginResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.senha() == null) {
            throw new BadCredentialsException("Usuário ou senha inválidos.");
        }

        Usuario usuario = usuarioRepository.findByUsername(request.username().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Usuário ou senha inválidos."));

        // compara a senha digitada com o hash BCrypt salvo no banco
        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BadCredentialsException("Usuário ou senha inválidos.");
        }

        String token = tokenService.gerarToken(usuario);
        return new LoginResponse(token, tokenService.getExpiracaoSegundos(), usuario.getUsername(),
                usuario.getNome(), usuario.getRole().name());
    }

}

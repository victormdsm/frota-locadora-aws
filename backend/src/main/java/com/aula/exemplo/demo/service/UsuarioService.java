package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.exception.RegraNegocioException;
import com.aula.exemplo.demo.model.Role;
import com.aula.exemplo.demo.model.Usuario;
import com.aula.exemplo.demo.repository.LocacaoRepository;
import com.aula.exemplo.demo.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final LocacaoRepository locacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, LocacaoRepository locacaoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.locacaoRepository = locacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }


    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o id: " + id));
    }


    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + username));
    }


    public Usuario save(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new RegraNegocioException("Informe o username.");
        }
        if (usuario.getSenha() == null || usuario.getSenha().length() < 6) {
            throw new RegraNegocioException("A senha deve ter pelo menos 6 caracteres.");
        }
        String username = usuario.getUsername().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(username)) {
            throw new RegraNegocioException("Já existe um usuário com o username " + username + ".");
        }
        usuario.setId(null);
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        if (usuario.getRole() == null) {
            usuario.setRole(Role.USER);
        }
        return usuarioRepository.save(usuario);
    }


    // Senha só é trocada se vier preenchida.
    public Usuario update(Long id, Usuario usuario, String usernameLogado) {
        Usuario existente = findById(id);
        if (existente.getUsername().equals(usernameLogado) && usuario.getRole() != null
                && usuario.getRole() != existente.getRole()) {
            throw new RegraNegocioException("Você não pode alterar o seu próprio perfil de acesso.");
        }
        existente.setNome(usuario.getNome());
        if (usuario.getRole() != null) {
            existente.setRole(usuario.getRole());
        }
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            if (usuario.getSenha().length() < 6) {
                throw new RegraNegocioException("A senha deve ter pelo menos 6 caracteres.");
            }
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        return usuarioRepository.save(existente);
    }


    public void delete(Long id, String usernameLogado) {
        Usuario usuario = findById(id);
        if (usuario.getUsername().equals(usernameLogado)) {
            throw new RegraNegocioException("Você não pode excluir o próprio usuário.");
        }
        if (locacaoRepository.existsByUsuarioId(id)) {
            throw new RegraNegocioException("Este usuário possui locações registradas e não pode ser excluído.");
        }
        usuarioRepository.delete(usuario);
    }

}

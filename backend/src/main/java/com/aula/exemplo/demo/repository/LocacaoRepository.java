package com.aula.exemplo.demo.repository;

import com.aula.exemplo.demo.model.Locacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocacaoRepository extends JpaRepository<Locacao, Long> {

    List<Locacao> findByUsuarioUsernameOrderByIdDesc(String username);

    List<Locacao> findAllByOrderByIdDesc();

    boolean existsByCarroId(Long carroId);

    boolean existsByUsuarioId(Long usuarioId);
}

package com.aula.exemplo.demo.repository;

import com.aula.exemplo.demo.model.Carro;
import com.aula.exemplo.demo.model.StatusCarro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarroRepository extends JpaRepository<Carro, Long> {

    List<Carro> findByStatus(StatusCarro status);

    boolean existsByMarcaId(Long marcaId);
}

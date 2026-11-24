package com.aula.exemplo.demo.repository;

import com.aula.exemplo.demo.model.Saida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculoRepository extends JpaRepository<Saida, Long> {
}

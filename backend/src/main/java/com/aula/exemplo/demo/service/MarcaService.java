package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.exception.RegraNegocioException;
import com.aula.exemplo.demo.model.Marca;
import com.aula.exemplo.demo.repository.CarroRepository;
import com.aula.exemplo.demo.repository.MarcaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final CarroRepository carroRepository;

    public MarcaService(MarcaRepository marcaRepository, CarroRepository carroRepository) {
        this.marcaRepository = marcaRepository;
        this.carroRepository = carroRepository;
    }


    public List<Marca> findAll() {
        return marcaRepository.findAll();
    }


    public Marca findById(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marca não encontrada com o id: " + id));
    }


    public Marca save(Marca marca) {
        return marcaRepository.save(marca);
    }


    public Marca update(Long id, Marca marca) {
        Marca marcaExistente = findById(id);
        marcaExistente.setNome(marca.getNome());
        return marcaRepository.save(marcaExistente);
    }


    public void delete(Long id) {
        Marca marca = findById(id);
        if (carroRepository.existsByMarcaId(id)) {
            throw new RegraNegocioException("Existem carros desta marca. Remova-os antes de excluir a marca.");
        }
        marcaRepository.delete(marca);
    }

}

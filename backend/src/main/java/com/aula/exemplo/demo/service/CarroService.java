package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.exception.RegraNegocioException;
import com.aula.exemplo.demo.model.Carro;
import com.aula.exemplo.demo.model.Marca;
import com.aula.exemplo.demo.model.StatusCarro;
import com.aula.exemplo.demo.repository.CarroRepository;
import com.aula.exemplo.demo.repository.LocacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarroService {

    private final CarroRepository carroRepository;
    private final LocacaoRepository locacaoRepository;
    private final MarcaService marcaService;

    public CarroService(CarroRepository carroRepository, LocacaoRepository locacaoRepository, MarcaService marcaService) {
        this.carroRepository = carroRepository;
        this.locacaoRepository = locacaoRepository;
        this.marcaService = marcaService;
    }


    public List<Carro> findAll() {
        return carroRepository.findAll();
    }


    public List<Carro> findDisponiveis() {
        return carroRepository.findByStatus(StatusCarro.DISPONIVEL);
    }


    public Carro findById(Long id) {
        return carroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado com o id: " + id));
    }


    public Carro save(Carro carro) {
        validar(carro);
        carro.setId(null);
        carro.setMarca(buscarMarca(carro.getMarca()));
        carro.setPlaca(carro.getPlaca().trim().toUpperCase());
        if (carro.getStatus() == null || carro.getStatus() == StatusCarro.LOCADO) {
            carro.setStatus(StatusCarro.DISPONIVEL);
        }
        return carroRepository.save(carro);
    }


    public Carro update(Long id, Carro carro) {
        validar(carro);
        Carro carroExistente = findById(id);
        carroExistente.setModelo(carro.getModelo());
        carroExistente.setMarca(buscarMarca(carro.getMarca()));
        carroExistente.setPlaca(carro.getPlaca().trim().toUpperCase());
        carroExistente.setAno(carro.getAno());
        carroExistente.setValorDiaria(carro.getValorDiaria());

        // O status LOCADO é controlado só pelas locações (locar / devolver).
        if (carro.getStatus() != null && carroExistente.getStatus() != StatusCarro.LOCADO) {
            if (carro.getStatus() == StatusCarro.LOCADO) {
                throw new RegraNegocioException("Para marcar como LOCADO, registre uma locação.");
            }
            carroExistente.setStatus(carro.getStatus());
        }
        return carroRepository.save(carroExistente);
    }


    public void delete(Long id) {
        Carro carro = findById(id);
        if (locacaoRepository.existsByCarroId(id)) {
            throw new RegraNegocioException("Este carro possui locações registradas e não pode ser excluído. Coloque-o em MANUTENCAO.");
        }
        carroRepository.delete(carro);
    }


    private void validar(Carro carro) {
        if (carro.getModelo() == null || carro.getModelo().isBlank()) {
            throw new RegraNegocioException("Informe o modelo do carro.");
        }
        if (carro.getPlaca() == null || carro.getPlaca().isBlank()) {
            throw new RegraNegocioException("Informe a placa do carro.");
        }
        if (carro.getValorDiaria() == null || carro.getValorDiaria().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da diária deve ser maior que zero.");
        }
    }


    // O carro chega com a marca apenas pelo id ({"marca": {"id": 1}}),
    // então buscamos a marca completa antes de salvar.
    private Marca buscarMarca(Marca marca) {
        if (marca == null || marca.getId() == null) {
            throw new RegraNegocioException("Informe o id da marca do carro.");
        }
        return marcaService.findById(marca.getId());
    }

}

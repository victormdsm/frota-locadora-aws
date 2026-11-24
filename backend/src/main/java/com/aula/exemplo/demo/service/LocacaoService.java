package com.aula.exemplo.demo.service;

import com.aula.exemplo.demo.dto.LocacaoRequest;
import com.aula.exemplo.demo.exception.RegraNegocioException;
import com.aula.exemplo.demo.model.Carro;
import com.aula.exemplo.demo.model.Locacao;
import com.aula.exemplo.demo.model.StatusCarro;
import com.aula.exemplo.demo.model.StatusLocacao;
import com.aula.exemplo.demo.model.Usuario;
import com.aula.exemplo.demo.repository.CarroRepository;
import com.aula.exemplo.demo.repository.LocacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LocacaoService {

    private final LocacaoRepository locacaoRepository;
    private final CarroRepository carroRepository;
    private final CarroService carroService;
    private final UsuarioService usuarioService;

    public LocacaoService(LocacaoRepository locacaoRepository, CarroRepository carroRepository,
                          CarroService carroService, UsuarioService usuarioService) {
        this.locacaoRepository = locacaoRepository;
        this.carroRepository = carroRepository;
        this.carroService = carroService;
        this.usuarioService = usuarioService;
    }


    public List<Locacao> findAll() {
        return locacaoRepository.findAllByOrderByIdDesc();
    }


    public List<Locacao> findMinhas(String username) {
        return locacaoRepository.findByUsuarioUsernameOrderByIdDesc(username);
    }


    // ADMIN vê qualquer locação; USER só as próprias.
    public Locacao findById(Long id, String username, boolean admin) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada com o id: " + id));
        verificarDono(locacao, username, admin);
        return locacao;
    }


    @Transactional
    public Locacao locar(LocacaoRequest request, String username, boolean admin) {
        if (request == null || request.carroId() == null) {
            throw new RegraNegocioException("Informe o carro a ser locado.");
        }
        LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : LocalDate.now();
        LocalDate fim = request.dataFimPrevista();
        if (fim == null) {
            throw new RegraNegocioException("Informe a data prevista de devolução.");
        }
        if (inicio.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de início não pode estar no passado.");
        }
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data de devolução deve ser igual ou posterior à data de início.");
        }

        Carro carro = carroService.findById(request.carroId());
        if (carro.getStatus() != StatusCarro.DISPONIVEL) {
            throw new RegraNegocioException("O carro " + carro.getModelo() + " não está disponível (" + carro.getStatus() + ").");
        }

        // ADMIN pode registrar locação para outro usuário; USER sempre loca para si mesmo.
        Usuario usuario = (admin && request.usuarioId() != null)
                ? usuarioService.findById(request.usuarioId())
                : usuarioService.findByUsername(username);

        Locacao locacao = new Locacao();
        locacao.setCarro(carro);
        locacao.setUsuario(usuario);
        locacao.setDataInicio(inicio);
        locacao.setDataFimPrevista(fim);
        locacao.setValorTotal(calcularValor(carro.getValorDiaria(), inicio, fim));
        locacao.setStatus(StatusLocacao.ATIVA);

        carro.setStatus(StatusCarro.LOCADO);
        carroRepository.save(carro);
        return locacaoRepository.save(locacao);
    }


    // Devolução: recalcula o valor pelos dias realmente usados (mínimo 1 diária).
    @Transactional
    public Locacao devolver(Long id, String username, boolean admin) {
        Locacao locacao = findById(id, username, admin);
        if (locacao.getStatus() != StatusLocacao.ATIVA) {
            throw new RegraNegocioException("Somente locações ATIVAS podem ser devolvidas.");
        }
        LocalDate hoje = LocalDate.now();
        LocalDate fimCobrado = hoje.isAfter(locacao.getDataFimPrevista()) ? hoje : locacao.getDataFimPrevista();

        locacao.setDataDevolucao(hoje);
        locacao.setValorTotal(calcularValor(locacao.getCarro().getValorDiaria(), locacao.getDataInicio(), fimCobrado));
        locacao.setStatus(StatusLocacao.FINALIZADA);
        liberarCarro(locacao.getCarro());
        return locacaoRepository.save(locacao);
    }


    @Transactional
    public Locacao cancelar(Long id, String username, boolean admin) {
        Locacao locacao = findById(id, username, admin);
        if (locacao.getStatus() != StatusLocacao.ATIVA) {
            throw new RegraNegocioException("Somente locações ATIVAS podem ser canceladas.");
        }
        locacao.setStatus(StatusLocacao.CANCELADA);
        locacao.setValorTotal(BigDecimal.ZERO);
        liberarCarro(locacao.getCarro());
        return locacaoRepository.save(locacao);
    }


    // Diárias = dias entre início e fim (mínimo 1) x valor da diária
    public BigDecimal calcularValor(BigDecimal valorDiaria, LocalDate inicio, LocalDate fim) {
        long dias = Math.max(1, ChronoUnit.DAYS.between(inicio, fim));
        return valorDiaria.multiply(BigDecimal.valueOf(dias));
    }


    private void liberarCarro(Carro carro) {
        carro.setStatus(StatusCarro.DISPONIVEL);
        carroRepository.save(carro);
    }


    private void verificarDono(Locacao locacao, String username, boolean admin) {
        if (!admin && !locacao.getUsuario().getUsername().equals(username)) {
            throw new AccessDeniedException("Locação de outro usuário.");
        }
    }

}

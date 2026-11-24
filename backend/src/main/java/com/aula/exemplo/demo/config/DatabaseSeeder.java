package com.aula.exemplo.demo.config;

import com.aula.exemplo.demo.model.Carro;
import com.aula.exemplo.demo.model.Marca;
import com.aula.exemplo.demo.model.Role;
import com.aula.exemplo.demo.model.StatusCarro;
import com.aula.exemplo.demo.model.Usuario;
import com.aula.exemplo.demo.repository.CarroRepository;
import com.aula.exemplo.demo.repository.MarcaRepository;
import com.aula.exemplo.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Value("${app.seed.admin.username}")
    private String adminUsername;
    @Value("${app.seed.admin.senha}")
    private String adminSenha;
    @Value("${app.seed.usuario.username}")
    private String usuarioUsername;
    @Value("${app.seed.usuario.senha}")
    private String usuarioSenha;


    // Cria o ADMIN e um USER comum na primeira subida (senha salva com BCrypt)
    @Bean
    public CommandLineRunner popularUsuarios(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!usuarioRepository.existsByUsername(adminUsername)) {
                usuarioRepository.save(new Usuario(null, "Administrador", adminUsername,
                        passwordEncoder.encode(adminSenha), Role.ADMIN));
                System.out.println(">>> Usuário ADMIN criado: " + adminUsername);
            }
            if (!usuarioRepository.existsByUsername(usuarioUsername)) {
                usuarioRepository.save(new Usuario(null, "Usuário Comum", usuarioUsername,
                        passwordEncoder.encode(usuarioSenha), Role.USER));
                System.out.println(">>> Usuário USER criado: " + usuarioUsername);
            }
        };
    }


    @Bean
    public CommandLineRunner popularCarros(MarcaRepository marcaRepository, CarroRepository carroRepository) {
        return args -> {
            if (carroRepository.count() > 0) {
                return;
            }

            Marca volkswagen = marcaRepository.save(new Marca(null, "Volkswagen"));
            Marca chevrolet = marcaRepository.save(new Marca(null, "Chevrolet"));
            Marca toyota = marcaRepository.save(new Marca(null, "Toyota"));
            Marca honda = marcaRepository.save(new Marca(null, "Honda"));
            Marca fiat = marcaRepository.save(new Marca(null, "Fiat"));

            Carro hilux = new Carro(null, "Hilux", toyota, "QRT4E21", 2023, new BigDecimal("320.00"));
            hilux.setStatus(StatusCarro.MANUTENCAO);

            carroRepository.saveAll(List.of(
                    new Carro(null, "Gol", volkswagen, "BRA2E19", 2022, new BigDecimal("119.90")),
                    new Carro(null, "Onix", chevrolet, "PRV7A33", 2024, new BigDecimal("139.90")),
                    new Carro(null, "Corolla", toyota, "MKT3C55", 2023, new BigDecimal("249.00")),
                    new Carro(null, "Civic", honda, "HND1B08", 2022, new BigDecimal("229.00")),
                    new Carro(null, "Argo", fiat, "FIA9D71", 2024, new BigDecimal("129.90")),
                    new Carro(null, "T-Cross", volkswagen, "VWT5F42", 2024, new BigDecimal("189.00")),
                    hilux
            ));

            System.out.println(">>> Banco populado com " + marcaRepository.count() + " marcas e "
                    + carroRepository.count() + " carros.");
        };
    }

}

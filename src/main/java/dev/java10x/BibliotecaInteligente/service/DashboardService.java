package dev.java10x.BibliotecaInteligente.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.java10x.BibliotecaInteligente.dto.ResumoDashboard;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;
import dev.java10x.BibliotecaInteligente.repository.UsuarioRepository;

@Service
public class DashboardService {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final Clock clock;

    public DashboardService(
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository,
            Clock clock) {
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ResumoDashboard resumo() {
        return new ResumoDashboard(
                livroRepository.count(),
                livroRepository.countByStatus(StatusLivros.DISPONIVEL),
                livroRepository.countByStatus(StatusLivros.EMPRESTADO),
                usuarioRepository.count(),
                emprestimoRepository.countByDataDevolucaoRealIsNull(),
                emprestimoRepository.countByDataDevolucaoRealIsNullAndDataDevolucaoPrevistaBefore(
                        LocalDate.now(clock)));
    }
}

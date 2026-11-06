package dev.java10x.BibliotecaInteligente.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/** Dados para registrar um empréstimo. Sem data de devolução, vale o prazo padrão. */
public record EmprestimoRequest(
        @NotNull(message = "Informe o usuário") Long usuarioId,
        @NotNull(message = "Informe o livro") Long livroId,
        LocalDate dataDevolucaoPrevista) {
}

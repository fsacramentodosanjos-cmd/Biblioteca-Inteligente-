package dev.java10x.BibliotecaInteligente.dto;

public record ResumoDashboard(
        long totalLivros,
        long livrosDisponiveis,
        long livrosEmprestados,
        long totalUsuarios,
        long emprestimosAbertos,
        long emprestimosAtrasados) {
}

package dev.java10x.BibliotecaInteligente.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import dev.java10x.BibliotecaInteligente.enums.SituacaoEmprestimo;
import dev.java10x.BibliotecaInteligente.model.Emprestimo;

/** Visão de um empréstimo: não expõe e-mail nem telefone do usuário. */
public record EmprestimoResponse(
        Long id,
        Long livroId,
        String livroTitulo,
        Long usuarioId,
        String usuarioNome,
        LocalDate dataEmprestimo,
        LocalDate dataDevolucaoPrevista,
        LocalDate dataDevolucaoReal,
        int renovacoes,
        SituacaoEmprestimo situacao,
        long diasAtraso,
        BigDecimal multa) {

    public static EmprestimoResponse de(Emprestimo e, LocalDate hoje, BigDecimal multaPorDia) {
        LocalDate fim = e.getDataDevolucaoReal() != null ? e.getDataDevolucaoReal() : hoje;
        long dias = Math.max(0, ChronoUnit.DAYS.between(e.getDataDevolucaoPrevista(), fim));

        SituacaoEmprestimo situacao;
        if (e.getDataDevolucaoReal() != null) {
            situacao = SituacaoEmprestimo.DEVOLVIDO;
        } else if (dias > 0) {
            situacao = SituacaoEmprestimo.ATRASADO;
        } else {
            situacao = SituacaoEmprestimo.EM_ABERTO;
        }

        BigDecimal multa = multaPorDia.multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP);

        return new EmprestimoResponse(
                e.getId(),
                e.getLivro().getId(),
                e.getLivro().getTitulo(),
                e.getUsuario().getId(),
                e.getUsuario().getNome(),
                e.getDataEmprestimo(),
                e.getDataDevolucaoPrevista(),
                e.getDataDevolucaoReal(),
                e.getRenovacoes(),
                situacao,
                dias,
                multa);
    }
}

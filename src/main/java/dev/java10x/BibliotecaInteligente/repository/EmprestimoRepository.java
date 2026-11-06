package dev.java10x.BibliotecaInteligente.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.java10x.BibliotecaInteligente.model.Emprestimo;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    List<Emprestimo> findAllByOrderByDataEmprestimoDescIdDesc();

    List<Emprestimo> findTop5ByOrderByDataEmprestimoDescIdDesc();

    List<Emprestimo> findByUsuarioIdOrderByDataEmprestimoDesc(Long usuarioId);

    List<Emprestimo> findByDataDevolucaoRealIsNullOrderByDataDevolucaoPrevistaAsc();

    @Query("SELECT e FROM Emprestimo e "
            + "WHERE e.dataDevolucaoPrevista < :hoje AND e.dataDevolucaoReal IS NULL "
            + "ORDER BY e.dataDevolucaoPrevista ASC")
    List<Emprestimo> buscarEmprestimosAtrasados(@Param("hoje") LocalDate hoje);

    boolean existsByLivroId(Long livroId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByLivroIdAndDataDevolucaoRealIsNull(Long livroId);

    boolean existsByUsuarioIdAndDataDevolucaoRealIsNullAndDataDevolucaoPrevistaBefore(Long usuarioId, LocalDate data);

    long countByUsuarioIdAndDataDevolucaoRealIsNull(Long usuarioId);

    long countByDataDevolucaoRealIsNull();

    long countByDataDevolucaoRealIsNullAndDataDevolucaoPrevistaBefore(LocalDate data);
}

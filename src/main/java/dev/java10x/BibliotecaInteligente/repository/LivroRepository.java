package dev.java10x.BibliotecaInteligente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.model.Livro;
import jakarta.persistence.LockModeType;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Long> {

    boolean existsByIsbn(String isbn);

    long countByStatus(StatusLivros status);

    List<Livro> findByStatusOrderByTituloAsc(StatusLivros status);

    /** Trava a linha até o fim da transação: impede dois empréstimos simultâneos do mesmo livro. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Livro> findWithLockById(Long id);

    @Query("SELECT l FROM Livro l "
            + "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR l.isbn LIKE CONCAT('%', :termo, '%') "
            + "ORDER BY l.titulo")
    List<Livro> buscar(@Param("termo") String termo);
}

package dev.java10x.BibliotecaInteligente.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.java10x.BibliotecaInteligente.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    @Query("SELECT u FROM Usuario u "
            + "WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :termo, '%')) "
            + "ORDER BY u.nome")
    List<Usuario> buscar(@Param("termo") String termo);
}

package dev.java10x.BibliotecaInteligente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import dev.java10x.BibliotecaInteligente.enums.CategoriaLivro;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;
    @Mock
    private EmprestimoRepository emprestimoRepository;

    private LivroService service;

    @BeforeEach
    void preparar() {
        service = new LivroService(livroRepository, emprestimoRepository);
        when(livroRepository.save(any(Livro.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void cadastroIgnoraIdEStatusEnviadosPeloCliente() {
        Livro livro = livro("  978-85-0000-000-0 ", StatusLivros.PERDIDO);
        livro.setId(99L);

        Livro salvo = service.salvar(livro);

        assertThat(salvo.getId()).isNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusLivros.DISPONIVEL);
        assertThat(salvo.getIsbn()).isEqualTo("978-85-0000-000-0");
    }

    @Test
    void cadastroRecusaIsbnDuplicado() {
        when(livroRepository.existsByIsbn("111")).thenReturn(true);

        assertThatThrownBy(() -> service.salvar(livro("111", StatusLivros.DISPONIVEL)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    void atualizacaoNaoMudaStatusDeLivroEmprestado() {
        Livro existente = livro("111", StatusLivros.EMPRESTADO);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(existente));

        Livro atualizado = service.atualizar(1L, livro("111", StatusLivros.DISPONIVEL));

        assertThat(atualizado.getStatus()).isEqualTo(StatusLivros.EMPRESTADO);
    }

    @Test
    void atualizacaoNaoPermiteMarcarComoEmprestado() {
        Livro existente = livro("111", StatusLivros.DISPONIVEL);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.atualizar(1L, livro("111", StatusLivros.EMPRESTADO)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizacaoPermiteMarcarComoPerdido() {
        Livro existente = livro("111", StatusLivros.DISPONIVEL);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(existente));

        Livro atualizado = service.atualizar(1L, livro("111", StatusLivros.PERDIDO));

        assertThat(atualizado.getStatus()).isEqualTo(StatusLivros.PERDIDO);
    }

    @Test
    void exclusaoRecusaLivroComHistoricoDeEmprestimos() {
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livro("111", StatusLivros.DISPONIVEL)));
        when(emprestimoRepository.existsByLivroId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("histórico");
    }

    private Livro livro(String isbn, StatusLivros status) {
        Livro livro = new Livro();
        livro.setTitulo("Dom Casmurro");
        livro.setAutor("Machado de Assis");
        livro.setIsbn(isbn);
        livro.setEditora("Domínio público");
        livro.setAnoPublicacao(1899);
        livro.setCategoria(CategoriaLivro.ROMANCE);
        livro.setStatus(status);
        return livro;
    }
}

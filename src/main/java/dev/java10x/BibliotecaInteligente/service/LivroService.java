package dev.java10x.BibliotecaInteligente.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.exception.NaoEncontradoException;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    public LivroService(LivroRepository livroRepository, EmprestimoRepository emprestimoRepository) {
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<Livro> listarTodos() {
        return livroRepository.findAll(Sort.by("titulo"));
    }

    /** Busca por título, autor ou ISBN. Sem termo, lista tudo. */
    @Transactional(readOnly = true)
    public List<Livro> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return livroRepository.buscar(termo.trim());
    }

    @Transactional(readOnly = true)
    public List<Livro> listarDisponiveis() {
        return livroRepository.findByStatusOrderByTituloAsc(StatusLivros.DISPONIVEL);
    }

    @Transactional(readOnly = true)
    public Livro buscarPorId(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Livro não encontrado."));
    }

    @Transactional
    public Livro salvar(Livro livro) {
        livro.setId(null); // cadastro nunca sobrescreve um livro existente
        livro.setStatus(StatusLivros.DISPONIVEL);
        livro.setIsbn(livro.getIsbn().trim());

        if (livroRepository.existsByIsbn(livro.getIsbn())) {
            throw new RegraNegocioException("Já existe um livro com este ISBN.");
        }

        return livroRepository.save(livro);
    }

    @Transactional
    public Livro atualizar(Long id, Livro dados) {
        Livro livro = buscarPorId(id);
        String isbn = dados.getIsbn().trim();

        if (!livro.getIsbn().equals(isbn) && livroRepository.existsByIsbn(isbn)) {
            throw new RegraNegocioException("Já existe um livro com este ISBN.");
        }

        livro.setTitulo(dados.getTitulo());
        livro.setAutor(dados.getAutor());
        livro.setIsbn(isbn);
        livro.setEditora(dados.getEditora());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setCategoria(dados.getCategoria());
        atualizarStatus(livro, dados.getStatus());

        return livroRepository.save(livro);
    }

    /**
     * EMPRESTADO só é definido pelo fluxo de empréstimo. Enquanto o livro está emprestado,
     * o status não muda por aqui; para os demais casos vale DISPONIVEL ou PERDIDO.
     */
    private void atualizarStatus(Livro livro, StatusLivros novo) {
        if (novo == null || livro.getStatus() == StatusLivros.EMPRESTADO || novo == livro.getStatus()) {
            return;
        }
        if (novo == StatusLivros.EMPRESTADO) {
            throw new RegraNegocioException("O status Emprestado só é definido ao registrar um empréstimo.");
        }
        livro.setStatus(novo);
    }

    @Transactional
    public void excluir(Long id) {
        Livro livro = buscarPorId(id);

        if (emprestimoRepository.existsByLivroId(id)) {
            throw new RegraNegocioException(
                    "Este livro tem histórico de empréstimos e não pode ser excluído. "
                            + "Se ele saiu do acervo, marque-o como perdido.");
        }

        livroRepository.delete(livro);
    }
}

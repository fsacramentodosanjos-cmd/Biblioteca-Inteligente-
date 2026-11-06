package dev.java10x.BibliotecaInteligente.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import dev.java10x.BibliotecaInteligente.enums.CategoriaLivro;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.model.Emprestimo;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.model.Usuario;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;
import dev.java10x.BibliotecaInteligente.repository.UsuarioRepository;

/**
 * Popula o banco com exemplos para testar a interface.
 * Só roda com BIBLIOTECA_DADOS_DEMO=true e apenas se o banco estiver vazio.
 */
@Component
public class DadosDemo implements CommandLineRunner {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final boolean ativo;

    public DadosDemo(
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoRepository emprestimoRepository,
            @Value("${biblioteca.dados-demo:false}") boolean ativo) {
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.ativo = ativo;
    }

    @Override
    public void run(String... args) {
        if (!ativo || livroRepository.count() > 0 || usuarioRepository.count() > 0) {
            return;
        }

        Livro domCasmurro = livro("Dom Casmurro", "Machado de Assis", "DEMO-0001", "Domínio público", 1899, CategoriaLivro.ROMANCE);
        Livro cortico = livro("O Cortiço", "Aluísio Azevedo", "DEMO-0002", "Domínio público", 1890, CategoriaLivro.ROMANCE);
        livro("Memórias Póstumas de Brás Cubas", "Machado de Assis", "DEMO-0003", "Domínio público", 1881, CategoriaLivro.ROMANCE);
        livro("O Pequeno Príncipe", "Antoine de Saint-Exupéry", "DEMO-0004", "Reynal & Hitchcock", 1943, CategoriaLivro.FICCAO);
        livro("Clean Code", "Robert C. Martin", "DEMO-0005", "Prentice Hall", 2008, CategoriaLivro.PROGRAMACAO);
        livro("Effective Java", "Joshua Bloch", "DEMO-0006", "Addison-Wesley", 2018, CategoriaLivro.PROGRAMACAO);
        livro("Cosmos", "Carl Sagan", "DEMO-0007", "Random House", 1980, CategoriaLivro.CIENCIA);

        Usuario ana = usuario("Ana Souza", "ana@exemplo.com", "71900000001");
        Usuario bruno = usuario("Bruno Lima", "bruno@exemplo.com", "71900000002");
        usuario("Carla Menezes", "carla@exemplo.com", "71900000003");

        LocalDate hoje = LocalDate.now();
        emprestar(domCasmurro, ana, hoje.minusDays(20), hoje.minusDays(6)); // atrasado
        emprestar(cortico, bruno, hoje.minusDays(3), hoje.plusDays(11));    // em dia
    }

    private Livro livro(String titulo, String autor, String isbn, String editora, int ano, CategoriaLivro categoria) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setAutor(autor);
        livro.setIsbn(isbn);
        livro.setEditora(editora);
        livro.setAnoPublicacao(ano);
        livro.setCategoria(categoria);
        livro.setStatus(StatusLivros.DISPONIVEL);
        return livroRepository.save(livro);
    }

    private Usuario usuario(String nome, String email, String telefone) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setDataMatricula(LocalDate.now().minusMonths(2));
        return usuarioRepository.save(usuario);
    }

    private void emprestar(Livro livro, Usuario usuario, LocalDate emprestimoEm, LocalDate previsto) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataEmprestimo(emprestimoEm);
        emprestimo.setDataDevolucaoPrevista(previsto);
        emprestimoRepository.save(emprestimo);

        livro.setStatus(StatusLivros.EMPRESTADO);
        livroRepository.save(livro);
    }
}

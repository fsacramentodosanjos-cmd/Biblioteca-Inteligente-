package dev.java10x.BibliotecaInteligente.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.java10x.BibliotecaInteligente.config.BibliotecaProperties;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoRequest;
import dev.java10x.BibliotecaInteligente.dto.EmprestimoResponse;
import dev.java10x.BibliotecaInteligente.enums.StatusLivros;
import dev.java10x.BibliotecaInteligente.exception.NaoEncontradoException;
import dev.java10x.BibliotecaInteligente.exception.RegraNegocioException;
import dev.java10x.BibliotecaInteligente.model.Emprestimo;
import dev.java10x.BibliotecaInteligente.model.Livro;
import dev.java10x.BibliotecaInteligente.model.Usuario;
import dev.java10x.BibliotecaInteligente.repository.EmprestimoRepository;
import dev.java10x.BibliotecaInteligente.repository.LivroRepository;
import dev.java10x.BibliotecaInteligente.repository.UsuarioRepository;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final BibliotecaProperties regras;
    private final Clock clock;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            UsuarioRepository usuarioRepository,
            LivroRepository livroRepository,
            BibliotecaProperties regras,
            Clock clock) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.regras = regras;
        this.clock = clock;
    }

    // ---------- consultas ----------

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarTodos() {
        return paraRespostas(emprestimoRepository.findAllByOrderByDataEmprestimoDescIdDesc());
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarRecentes() {
        return paraRespostas(emprestimoRepository.findTop5ByOrderByDataEmprestimoDescIdDesc());
    }

    @Transactional(readOnly = true)
    public EmprestimoResponse buscarPorId(Long id) {
        return paraResposta(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarEmAberto() {
        return paraRespostas(emprestimoRepository.findByDataDevolucaoRealIsNullOrderByDataDevolucaoPrevistaAsc());
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarAtrasados() {
        return paraRespostas(emprestimoRepository.buscarEmprestimosAtrasados(hoje()));
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NaoEncontradoException("Usuário não encontrado.");
        }
        return paraRespostas(emprestimoRepository.findByUsuarioIdOrderByDataEmprestimoDesc(usuarioId));
    }

    /** Filtro da tela de empréstimos: "abertos", "atrasados" ou qualquer outro valor para todos. */
    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarPorFiltro(String filtro) {
        return switch (filtro == null ? "" : filtro) {
            case "abertos" -> listarEmAberto();
            case "atrasados" -> listarAtrasados();
            default -> listarTodos();
        };
    }

    // ---------- operações ----------

    @Transactional
    public EmprestimoResponse realizarEmprestimo(EmprestimoRequest pedido) {
        LocalDate hoje = hoje();

        Usuario usuario = usuarioRepository.findById(pedido.usuarioId())
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));

        // A trava na linha do livro evita que dois pedidos simultâneos emprestem o mesmo exemplar.
        Livro livro = livroRepository.findWithLockById(pedido.livroId())
                .orElseThrow(() -> new NaoEncontradoException("Livro não encontrado."));

        if (livro.getStatus() == StatusLivros.PERDIDO) {
            throw new RegraNegocioException("Este livro está marcado como perdido.");
        }
        if (livro.getStatus() != StatusLivros.DISPONIVEL
                || emprestimoRepository.existsByLivroIdAndDataDevolucaoRealIsNull(livro.getId())) {
            throw new RegraNegocioException("Este livro já está emprestado.");
        }
        if (emprestimoRepository.existsByUsuarioIdAndDataDevolucaoRealIsNullAndDataDevolucaoPrevistaBefore(
                usuario.getId(), hoje)) {
            throw new RegraNegocioException(
                    usuario.getNome() + " tem empréstimo em atraso. Registre a devolução antes de emprestar outro livro.");
        }
        if (emprestimoRepository.countByUsuarioIdAndDataDevolucaoRealIsNull(usuario.getId())
                >= regras.getLimiteEmprestimos()) {
            throw new RegraNegocioException(
                    "Limite de " + regras.getLimiteEmprestimos() + " livros emprestados ao mesmo tempo atingido.");
        }

        LocalDate prevista = pedido.dataDevolucaoPrevista() != null
                ? pedido.dataDevolucaoPrevista()
                : hoje.plusDays(regras.getPrazoPadraoDias());

        if (prevista.isBefore(hoje)) {
            throw new RegraNegocioException("A data de devolução não pode ser anterior a hoje.");
        }

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setLivro(livro);
        emprestimo.setDataEmprestimo(hoje);
        emprestimo.setDataDevolucaoPrevista(prevista);

        livro.setStatus(StatusLivros.EMPRESTADO);
        livroRepository.save(livro);

        return paraResposta(emprestimoRepository.save(emprestimo));
    }

    @Transactional
    public EmprestimoResponse devolverLivro(Long id) {
        Emprestimo emprestimo = buscarEntidade(id);

        if (emprestimo.getDataDevolucaoReal() != null) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido.");
        }

        emprestimo.setDataDevolucaoReal(hoje());

        Livro livro = emprestimo.getLivro();
        if (livro.getStatus() == StatusLivros.EMPRESTADO) {
            livro.setStatus(StatusLivros.DISPONIVEL);
            livroRepository.save(livro);
        }

        return paraResposta(emprestimoRepository.save(emprestimo));
    }

    @Transactional
    public EmprestimoResponse renovar(Long id) {
        Emprestimo emprestimo = buscarEntidade(id);
        LocalDate hoje = hoje();

        if (emprestimo.getDataDevolucaoReal() != null) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido.");
        }
        if (emprestimo.getDataDevolucaoPrevista().isBefore(hoje)) {
            throw new RegraNegocioException("Empréstimo em atraso não pode ser renovado. Registre a devolução.");
        }
        if (emprestimo.getRenovacoes() >= regras.getMaxRenovacoes()) {
            throw new RegraNegocioException(
                    "Limite de " + regras.getMaxRenovacoes() + " renovações atingido.");
        }

        emprestimo.setDataDevolucaoPrevista(
                emprestimo.getDataDevolucaoPrevista().plusDays(regras.getPrazoPadraoDias()));
        emprestimo.setRenovacoes(emprestimo.getRenovacoes() + 1);

        return paraResposta(emprestimoRepository.save(emprestimo));
    }

    // ---------- auxiliares ----------

    private Emprestimo buscarEntidade(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Empréstimo não encontrado."));
    }

    private LocalDate hoje() {
        return LocalDate.now(clock);
    }

    private EmprestimoResponse paraResposta(Emprestimo emprestimo) {
        return EmprestimoResponse.de(emprestimo, hoje(), regras.getMultaPorDia());
    }

    private List<EmprestimoResponse> paraRespostas(List<Emprestimo> emprestimos) {
        return emprestimos.stream().map(this::paraResposta).toList();
    }
}
